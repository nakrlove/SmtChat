package sock

import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable
import java.net.InetAddress
import java.net.Socket

class SocketClient : Serializable {
    private lateinit var socket: Socket
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    fun connect(port: Int) {
        try {
            val socketAddress = InetAddress.getLocalHost()
            socket = Socket(socketAddress, port)
            outputStream = socket.getOutputStream()
            inputStream = socket.getInputStream()
        } catch (e: Exception) {
            println("socket connect exception start!!")
            println("e: $e")
        }
    }

    fun sendData(data: String) {
        outputStream.write(
            (data + "\n").toByteArray(Charsets.UTF_8)
        )
    }

    fun flush() {
        outputStream.flush()
    }

    fun read(): Boolean {
        var isRead = false
        if (inputStream.available() > 0) {
            isRead = true
        }
        inputStream.bufferedReader(Charsets.UTF_8).forEachLine {
            println(it)
        }
        return isRead
    }

    fun closeConnect() {
        outputStream.close()
        inputStream.close()
        socket.close()
        println(" close Client Connect ################")
    }
}

fun main() {
    val socket = SocketClient()
    socket.connect(6789)
    val testData = "Hi, there! 니캉내캉1"
    for (i in 1..10) {
        if(i == 1) {
            socket.sendData("First::니캉내캉1::")
//            socket.flush()
//            println("[First/Nicname]니캉내캉")
        } else {
            socket.sendData("니캉내캉1::${testData}")
//            socket.flush()
//            println(testData)
        }
    }
    socket.sendData("니캉내캉1::END")
    socket.flush()
    var isRead = false
    while (!isRead) {
        isRead = socket.read()
    }
    if (isRead) {
        socket.closeConnect()
    }
}
