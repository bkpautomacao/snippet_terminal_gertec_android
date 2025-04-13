package br.com.gabrielmorais.terminalgertec

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainViewModel : ViewModel() {
    private val _product = MutableStateFlow<Produto?>(null)
    val product = _product.asSharedFlow()

    private val _isConnected = MutableStateFlow<Boolean>(false)
    val isConnected = _isConnected.asSharedFlow()

    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    init {
        connect()
    }

    fun connect() {
        viewModelScope.launch(Dispatchers.IO) {
            socket = Socket("192.168.0.101", 16510)
            inputStream = socket?.inputStream
            outputStream = socket?.outputStream
            startReceiving()
        }
    }

    fun sendData(id: Int, data: String) {
        val idType: Short = when (id) {
            1 -> 0x1001
            2 -> 0x1013
            3 -> 0x1014
            else -> {
                Log.i("MainViewModel", "Gertec: comando não reconhecido")
                0
            }
        }

        val cmd = montarComando(idType, byteArrayOf(0))
        Log.i("MainViewModel", "Gertec: Comando enviado: ${cmd.toList()}")
        outputStream?.write(cmd)
    }

    private fun montarComando(id: Short, argumento: ByteArray): ByteArray {
        val buffer = ByteBuffer.allocate(1 + 2 + 4 + argumento.size)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        buffer.put(0x02)                       // STX
        buffer.putShort(id)                    // ID do comando
        buffer.putInt(argumento.size)          // Tamanho do argumento
        buffer.put(argumento)                  // Argumentos (se houver)

        return buffer.array()
    }

    private fun startReceiving() {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            while (true) {
                val buffer = ByteArray(1024)
                val result = inputStream?.read(buffer)
                val byteBuffer = ByteBuffer.allocate(result!!)
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                val rawData = buffer.filter { it > 0 }.toByteArray()
                byteBuffer.put(rawData)
                Log.i("MainViewModel", "Gertec: Comando recebido: ${byteBuffer.array().toList()}")
                if (buffer.contains(2)) {
                    sendData(3, "")
                }
            }
        }
    }

//    private val apiSC504 = ApiSC504(
//        address = "192.168.0.101",
//        onDisconnected = {},
//        onConnected = {},
//        onMessageReceived = ::handleGertecMessage,
//    )
//    init {
//        apiSC504.connect()
//    }
//
//    fun sendMessage(message: Short) = apiSC504.send(message)

//    private fun handleGertecMessage(message: String) {
//
//        when {
////            message == ApiSC504.OK -> apiSC504.send("#tc406|4.0\u0000")
////            message == ApiSC504.LIVE -> apiSC504.send("live\u0000")
//            message.startsWith("\u0002") -> {
//                apiSC504.send(ID_KEEP_ALIVE)
//            }
//
//            message.startsWith(ApiSC504.PRODUCT_NOT_FOUNDED) -> {
//                Log.i("MainViewModel", "Gertec: Produto não encontrado")
//            }
//
//            message.startsWith(ApiSC504.UPDATE_CONFIG) -> {
//                val ip = apiSC504.ip()
//                var m =
//                    "#updconfig192.168.0.1Sem Suporte5EmuTCSem SuporteSem SuporteSem Suporte\u0000"
//                Log.i("MainViewModel", "Gertec: Mensagem enviada $m ")
////                apiGertec.send(m)
//                m = "#macaddr"
//            }
//
//            message.matches(ApiSC504.productPattern) -> {
//                val produtoString = apiSC504.propertiesList(message)
//                val produto = Produto(
//                    price = produtoString[0],
//                    description = produtoString[1]
//                )
//                _product.update { produto }
//            }
//
//            else -> Log.i("MainViewModel", "Gertec: Comando ignorado: $message")
//        }
//    }
}