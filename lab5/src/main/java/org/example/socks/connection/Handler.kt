package org.example.socks.connection

import java.io.IOException
import java.nio.channels.SelectionKey


interface Handler {
    @Throws(IOException::class)
    fun close()
    fun accept(key: SelectionKey?)
}
