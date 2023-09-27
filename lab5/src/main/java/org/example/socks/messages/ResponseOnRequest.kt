package org.example.socks.messages

import java.util.*


class ResponseOnRequest(private val request: Request) {
    private var data: ByteArray = ByteArray(request.bytes.size)

    init {
        data = request.bytes.copyOf()
    }

    fun create(isConnected: Boolean): ByteArray {
        data[0] = SOCKS_5
        data[1] = SUCCEEDED
        if (!request.isCommand(CONNECT_TCP)) data[1] = COMMAND_NOT_SUPPORTED
        if (!isConnected) data[1] = HOST_NOT_AVAILABLE
        if (request.addressType == IPv6) data[1] = ADDRESS_TYPE_NOT_SUPPORTED
        return data
    }

    companion object: ToolsMessage()
}
