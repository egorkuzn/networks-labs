package org.example.socks.messages

import org.example.socks.auth.AuthMode
import org.example.socks.connection.Connection
import java.io.IOException


class MessageReader internal constructor(buff: ByteArray?) {
    companion object: ToolsMessage() {
        @Throws(IOException::class)
        fun readHelloMessage(session: Connection): Hello? { //// читает приветствие от клиента
            /// проверяя на корректность  - вслучае успеха возвращает прочитанное сообщение
            val read_bytes: Int = session.clientChannel!!.read(session.readBuff)
            if (read_bytes == -1) {
                session.close()
                return null
            }

            if (Hello.isCorrectSizeOfMessage(session.readBuff)) {
                session.readBuff = session.readBuff.flip()
                return Hello(session.readBuff)
            }

            return null
        }

        @Throws(IOException::class)
        fun readSubNegotiation(session: Connection): Negotiation? {
            val read_bytes: Int = session.clientChannel!!.read(session.readBuff)
            if (read_bytes == -1) {
                println("get null")
                session.close()
                return null
            }
            session.readBuff = session.readBuff.flip()
            return Negotiation(session.readBuff)
        }

        @Throws(IOException::class)
        fun readRequestMessage(session: Connection): Request? {
            val read_bytes: Int = session.clientChannel!!.read(session.readBuff)
            if (read_bytes == -1) {
                session.close()
                return null
            }
            if (Request.isCorrectSizeOfMessage(session.readBuff)) {
                session.readBuff = session.readBuff.flip()
                return Request(session.readBuff)
            }
            return null
        }

        fun getResponse(hello: Hello, authMode: AuthMode?): ByteArray {
            val data = ByteArray(2)
            data[0] = SOCKS_5
            if (!hello.hasMethod(authMode)) data[1] = NO_ACCEPTABLE_METHODS else data[1] = getCurrentMethod(authMode!!)
            return data
        }
    }
}
