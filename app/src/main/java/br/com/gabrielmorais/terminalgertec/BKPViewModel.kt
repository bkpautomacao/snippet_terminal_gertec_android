package br.com.gabrielmorais.terminalgertec

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update

class BKPViewModel : ViewModel() {

    private val _product = MutableStateFlow<Produto?>(null)
    val product = _product.asSharedFlow()

    private val _isConnected = MutableStateFlow<Boolean>(false)
    val isConnected = _isConnected.asSharedFlow()


    private var apiSC501: ApiSC501

    init {
        apiSC501 = ApiSC501(
            address = "192.168.0.101",
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
            message == ApiSC501.LIVE -> apiSC501.send("live\u0000")
            message.startsWith(ApiSC501.PRODUCT_NOT_FOUNDED) -> {
                Log.i("MainViewModel", "Gertec: Produto não encontrado")
            }

//            message.startsWith(ApiSC501.UPDATE_CONFIG) -> {
//                val ip = apiSC501.ip()
//                var m =
//                    "#updconfig;192.168.0.1;Sem Suporte5EmuTC;Sem Suporte;Sem Suporte;Sem Suporte\u0000"
//                Log.i("MainViewModel", "Gertec: Mensagem enviada $m ")
////                apiGertec.send(m)
//                m = "#macaddr"
//            }

            message.matches(ApiSC501.productPattern) -> {
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

    fun sendMessage(message: String) = apiSC501.send("$message\u0000")

    override fun onCleared() {
        super.onCleared()
        apiSC501.close()
    }

}