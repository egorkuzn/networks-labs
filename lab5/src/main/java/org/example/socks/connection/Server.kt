package org.example.socks.connection

import org.example.socks.auth.AuthMode
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel


class Server(port: Int, selector: Selector?, private val users: HashMap<String, String>, authMode: AuthMode) :
    Handler {
    private val authMode: AuthMode
    private val serverChannel = ServerSocketChannel.open()
    private val dns: Dns

    init {
        dns = Dns(port, selector)
        serverChannel.bind(InetSocketAddress(port))
        serverChannel.configureBlocking(false)
        serverChannel.register(selector, SelectionKey.OP_ACCEPT, this)
        this.authMode = authMode
    }

    @Throws(IOException::class)
    fun closeDNS() {
        dns.close()
    }

    @Throws(IOException::class)
    override fun close() {
        serverChannel.close()
    }

    override fun accept(key: SelectionKey?) {
        try {
            if (!key!!.isValid) {
                close()
                return
            }
            Connection(serverChannel.accept(), dns, key.selector(), users, authMode)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}
