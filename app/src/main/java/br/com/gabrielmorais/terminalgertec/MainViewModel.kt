package br.com.gabrielmorais.terminalgertec

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.nio.charset.StandardCharsets

class MainViewModel : ViewModel() {

    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null

    private val _product = MutableStateFlow<Produto?>(null)
    val product = _product.asSharedFlow()


    fun connect() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                socket = Socket("192.168.0.101", 6500)
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
                startReceiving()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                socket?.getOutputStream()?.write(message.toByteArray())
//                writer?.apply {
//                    Log.i("MainViewModel", "Gertec: Enviando mensagem: $message")
//                    write(message)
//                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startReceiving() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(300)
                val buffer = ByteArray(16)
                socket?.getInputStream()?.read(buffer)
                val message = removeNonAlphaCharacters(String(buffer, StandardCharsets.UTF_8))
                Log.i("MainViewModel", "Gertec: Mensagem recebida - $message")
                when (message) {
                    "#ok" -> sendMessage("ok")
                    "#live?" -> sendMessage("live\u0000")
                    else -> {
                        val produtoString = message.removePrefix("#").split("|")
                        val produto = Produto(
                            price = produtoString[0],
                            description = produtoString[1]
                        )
                        _product.update { produto }
                        Log.i("MainViewModel", "Gertec: $message")
                        Log.i("MainViewModel", "Gertec: $produto")
                    }
                }
            }
        }
    }

    private fun removeNonAlphaCharacters(s: String): String {
        val regex = "[^a-zA-Z0-9.#?$|,]".toRegex()
        return s.replace(regex, "")
    }

    override fun onCleared() {
        super.onCleared()
        socket?.close()
    }

}