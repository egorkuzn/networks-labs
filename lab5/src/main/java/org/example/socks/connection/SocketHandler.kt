package org.example.socks.connection

import java.io.IOException
import java.nio.channels.SelectionKey


interface SocketHandler : Handler {
    @Throws(IOException::class)
    fun read(key: SelectionKey?)

    @Throws(IOException::class)
    fun write(key: SelectionKey?)
}
