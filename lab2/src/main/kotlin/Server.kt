package lab2

import java.io.InputStream
import java.net.ServerSocket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Server: Thread(){
    private var port: Int = 4001
    private var isRunning: Boolean = true

    public fun setPort(port : Int){
        this.port = port
    }

    public override fun run(){
        val workerPool: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val serverSocket = ServerSocket(port, 10)
        var id = 0

        while (isRunning){
            println("Server: new client ${++id} caught")
            val clientSocket = serverSocket.accept()

            if(isHi(clientSocket.getInputStream()))
                workerPool.submit(ClientController(clientSocket, id))
            else
                println("Server: no \"Hi!\" from $id client")
        }
    }

    private fun isHi(socket_stream: InputStream): Boolean {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        socket_stream.read(buffer)
        var string = buffer.decodeToString()
        string = string.substring(0, string.indexOf(Char(0)))
        println("Server: I have something like \"${string}\"")
        return string == "Hi!"
    }
}