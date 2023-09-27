package lab2

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.math.roundToInt

class ClientController(private val socket: Socket, private val id: Int) : Runnable {
    private var bytes: Int = 0
    private val socketStream = socket.getInputStream()
    private lateinit var filename: String
    private var fileSize: Long = 0
    private var bytesCopied: Long = 0
    private var bytesForPeriod: Int = 0
    private var isRepeat = false

    override fun run() {
        println("Client controller $id: started")
        getDownloadParams()

        val path = Path("uploads/${filename}").toAbsolutePath().toString()
        println("Client controller $id: upload \"$path\" creation")

        val file = File(path)
        file.mkdirs()

        if(!file.createNewFile()){
            println("Client controller $id: file already exists. So I will delete it and write something new there")
            file.delete()
            file.createNewFile()
        }

        copyTo(socketStream, file.outputStream())
        val symbol = if (file.length() == fileSize) '=' else '!'
        println("Client controller $id: uploaded. Size ${file.length()} B $symbol= $fileSize B => DONE!")
    }

    private fun copyTo(input: InputStream, out: OutputStream){
        var lastTime = 0L
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

        input.apply {
            bytes = read(buffer)

            while (bytes >= 0) {
                out.write(streamEndControl(bytesCopied, buffer), 0, bytes)
                bytesCopied += bytes

                if(!socket.isClosed)
                    bytes = read(buffer)
                else
                    break

                lastTime += connectionViewController(lastTime, bytesCopied)
            }
        }
    }

    private fun connectionViewController(lastTime: Long, bytesCopied: Long): Long {
        val dt = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - lastTime
        bytesForPeriod += bytes

        if(dt > 3 || bytes < 0) {
            val status = bytesCopied.toDouble() / fileSize.toDouble() * 100.0
            var outputString = "Client controller $id: ${status.roundToInt()}% uploaded"

            if(status.roundToInt() != 100) {
                val speed = bytesForPeriod.toDouble() * 8.0 / dt.toDouble()
                outputString += " || "
                outputString += showSpeed(speed)
            }

            println(outputString)
            bytesForPeriod = 0
            return dt
        }

        return 0L
    }

    private fun showSpeed(speed: Double): String {
        val normalSpeed: Int
        return if(speed > 1024.0 * 1024.0 * 1024.0) {
            normalSpeed = (speed / (1024.0 * 1024.0 * 1024.0)).roundToInt()
            "$normalSpeed GBit/sec speed"
        } else if(speed > 1024.0 * 1024.0) {
            normalSpeed = (speed / (1024.0 * 1024.0)).roundToInt()
            "$normalSpeed MBit/sec speed"
        } else if(speed > 1024.0) {
            normalSpeed = (speed / 1024.0).roundToInt()
            "$normalSpeed KBit/sec speed"
        } else {
            "${speed.roundToInt()} Bit/sec speed"
        }
    }

    private fun streamEndControl(bytesCopied: Long, buffer: ByteArray): ByteArray{
        return if(bytesCopied + bytes > fileSize){
            socket.getOutputStream().bufferedWriter().apply {
                write("Good job!\n")
                flush()
                close()
            }

            println(buffer.decodeToString(bytes - 4, bytes))
            bytes = (fileSize - bytesCopied).toInt()
            val newBuffer = ByteArray(bytes)
            isRepeat = true
            buffer.copyInto(newBuffer, 0, 0, bytes)
            newBuffer
        } else
            buffer
    }

    private fun getDownloadParams() {
        val readerBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
        socketStream.read(readerBuffer)
        val stringOfFileParams = readerBuffer.decodeToString()
        filename = stringOfFileParams.substring(stringOfFileParams.indexOf(' ') + 1,
                                                    stringOfFileParams.lastIndexOf(':'))
        println("Client controller $id: filename - \"$filename\"")
        fileSize = stringOfFileParams.substring(stringOfFileParams.lastIndexOf(':') + 1,
            stringOfFileParams.indexOf(Char(0))).toLong()
        println("Client controller $id: filesize - $fileSize B")
    }
}