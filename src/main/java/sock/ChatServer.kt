package sock

//import kotlinx.coroutines.Runnable
import org.json.JSONObject
import sock.ChatServer.Companion.isUser
import sock.ChatServer.Companion.userCount
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

fun main(){
    ChatServer().startServer()
}

fun JSONObject.putData(key: String, value: String): JSONObject{
    var jsondata = JSONObject().apply {
        put(key, value)
//        put(Client.MESSAGE_ID, "conect_id")
    }
//    return jsondata.toString()
    return jsondata
}

class ChatServer {

    lateinit var serverSocket: ServerSocket



    companion object{

        //접속 Client 담아두기
        val connections = mutableListOf<Client>()
        //사용자 담아두기
        val users = hashMapOf<String,String>()

        @JvmStatic lateinit var ServerExecutorService: ExecutorService
        @JvmStatic lateinit var ClientExecutorService: ExecutorService

        fun removeClient(client: Client) = connections.remove(client)
        fun removeUser(connectInfo: String) = users.remove(connectInfo)
        fun addClient(client: Client) = connections.add(client)

        fun userCount() = users.count()
        fun ServerServiceSubmit(runnable: Runnable) = ServerExecutorService.submit(runnable)
        fun ClientServiceSubmit(runnable: Runnable) = ClientExecutorService.submit(runnable)

        // 동명사용자 여부 확인
        fun isUser(protocal: String ,user: String): Boolean{
            val result = users.containsKey(protocal)
            println("1 isUser ${result} ")
            if(!result){
                users.put("${protocal}",user)
            }
            println("2 isUser ${result} ")
            return result
        }


    }

    fun startServer(){
        ClientExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

        ServerExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
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
                        val client = Client(socket)
                        addClient(client)
                        ClientServiceSubmit(client)
                        clientInfo = "[연결 수락 :  ${socket.getRemoteSocketAddress()} : ${Thread.currentThread().getName()}]"
                        println(""" ==========================
                        |           connected check!!
                        |    ${clientInfo} / [연결 개수 : ${connections.size} / 접속자: ${userCount()}]
                        | ========================== """.trimMargin())

                        //접속자 알림
//                        connections.forEach { client ->
//
//
//                            val msg = JSONObject().sendParse("${socket.getRemoteSocketAddress()} : ${
//                                Thread.currentThread().getName()
//                            } 접속 하였습니다.")
//
//                            println(" connection user:${msg}")
//                            client.send(msg)
//                        }

                    }catch(e: Exception){
                        if(!serverSocket.isClosed){
                            stopServer()
                            break@RUN
                        }
                    }
                }

            }
        }
        ServerServiceSubmit(runnable) // 스레드 풀에서 처리

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

            ServerExecutorService?.let{
                if(!it.isShutdown) it.shutdown()
            }

            ClientExecutorService?.let{
                if(!it.isShutdown) it.shutdown()
            }

        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}


class Client(val socket: Socket) :Runnable{

    private var userName: String? = null
    private var profile: String? = null
    private var email: String? = null
    private var gender:String? = null

//    init{
//        recevie()
//    }
//    fun recevie(){

//        val runnable = object: Runnable {

            override fun run() {
                val connectInfo = "${socket.getRemoteSocketAddress()}"
                try{
                    socket?.getInputStream()?.bufferedReader(Charsets.UTF_8)?.forEachLine { it ->

                        var message: String = it

                        val userObj = JSONObject(it)
                        val isDisconnect = userObj.get(MESSAGE_DATA).toString()

                        if(isDisconnect == "QUIT"){
//                            message = "${socket.getRemoteSocketAddress()}  : ${Thread.currentThread().getName()} 접속종료 하였습니다."
                            message = JSONObject().apply {
                                put(MESSAGE_DATA,"${connectInfo} 접속종료 하였습니다.")
                            }.toString()
                        }


                        ChatServer.connections.forEach { client ->
                            var sendFlag = true
                            when(socket.getRemoteSocketAddress())
                            {
                                //접속자 당사자용 메세지
                                client.socket.remoteSocketAddress -> {
                                    userName = userObj.get(NICKNAME_KEY).toString()
                                    val chkFlag = userObj.get(NICKNAME_CHK).toString()

                                    println("USER info = ${userName} , chkFlag =${chkFlag}}")

                                    when{

                                        "N".equals(chkFlag) && isUser(connectInfo,userName!!) -> {
                                            message = JSONObject().apply {
                                                put(MESSAGE_DATA,"이미 사용중인 닉내임 입니다.")
                                                put(NICKNAME_CHK,"N")
                                            }.toString()
                                        }

                                        "QUIT".equals(isDisconnect) -> {
                                            message = JSONObject().apply {
                                                put(MESSAGE_DATA,"CLOSED")
                                                put(NICKNAME_CHK,"Y")
                                            }.toString()
                                        }
                                        "Y".equals(chkFlag) -> {
                                            sendFlag = false
                                        }


                                        else -> {
                                            message = userObj.apply {
                                                put(NICKNAME_CHK,"Y")
                                            }.toString()
                                        }
                                    }

//                                    if(!"Y".equals(chkFlag) && isUser(user)){
//                                        println("USER Name Sender ")
//                                        message = JSONObject().apply {
//                                            put(MESSAGE_KEY,"이미 사용중인 닉내임 입니다.")
//                                            put(NICKNAME_KEY,"N")
//                                        }.toString()
//                                    }
                                }

                            }

                            println(message)
                            if(sendFlag) {
                                client.send(message,isDisconnect)
                                sendFlag = true
                            }

                        }


                    }

                }catch(e: Exception){
                    println("""
                     | [recevie 클라이언트 통신 안됨: ${socket.getRemoteSocketAddress()}  : ${Thread.currentThread().getName()} / 접속자수: ${userCount()}  ]"
                    """.trimIndent())

//                    try {
//                        ChatServer.removeClient(this@Client)
//                        ChatServer.removeUser(connectInfo)
//                        socket.close()
//                    }catch(io: IOException){
//                        io.printStackTrace()
//                    }
                }
//            }
//        }

    }

    fun send(data: String,isDisconnect: String){

//        val runnable = object: Runnable{
            val connectInfo = "${socket.getRemoteSocketAddress()}"
//            override fun run() {
                try{
                    socket.outputStream?.let{
//                        it.write( (jsonString + "\n").toByteArray(Charsets.UTF_8) )
                        it.write( (data + "\n").toByteArray(Charsets.UTF_8) )
                        it.flush()
                    }

                    if(isDisconnect  == "QUIT"){
//                                throw IOException("close client")
                        ChatServer.removeClient(this@Client)
                        ChatServer.removeUser(connectInfo)
                        socket.close()
                    }

                }catch (e: Exception){
                    println("""
                     | [send 클라이언트 통신 안됨: ${socket.getRemoteSocketAddress()}  : ${Thread.currentThread().getName()} " / 접속자수 ${userCount()}]"
                    """.trimIndent())

//                    try {
//                        ChatServer.removeClient(this@Client)
//                        socket.close()
//                    }catch(io: IOException){
//                        io.printStackTrace()
//                    }
                }
//            }
//        }
//        ChatServer.ClientServiceSubmit(runnable)
    }




    companion object{
        const val MESSAGE_DATA = "msgData"
        const val MESSAGE_ID = "msgId"
        const val NICKNAME_KEY = "nickname_key"
        const val NICKNAME_CHK = "nickname_chk"
    }
}

