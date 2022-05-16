package sock

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class ClientThread(val socket: Socket): Thread() {

    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    fun connect() {
        try {
            outputStream = socket.getOutputStream()
            inputStream  = socket.getInputStream()
        } catch (e: Exception) {
            println("socket connect exception start!!")
            println("e: $e")
        }
    }


    fun flush() {
        outputStream.flush()
    }

//    fun read(): Boolean {
//        var isRead = false
//        if (inputStream.available() > 0) {
//            isRead = true
//        }
//        inputStream.bufferedReader(Charsets.UTF_8).forEachLine {
//            println(it)
//        }
//        return isRead
//    }

    fun closeConnect() {
        outputStream.close()
        inputStream.close()
        socket.close()
        println(" close Client Connect ################")
    }


    fun sendData(sendMsg: String?){
        sendMsg?.let{
            socket.outputStream?.let{
                it.write( (sendMsg + "\n").toByteArray(Charsets.UTF_8) )
                it.flush()
            }
        }
    }

    fun UserAccept() {
        var message: String? = null
        socket?.getInputStream()?.bufferedReader(Charsets.UTF_8)?.forEachLine { it ->

//            Regex(".*First::.*").matches(it)?.let{ FirstMatche ->
//
//                if(!FirstMatche) return@let
//
//                it.split("::")?.let{ sockMsg ->
//                    //접속사용자 등록 (KEY: IP/닉네임)
//                    println("    list value=<${sockMsg[1]}>")
////                    socketKey = "${socket?.inetAddress}::${sockMsg[1]}"
////                    SocketServer.userList[socketKey!!] =  socket
////                    return@forEachLine
//                }
//
//            }


//            Regex(".*::END.*").matches(it)?.let{ EndMatche ->
//
//                if(!EndMatche) return@let
//
//                it.split("::")?.let{ sockMsg ->
//                    //접속사용자 등록 (KEY: IP/닉네임)
//                    println(" END VALUE =<${sockMsg[0]}>")
//                    socketKey = "${socket?.inetAddress}::${sockMsg[0]}"
//                    SocketServer.getUserList(socketKey!!)?.run {
//                        this.close()
//                        SocketServer.userList.remove(socketKey!!,this)
//                        serverSocket.close()
//                    }
//
//                    println(" socket list count= ${SocketServer.userList.size}")
////                    return@forEachLine
//                }
//
//            }

            it.split("::")?.let{ sockMsg ->
                //접속사용자 등록 (KEY: IP/닉네임)

                message = sockMsg[1]
                println("Message = {${message}}")
                sendData(message)
                return@forEachLine
            }
        }

    }



    override fun run() {
        connect()
        UserAccept()

    }
}