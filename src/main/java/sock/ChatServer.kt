package sock

//import kotlinx.coroutines.Runnable
import org.json.JSONObject
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

fun main(){
    ChatServer().startServer()
}

fun JSONObject.sendParse(message: String): String{
    var jsondata = JSONObject().apply {
        put(Client.MESSAGE_KEY, message)
        put(Client.MESSAGE_ID, "conect_id")
    }
    return jsondata.toString()
}

class ChatServer {

    lateinit var serverSocket: ServerSocket



    companion object{

        val connections = mutableListOf<Client>()
        @JvmStatic lateinit var executorService: ExecutorService

        fun removeClient(client: Client) = connections.remove(client)
        fun addClient(client: Client) = connections.add(client)

        fun executorServiceSubmit(runnable: Runnable) = executorService.submit(runnable)

    }

    fun startServer(){

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        try{
            serverSocket = ServerSocket(6789)

        }catch(e: Exception){
            if(!serverSocket.isClosed){
                stopServer()
                return
            }
        }

        val runnable = object: Runnable {

            var clientInfo: String? = null
            override fun run(){
                println(""" ==========================
                        | Waiting Client Connect 
                        | ========================== """.trimMargin())

                RUN@ while(true) {
                    try {

                        val socket = serverSocket.accept()
//                        connections.add(Client(socket))
                        addClient(Client(socket))
                        clientInfo = "[연결 수락 :  ${socket.getRemoteSocketAddress()} : ${Thread.currentThread().getName()}]"
                        println(""" ==========================
                        |           connected check!!
                        |    ${clientInfo} / [연결 개수 : ${connections.size} ]
                        | ========================== """.trimMargin())

                        //접속자 알림
                        connections.forEach { client ->
                            val msg = JSONObject().sendParse("${socket.getRemoteSocketAddress()} : ${Thread.currentThread().getName()} 접속 하였습니다.")
                            println(" connection user:${msg}")
                            client.send(msg)
                        }

                    }catch(e: Exception){
                        if(!serverSocket.isClosed){
                            stopServer()
                            break@RUN
                        }
                    }
                }

            }
        }
        executorServiceSubmit(runnable) // 스레드 풀에서 처리

    }

    fun stopServer(){
        try{
            val iterator = connections.iterator()
            while(iterator.hasNext()){
                var client = iterator.next()
                client.socket.close()
                iterator.remove()
            }
            serverSocket?.let{
                if(!it.isClosed) it.close()
            }

            executorService?.let{
                if(!it.isShutdown) it.shutdown()
            }


        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}


class Client(val socket: Socket) {

    init{
        recevie()
    }
    fun recevie(){

        val runnable = object: Runnable {

            override fun run() {
                try{
                    socket?.getInputStream()?.bufferedReader(Charsets.UTF_8)?.forEachLine { it ->

                        var message: String = it
                        if(message.toUpperCase() == "QUIT"){
//                            message = "${socket.getRemoteSocketAddress()}  : ${Thread.currentThread().getName()} 접속종료 하였습니다."
                            message = JSONObject().sendParse("${socket.getRemoteSocketAddress()} : ${Thread.currentThread().getName()} 접속종료 하였습니다.")
                        }

                        println(message)
                        ChatServer.connections.forEach { client ->
//                            client.send(message+"\n")
                            client.send(message)
                        }

                        if(it?.toUpperCase() == "QUIT"){
                            throw IOException("close client")
                        }
                    }

                }catch(e: Exception){
                    println("""
                     | [recevie 클라이언트 통신 안됨: ${socket.getRemoteSocketAddress()}  : ${Thread.currentThread().getName()}  ]"
                    """.trimIndent())

                    try {
                        ChatServer.removeClient(this@Client)
                        socket.close()
                    }catch(io: IOException){
                        io.printStackTrace()
                    }
                }
            }
        }
        ChatServer.executorServiceSubmit(runnable)
    }

    fun send(data: String){

        val runnable = object: Runnable{

            override fun run() {
                try{
                    socket.outputStream?.let{
//                        it.write( (jsonString + "\n").toByteArray(Charsets.UTF_8) )
                        it.write( (data + "\n").toByteArray(Charsets.UTF_8) )
                        it.flush()
                    }
                }catch (e: Exception){
                    println("""
                     | [send 클라이언트 통신 안됨: ${socket.getRemoteSocketAddress()}  : ${Thread.currentThread().getName()} " ]"
                    """.trimIndent())
                    try {
                        ChatServer.removeClient(this@Client)
                        socket.close()
                    }catch(io: IOException){
                        io.printStackTrace()
                    }
                }
            }
        }
        ChatServer.executorServiceSubmit(runnable)
    }




    companion object{
        const val MESSAGE_KEY = "msgData"
        const val MESSAGE_ID = "msgId"
    }
}

