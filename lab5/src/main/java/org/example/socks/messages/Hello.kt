package org.example.socks.messages

import org.example.socks.auth.AuthMode
import java.nio.ByteBuffer


class Hello(val data: ByteBuffer) {
    init {
        require(data[1] + 2 == data.limit())
    }

    fun hasMethod(authMode: AuthMode?): Boolean {
        val curMethod: Byte = getCurrentMethod(authMode!!)
        for (i in 0 until data[1]) if (curMethod == data[i + 2]) return true
        return false
    }

    companion object: ToolsMessage() {
        fun isCorrectSizeOfMessage(data: ByteBuffer): Boolean {
            return data.position() > 1 && data.position() >= 2 + data[1]
        }
    }
}
