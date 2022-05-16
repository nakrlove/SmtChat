package sock

import sock.SocketServerRun.Companion.getClient
import sock.SocketServerRun.Companion.setClient
import sock.SocketServerRun.Companion.userMember
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class SocketServerRun {

    companion object{
        val userMember = mutableMapOf<String,ClientThread>()


        fun getClient(clientKey: String) = userMember[clientKey]

        fun setClient(clientKey: String,clientObj: ClientThread) {
            userMember[clientKey] =clientObj
        }
    }


    lateinit var serverSocket: ServerSocket

    fun connect(port: Int) {
        println("server connect start!! port : $port")
        serverSocket = ServerSocket(port)
    }


}

fun main(){

    try {

        val socketServer = SocketServerRun()
        println("############### Ready Start ##########")
        socketServer.connect(6789)
        do {
            val connected: Socket = socketServer.serverSocket.accept()
//            val clientThread = ClientThread(connected)

            var socketKey = "${connected.inetAddress}"
            val client = getClient(socketKey)
            if(client == null) {
                setClient(socketKey!!,ClientThread(connected))
                userMember[socketKey!!]?.start()
            }else {
                client.UserAccept()
            }


        } while (true)

    }catch(e: IOException){
        e.printStackTrace()
    }
}