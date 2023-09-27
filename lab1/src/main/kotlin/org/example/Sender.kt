package lab1.src.kotlin

import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket

class Sender(port: Int, group: InetAddress, address: InitAddress) : Thread(){
    private val sleepTime = 1000L
    private val socket = MulticastSocket()
    private val message = "FIT 2022"
    private var packet = DatagramPacket(message.encodeToByteArray(), message.encodeToByteArray().size, group, port)

    override fun run(){
        while (!currentThread().isInterrupted){
            try {
                socket.send(packet)
            } catch (e: IOException){
                e.printStackTrace()
            }

            try {
                sleep(sleepTime)
            } catch (e: InterruptedException){
                e.printStackTrace()
            }
        }
    }
}