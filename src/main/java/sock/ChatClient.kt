package sock

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.Socket



fun main(){
    ChatClient().startClient()
}

class ChatClient {
    companion object{
        const val MESSAGE_DATA = "msgData"
        const val MESSAGE_ID = "msgId"
        const val NICKNAME_KEY = "nickname_key"
        const val NICKNAME_CHK = "nickname_chk"
        const val QUIT:String = "QUIT"
        lateinit var socket:Socket
    }

    fun startClient(){

        var thread: Thread?  = object:Thread() {
            override fun run() {
                try{
                    val socketAddress = InetAddress.getLocalHost()
                    socket = Socket(socketAddress, 6789)
                }catch(e: Exception){
                    if (!socket.isClosed) {
                        stopClient()
                    }
                    return
                }
                receive()
            }
        }
        thread?.start()


        val br = BufferedReader(InputStreamReader(System.`in`))
//        val token = StringTokenizer(br.readLine())

        while (true) {
            val message = br.readLine()
            send(message)
            if(message.toUpperCase() == QUIT){
                br.close()
                break
            }
        }
        println(" CLOSE CALLED")

//        println(Integer.parseInt(token.nextToken()) + Integer.parseInt(token.nextToken()))
//        br.close()



//        val sc: Scanner = Scanner(System.`in`)
//        println("콘솔에 메세지를 입력하시면 채팅이 진행됩니다.")
//        while(sc.hasNext()){
//
//            val message = sc.nextLine()
//            send(message)
//            if(message.toUpperCase() == QUIT){
//                break
//            }
//        }
    }

    fun stopClient(){
        try{
            socket?.let{
                if(!it.isClosed) socket.close()
            }
        }catch(e: Exception){
            e.printStackTrace()
        }
    }

    fun receive(){
        while(true){
            try{
                socket?.getInputStream()?.bufferedReader(Charsets.UTF_8)?.forEachLine {
                    println(it)
                }
            }catch (e: Exception){
                stopClient()
                break
            }
        }
    }

    fun send(data: String){
        var jsondata = JSONObject().apply {
            put(NICKNAME_KEY, "nickNameKeyTEST")
            put(NICKNAME_CHK, "Y")
            put(MESSAGE_DATA,data)
        }
        Thread() {

                try{
                    socket.outputStream?.let{
                        it.write( (jsondata.toString() + "\n").toByteArray(Charsets.UTF_8) )
                        it.flush()
                    }
                    //연결 종료
                    if(data.toUpperCase() == QUIT){
                        stopClient()
                    }

                }catch(e: Exception){
                    if (!socket.isClosed) {
                        stopClient()
                    }
                }
        }.start()
    }



}


