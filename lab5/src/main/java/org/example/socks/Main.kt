package org.example.socks

import org.example.socks.auth.AuthMode
import org.example.socks.auth.Users
import org.example.socks.proxy.Proxy

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        Proxy(9000, Users().userMap, AuthMode.NO_AUTH).start()
    }
}
