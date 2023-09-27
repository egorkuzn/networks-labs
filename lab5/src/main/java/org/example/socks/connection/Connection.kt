package org.example.socks.connection

import org.example.socks.auth.AuthMode
import org.example.socks.messages.*
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.*


class Connection(
    aux_client: SocketChannel?,
    aux_dns: Dns,
    selector: Selector?,
    users: HashMap<String, String>,
    aux_mod: AuthMode
) :
    SocketHandler {
    private val authMode: AuthMode
    private var serverChannel: SocketChannel? = null
    private val users: HashMap<String, String>
    val clientChannel: SocketChannel?
    private val dns: Dns
    private var state = State.HELLO
    var readBuff = ByteBuffer.allocateDirect(BUFFER_SIZE)
    private var writeBuff: ByteBuffer? = null
    private var hello: Hello? = null
    private var request: Request? = null
    private var negotiation: Negotiation? = null

    init {
        dns = aux_dns
        this.users = users
        clientChannel = aux_client
        clientChannel!!.configureBlocking(false)
        clientChannel.register(selector, SelectionKey.OP_READ, this)
        authMode = aux_mod
    }

    override fun accept(key: SelectionKey?) {
        try {
            if (!key!!.isValid) {
                close()
                key.cancel()
                return
            }
            if (key.isReadable) {
                read(key)
            } else if (key.isWritable) {
                write(key)
            } else if (key.isConnectable && key.channel() == serverChannel) {
                serverConnect(key)
            }
        } catch (ex: IOException) {
            try {
                close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    override fun close() {
        clientChannel?.close()
        serverChannel?.close()
    }

    @Throws(IOException::class)
    override fun read(key: SelectionKey?) {
        if (key!!.channel() == clientChannel) {
            clientRead(key)
        } else if (key.channel() == serverChannel) {
            serverRead(key)
        }
    }

    @Throws(IOException::class)
    private fun clientRead(key: SelectionKey) {
        when (state) {
            State.HELLO -> {
                println("GOT HELLO FROM " + clientChannel!!.socket().inetAddress)
                hello = MessageReader.readHelloMessage(this)
                if (hello == null) return
                key.interestOps(SelectionKey.OP_WRITE)
                readBuff.clear()
            }

            State.NEGOTIATION -> {
                println("GOT NEGOTIATION FROM " + clientChannel!!.socket().inetAddress)
                negotiation = MessageReader.readSubNegotiation(this)
                if (negotiation == null) return
                println(negotiation!!.negotiationRequest)
                key.interestOps(SelectionKey.OP_WRITE)
                readBuff.clear()
            }

            State.REQUEST -> {
                println("GOT REQUEST FROM " + clientChannel!!.socket().inetAddress)
                request = MessageReader.readRequestMessage(this)
                if (request == null) return
                if (!connect()) {
                    serverChannel = null
                    key.interestOps(SelectionKey.OP_WRITE)
                } else {
                    serverChannel!!.register(key.selector(), SelectionKey.OP_CONNECT, this)
                    key.interestOps(0)
                }
                readBuff.clear()
            }

            State.MESSAGE -> {
                println("READ NEW MSG FROM")
                if (readFrom(clientChannel, readBuff)) {
                    serverChannel!!.register(key.selector(), SelectionKey.OP_WRITE, this)
                    key.interestOps(0)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun serverRead(key: SelectionKey) {
        if (readFrom(serverChannel, readBuff)) {
            clientChannel!!.register(key.selector(), SelectionKey.OP_WRITE, this)
            key.interestOps(0)
        }
    }

    @Throws(IOException::class)
    private fun serverConnect(key: SelectionKey) {
        if (!serverChannel!!.isConnectionPending) return
        if (!serverChannel!!.finishConnect()) return
        key.interestOps(0)
        clientChannel!!.register(key.selector(), SelectionKey.OP_WRITE, this)
    }

    @Throws(IOException::class)
    override fun write(key: SelectionKey?) {
        if (key!!.channel() === clientChannel) clientWrite(key!!) else if (key!!.channel() === serverChannel) serverWrite(key!!)
    }

    @Throws(IOException::class)
    private fun clientWrite(key: SelectionKey) {
        when (state) {
            State.HELLO -> {
                if (writeBuff == null) {
                    writeBuff = ByteBuffer.wrap(MessageReader.getResponse(hello!!, authMode))
                }
                if (writeTo(clientChannel, writeBuff)) {
                    writeBuff = null
                    if (hello!!.hasMethod(authMode)) {
                        key.interestOps(SelectionKey.OP_READ)
                        state = if (authMode === AuthMode.AUTH) State.NEGOTIATION else State.REQUEST
                    } else {
                        System.err.println("NOT SUPPORT FFS!" + clientChannel!!.socket().inetAddress + " is closing...")
                        close()
                    }
                    hello = null
                }
            }

            State.NEGOTIATION -> {
                if (writeBuff == null) {
                    negotiation!!.responseControl(users)
                    writeBuff = ByteBuffer.wrap(negotiation!!.response)
                }
                if (writeTo(clientChannel, writeBuff)) {
                    writeBuff = null
                    if (negotiation!!.hasSuccess()) {
                        key.interestOps(SelectionKey.OP_READ)
                        state = State.REQUEST
                    } else {
                        System.err.println("NEGOTIATION FROM " + clientChannel!!.socket().inetAddress + " DENIED")
                        close()
                    }
                    negotiation = null
                }
            }

            State.REQUEST -> {
                if (writeBuff == null) {
                    val response = ResponseOnRequest(request!!)
                    writeBuff = ByteBuffer.wrap(response.create(serverChannel != null)) //
                }
                if (writeTo(clientChannel, writeBuff)) {
                    writeBuff = null
                    if (!request!!.isCommand(Request.CONNECT_TCP) || serverChannel == null) {
                        close()
                        println("NOT TCP REQUEST BY " + clientChannel!!.socket().inetAddress)
                    } else {
                        key.interestOps(SelectionKey.OP_READ)
                        serverChannel!!.register(key.selector(), SelectionKey.OP_READ, this)
                        state = State.MESSAGE
                    }
                    request = null
                }
            }

            State.MESSAGE -> {
                if (writeTo(clientChannel, readBuff)) {
                    key.interestOps(SelectionKey.OP_READ)
                    serverChannel!!.register(key.selector(), SelectionKey.OP_READ, this)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun serverWrite(key: SelectionKey) {
        if (writeTo(serverChannel, readBuff)) {
            key.interestOps(SelectionKey.OP_READ)
            clientChannel!!.register(key.selector(), SelectionKey.OP_READ, this)
        }
    }

    fun connectToServer(address: InetAddress): Boolean {
        println("CONNECTED WITH SERVER SIDE  $address")
        try {
            serverChannel!!.connect(InetSocketAddress(address, request!!.destPort.toInt()))
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    @Throws(IOException::class)
    private fun connect(): Boolean {
        serverChannel = SocketChannel.open()
        serverChannel!!.configureBlocking(false)
        when (request!!.addressType) {
            Request.IPv4 -> return connectToServer(InetAddress.getByAddress(request!!.destAddress))
            Request.IPv6 -> {
                System.err.println("NOT SUPPORT IPV6")
                return false
            }

            Request.DOMAIN_NAME -> {
                dns.sendToResolve(request!!.destAddress.contentToString(), this)
            }
        }
        return true
    }

    @Throws(IOException::class)
    private fun readFrom(channel: SocketChannel?, buffer: ByteBuffer): Boolean {
        buffer.compact()
        val read_bytes = channel!!.read(buffer)
        if (read_bytes == -1) {
            close()
            return false
        }
        if (read_bytes != 0) buffer.flip()
        return read_bytes != 0
    }

    @Throws(IOException::class)
    private fun writeTo(channel: SocketChannel?, buffer: ByteBuffer?): Boolean {
        channel!!.write(buffer)
        return !buffer!!.hasRemaining()
    }

    private enum class State {
        HELLO, REQUEST, MESSAGE, NEGOTIATION
    }

    companion object {
        private const val BUFFER_SIZE = 4096
    }
}
