package lab1.src.kotlin

import java.io.IOException
import java.net.InetAddress

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val ipAddress = "232.1.1.1" // args[0];
        val port = 8888
        val group: InetAddress
        val address = InitAddress()

        try {
            group = InetAddress.getByName(ipAddress)
            val sender = Sender(port, group, address)
            val sender2 = Sender(port, group, address)
            val sender3 = Sender(port, group, address)
            sender.start()
            val receiver = Receiver(port, group, address)
            receiver.start()
            val checker = Checker(address)
            checker.start()
            sender2.start()
            sender3.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}