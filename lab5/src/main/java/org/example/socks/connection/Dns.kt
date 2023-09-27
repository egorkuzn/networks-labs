package org.example.socks.connection

import org.xbill.DNS.*
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.*


class Dns(port: Int, selector: Selector?) : SocketHandler {
    private val resolverChannel = DatagramChannel.open()
    private val DnsServerAddr: InetSocketAddress
    private val readBuff = ByteBuffer.allocateDirect(Message.MAXLENGTH)
    private val writeBuff = ByteBuffer.allocateDirect(Message.MAXLENGTH)
    private val key: SelectionKey
    private val deque: Deque<Message> = LinkedList<Message>()
    private val attachments: MutableMap<Int, Connection> = HashMap()

    init {
        resolverChannel.configureBlocking(false)
        resolverChannel.register(selector, 0, this)
        key = resolverChannel.keyFor(selector)
        resolverChannel.bind(InetSocketAddress(port))
        DnsServerAddr = ResolverConfig.getCurrentConfig().server()
        resolverChannel.connect(DnsServerAddr)
        readBuff.clear()
        writeBuff.clear()
    }

    fun sendToResolve(domainName: String, handler: Connection) {
        try {
            val dnsRequest: Message = Message.newQuery(Record.newRecord(Name("$domainName."), Type.A, DClass.IN))
            deque.addLast(dnsRequest)
            attachments[dnsRequest.getHeader().getID()] = handler
            key.interestOps(key.interestOps() or SelectionKey.OP_WRITE)
        } catch (ex: TextParseException) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    override fun close() {
        resolverChannel.close()
    }

    override fun accept(key: SelectionKey?) {
        try {
            if (!key!!.isValid) {
                close()
                key.cancel()
                return
            }
            if (key.isReadable) read(key) else if (key.isWritable) write(key)
        } catch (ex: IOException) {
            ex.printStackTrace()
            try {
                close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    override fun read(key: SelectionKey?) {
        if (resolverChannel.receive(readBuff) != null) {
            readBuff.flip()
            val data = ByteArray(readBuff.limit())
            readBuff[data]
            readBuff.clear()
            val response = Message(data)
            val session = attachments.remove(response.getHeader().getID())
            for (record in response.getSection(Section.ANSWER)) if (record is ARecord) {
                val it: ARecord = record
                if (session!!.connectToServer(it.getAddress())) break
            }
        }
        if (attachments.isEmpty()) key!!.interestOps(key.interestOps() xor SelectionKey.OP_READ)
    }

    @Throws(IOException::class)
    override fun write(key: SelectionKey?) {
        var dnsRequest: Message? = deque.pollFirst()
        while (dnsRequest != null) {
            writeBuff.clear()
            writeBuff.put(dnsRequest.toWire())
            writeBuff.flip()
            if (resolverChannel.send(writeBuff, DnsServerAddr) == 0) {
                deque.addFirst(dnsRequest)
                break
            }
            key!!.interestOps(key.interestOps() or SelectionKey.OP_READ)
            dnsRequest = deque.pollFirst()
        }
        key!!.interestOps(key.interestOps() xor SelectionKey.OP_WRITE)
    }
}

