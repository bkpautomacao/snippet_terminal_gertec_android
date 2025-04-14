package br.com.gabrielmorais.terminalgertec

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private val _product = MutableStateFlow<Produto?>(null)
    val product = _product.asSharedFlow()

    private val _isConnected = MutableStateFlow<Boolean>(false)
    val isConnected = _isConnected.asSharedFlow()


    private lateinit var apiSC501: ApiSC501

    fun connect(ip: String) {
        apiSC501 = ApiSC501(
            address = ip,
            onConnected = {
                _isConnected.update { true }
                Log.i("MainViewModel", "Gertec: Tentando reconectar")
            },
            onDisconnected = {
                _isConnected.update { false }
                Log.i("MainViewModel", "Gertec: Terminal desconectado")
            },
            onMessageReceived = ::handleGertecMessage
        )
        apiSC501.connect()
    }


    private fun handleGertecMessage(message: String) {

        when {
            message == ApiSC501.OK -> apiSC501.send("#tc406|4.0\u0000")
            message == ApiSC501.LIVE -> apiSC501.send("#live\u0000")
            message.startsWith(ApiSC501.PRODUCT_NOT_FOUNDED_SERVUNI) || message.startsWith(ApiSC501.PRODUCT_NOT_FOUNDED_TCSERVER) -> {
                val p = Produto(description = "Não encontrado", price = "0")
                _product.update { p }
                Log.i("MainViewModel", "Gertec: Produto não encontrado")
            }

            message.startsWith(ApiSC501.MACADDRESS) -> {
                val macResponse = "#macaddr+00:00:00:00:00:00"
                sendMessage(macResponse)
            }

            message.matches(ApiSC501.productPattern1) || message.matches(ApiSC501.productPattern2) -> {
                val produtoString = apiSC501.propertiesList(message)
                val produto = Produto(
                    price = produtoString[0],
                    description = produtoString[1]
                )
                _product.update { produto }
            }

            else -> Log.i("MainViewModel", "Gertec: Comando ignorado: $message")
        }
    }

    fun disconnect() {
        apiSC501.close()
    }

    fun sendMessage(message: String) = apiSC501.send(message)

    override fun onCleared() {
        super.onCleared()
        apiSC501.close()
    }

}