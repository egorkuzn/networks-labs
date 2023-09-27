package lab1.src.kotlin

import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface

class Receiver(port: Int, group: InetAddress, private var address: InitAddress): Thread() {
    private val socket = MulticastSocket(port)

    init {
        socket.joinGroup(InetSocketAddress(group, port), NetworkInterface.getByInetAddress(group))
    }

    public override fun run() {
        while (!currentThread().isInterrupted){
            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)

            try {
                socket.receive(packet)
            } catch (e: IOException){
                e.printStackTrace()
            }

            address.add(packet.socketAddress, System.currentTimeMillis())
        }
    }
}