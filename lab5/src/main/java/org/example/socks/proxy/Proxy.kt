package org.example.socks.proxy

import org.example.socks.auth.AuthMode
import org.example.socks.connection.Handler
import org.example.socks.connection.Server
import java.nio.channels.Selector


class Proxy(port: Int, users: HashMap<String, String>, authMode: AuthMode) :
    AutoCloseable, Thread() {
    private val selector = Selector.open()
    private val server: Server

    init {
        server = Server(port, selector, users, authMode)
    }

    @Throws(Exception::class)
    override fun close() {
        selector.close()
        server.close()
        server.closeDNS()
    }

    override fun run() {
        while (!isInterrupted) {
            var count = 0
            try {
                count = selector.select(10000)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            if (count == 0) continue
            val modified = selector.selectedKeys()
            for (selected in modified) {
                val key: Handler = selected.attachment() as Handler
                key.accept(selected)
            }
            modified.clear()
        }
        try {
            close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}