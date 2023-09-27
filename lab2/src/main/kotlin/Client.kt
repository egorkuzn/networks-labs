package lab2

import java.io.File
import java.io.OutputStream
import java.net.Socket
import java.nio.file.Paths

class Client: Thread() {
    private var sourcePath: String = ""
    private var dnsName: String = "localhost"
    private var port: Int = 4001
    private var uploadingStatus: Float = 0f

    public fun setResourcePath(resource_local_path : String){
        this.sourcePath = resource_local_path
    }

    public fun setServerPath(dns_name : String, port : Int){
        this.dnsName = dns_name
        this.port = port
    }

    public fun setAllParams(resource_local_path: String, dns_name: String, port: Int){
        this.sourcePath = resource_local_path
        this.dnsName = dns_name
        this.port = port
    }

    private fun sendToServer(){
        println("Client: started")

        if(!isValidFields()) {
            println("Client: no valid fields")
            return
        }

        val socket = Socket(dnsName, port)
        val socketOutputStream = socket.getOutputStream()
        sayHi(socketOutputStream)
        val file = File(sourcePath)

        socketOutputStream.write(fileParams(file.length()))


        file.forEachBlock(4096){ buffer: ByteArray, bytesRead: Int ->
                socketOutputStream.write(buffer, 0, bytesRead)
            }

        sayBye(socketOutputStream)
        val message = socket.getInputStream().bufferedReader().readLine()
        println("Client: server said me \"$message\"")
        socket.close()
        println("Client: socket close stat is :: ${socket.isClosed} closed")
        clearFields()
    }

    private fun fileParams(length: Long): ByteArray {
        val fileName = if(sourcePath.contains('/'))
            sourcePath.substring(sourcePath.lastIndexOf('/') + 1)
        else
            sourcePath

        return "Filename: ${fileName}:${length}".toByteArray()
    }

    private fun clearFields() {
        sourcePath = ""
        uploadingStatus = 0f
    }

    private fun isValidFields(): Boolean {
        return isValidDNS() &&
                isValidSourcePath() &&
                isValidPort() &&
                isValidStatus()
    }

    private fun isValidStatus(): Boolean {
        return uploadingStatus == 0f
    }

    private fun isValidPort(): Boolean {
        return port > 0
    }

    private fun isValidSourcePath(): Boolean {
//        source_path.matches(Regex(""))
        return true
    }

    private fun isValidDNS(): Boolean {
//        TODO("Not yet implemented")
        return true
    }

    private fun sayBye(socketInputStream: OutputStream) {
        socketInputStream.write("Bye!".toByteArray())
        println("Client: saied \"Bye!\"")
    }

    private fun sayHi(socketInputStream: OutputStream) {
        val hiBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
        "Hi!".toByteArray().copyInto(hiBuffer, 0, 0, 3)
        socketInputStream.write(hiBuffer, 0, DEFAULT_BUFFER_SIZE)
        println("Client: saied \"Hi!\"")
    }

    override fun run() {
        sendToServer()
    }
}