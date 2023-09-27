package org.example.socks.messages

import org.example.socks.auth.AuthMode


open class ToolsMessage {
        val X: Byte = 0x01
        val SUCCESS: Byte = 0x00
        val DENIED: Byte = 0x05
        val SOCKS_5: Byte = 0x05
        val IPv4 = 0x01.toByte()
        val IPv6 = 0x04.toByte()
        val DOMAIN_NAME = 0x03.toByte()
        val AUTH = 0x02.toByte()
        val COMMAND_NOT_SUPPORTED: Byte = 0x07
        val ADDRESS_TYPE_NOT_SUPPORTED: Byte = 0x08
        val SUCCEEDED: Byte = 0x00
        val HOST_NOT_AVAILABLE: Byte = 0x04
        val NO_AUTHENTICATION: Byte = 0x00
        val NO_ACCEPTABLE_METHODS = 0xFF.toByte()
        val CONNECT_TCP = 0x01.toByte()

        fun getCurrentMethod(authMode: AuthMode): Byte {
            return if (authMode === AuthMode.AUTH) AUTH else NO_AUTHENTICATION
        }
}
