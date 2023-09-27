package org.example.socks.messages

import java.nio.ByteBuffer
import java.util.HashMap


class Negotiation(var negotiationRequest: ByteBuffer) {
    lateinit var response: ByteArray
        private set

    init {
        require(negotiationRequest[0] != X)
    }

    fun hasSuccess(): Boolean {
        return response[0] == X && response[1] == SUCCESS
    }

    fun responseControl(map: HashMap<String, String>) {
        val login = ByteArray(negotiationRequest[1].toInt())
        response = ByteArray(2)
        response[0] = X
        response[1] = SUCCESS
        if (negotiationRequest[1] >= 0) System.arraycopy(negotiationRequest, 2, login, 0, negotiationRequest[1].toInt())
        val loginString = login.decodeToString()
        val plen: Int = negotiationRequest[negotiationRequest[1] + 2].toInt()
        val pstart: Int = negotiationRequest[1] + 2
        val password = ByteArray(plen)
        for (i in 0 until plen) password[i] = negotiationRequest[pstart + 1 + i]
        val passwordString = password.decodeToString()
        if (!map.containsKey(loginString)) response[1] = DENIED
        if (response[1] != DENIED && map[loginString] != passwordString) response[1] = DENIED
    }

    companion object: ToolsMessage()
}
