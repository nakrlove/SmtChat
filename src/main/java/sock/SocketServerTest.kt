package sock

import java.io.*
import java.net.ServerSocket
import java.net.Socket

class SocketServerTest : Serializable {


    lateinit var serverSocket: ServerSocket
    private lateinit var clientSocket: Socket
//    private lateinit var inputStream: InputStream
//    private lateinit var outputStream: OutputStream

    fun isClose(): Boolean {
        if (this::serverSocket.isInitialized) {
            return serverSocket.isClosed
        }
        return true
    }

    fun connect(port: Int) {
        println("server connect start!! port : $port")
        serverSocket = ServerSocket(port)
//        clientSocket = serverSocket.accept()
//        inputStream = clientSocket.getInputStream()
//        outputStream = clientSocket.getOutputStream()
//        println(" connect clientSocket = ${clientSocket.inetAddress}")
    }



//
//    fun read(): Boolean {
//        if (inputStream.available() > 0) {
//            inputStream.bufferedReader(Charsets.UTF_8).forEachLine {
//                println(it)
////                ("[First]" == it).apply {
////                    "닉네임"
////                }
//            }
//            return true
//        }
//        return false
//    }
//
//    fun sendData(data: String) {
//        println(data)
//        outputStream.write(
//            (data + "\n").toByteArray(Charsets.UTF_8)
//        )
//        outputStream.flush()
//    }

    fun sendDataAllUser(socketKey: String?,sendMsg: String?){

        socketKey?.let{
            var socketObj = getUserList(it)

            println("Socket Check = key[${socketKey}], Socket =[${socketObj ?: "sock null"}]")

            socketObj?.let{
                it.outputStream?.write(
                    (sendMsg + "\n").toByteArray(Charsets.UTF_8)
                )
                it.outputStream?.flush()
            }
        }


    }

    fun connectClose() {
        serverSocket.close()
    }


    fun AcceptUser(socket: Socket?) {
        var socketKey: String? = null
        var message: String? = null
        socket?.getInputStream()?.bufferedReader(Charsets.UTF_8)?.forEachLine { it ->

            Regex(".*First::.*").matches(it)?.let{ FirstMatche ->

                if(!FirstMatche) return@let

                it.split("::")?.let{ sockMsg ->
                    //접속사용자 등록 (KEY: IP/닉네임)
                    println("    list value=<${sockMsg[1]}>")
                    socketKey = "${socket?.inetAddress}::${sockMsg[1]}"
                    userList[socketKey!!] =  socket
//                    return@forEachLine
                }

            }


            Regex(".*::END.*").matches(it)?.let{ EndMatche ->

                if(!EndMatche) return@let

                it.split("::")?.let{ sockMsg ->
                    //접속사용자 등록 (KEY: IP/닉네임)
                    println(" END VALUE =<${sockMsg[0]}>")
                    socketKey = "${socket?.inetAddress}::${sockMsg[0]}"
                    getUserList(socketKey!!)?.run {
                        this.close()
                        userList.remove(socketKey!!,this)
                        serverSocket.close()
                    }

                    println(" socket list count= ${userList.size}")
//                    return@forEachLine
                }

            }

            it.split("::")?.let{ sockMsg ->
                //접속사용자 등록 (KEY: IP/닉네임)

                message = sockMsg[1]
                println("Message = {${message}}")
                sendDataAllUser(socketKey,message)
                return@forEachLine
            }
        }

    }

    companion object{

        /* 접속 사용자 */
        val userList =  mutableMapOf<String,Socket>()

        /* 생성된 채팅방명 */
        val makedRoom = mutableMapOf<String,Any?>()
//        fun AcceptUser(socket: Socket?) {

//            clientSocket = serverSocket.accept()
//            setAccept(serverSocket.accept())
//                allUser["${socket?.inetAddress}"] ?:  socket
//            inputStream = clientSocket.getInputStream()
//            outputStream = clientSocket.getOutputStream()
//            println(" connect clientSocket = ${clientSocket.inetAddress}")
//        }
        fun getUserList(key: String) = userList[key]
    }
}


fun label() : String?{
    var result: String? = null
    test1@ for(i in 1..10) {
        if( i == 2){
            break@test1
        }
    }
    return result
}
fun main4(){
    val result = label()
    println(result)
}

fun main(){

    try {
        val socketServer = SocketServerTest()
        println("############### Ready Start ##########")

            do {
                if (socketServer.isClose()) {
                    socketServer.connect(6789)
                }
//                else {
//                    socketServer.connectClose()
//                }
                val connected: Socket = socketServer.serverSocket.accept()
                ClientThread(connected).start()
            } while (true)



    }catch(e: IOException){
        e.printStackTrace()
    }
}


fun main1() {
    val socketServer = SocketServerTest()

    while (true) {
        try {

            println("############### Ready Start ##########")
            if (socketServer.isClose()) {
                socketServer.connect(6789)

            } else {
                socketServer.connectClose()
            }
            socketServer.AcceptUser(socketServer.serverSocket.accept())
            var isRead = false
//            while (isRead.not()) {
//                isRead = socketServer.read()
//            }
            println("Finish isRead : $isRead")
//            if (isRead) {
//                socketServer.sendData("response")
//                socketServer.sendDataAllUser("response")

//            }
        } catch (e: Exception) {
            println(e.toString())
//            socketServer.connectClose()
        }
    }
}