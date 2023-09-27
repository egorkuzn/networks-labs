package org.example.socks.messages

import java.nio.ByteBuffer

class Request(private val buffer: ByteBuffer) {
    fun isCommand(command: Byte): Boolean {
        for(i in data) {
            print(i)
        }

        println()

        return command == data[1]
    }

    private var data: ByteArray = ByteArray(buffer.limit())

    init {
        buffer.get(0, data)
    }

    val addressType: Byte
        get() = data[3]
    val destAddress: ByteArray?
        get() = when (addressType) {
                    IPv4 -> data.copyOfRange(4, 8)
                    DOMAIN_NAME -> {
                        val length: Int = data[4].toInt()
                        data.copyOfRange(5, 5 + length)
                    }

                    IPv6 -> data.copyOfRange(4, 20)
                    else -> null
                }

    val destPort: Short
        get() =  when (data[3]) {
                    IPv4 -> buffer.slice(8, 2).short
                    DOMAIN_NAME -> {
                        val length: Int = data[4].toInt()
                        buffer.slice(5 + length, 2).short
                    }

                    IPv6 -> buffer.slice(20, 2).short
                    else -> -1
                }


    val bytes: ByteArray
        get() = data

    companion object: ToolsMessage() {
        private fun isCorrect(data: ByteArray): Boolean {
            if (data.size < 5) return false
            if (data[2].toInt() != 0x00) return false

            when (data[3]) {
                IPv4 -> if (data.size != 10) return false
                IPv6 -> if (data.size != 22) return false
                DOMAIN_NAME -> if (data.size != 7 + data[4]) return false
            }

            return true
        }

        fun isCorrectSizeOfMessage(data: ByteBuffer): Boolean {
            if (data.position() < 5) return false

            when (data[3]) {
                IPv4 -> if (data.position() != 10) return false
                IPv6 -> if (data.position() != 22) return false
                DOMAIN_NAME -> if (data.position() != 7 + data[4]) return false
            }

            return true
        }
    }
}
