package br.com.gabrielmorais.terminalgertec

import android.util.Log
import androidx.lifecycle.ViewModel
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.charset.StandardCharsets

class MainViewModel(
    private val usbSerial: UsbSerial
) : ViewModel(), SerialInputOutputManager.Listener {

    private val _product = MutableStateFlow<Produto?>(null)
    val product = _product.asSharedFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asSharedFlow()
    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    private val _linhaProduto = MutableStateFlow("")
    val linhaProduto = _linhaProduto.asSharedFlow()
    private lateinit var usbSerialPort: UsbSerialPort
    private lateinit var apiSC501: ApiSC501

    fun startSerialScanner() {
        try {
            val driver = usbSerial.getAvailableDevice()
            usbSerialPort = usbSerial.openPortConnection(driver)
            val usbIoManager = SerialInputOutputManager(usbSerialPort, this)
            usbIoManager.start()
            Log.i("MainViewModel", "startSerialScanner: Iniciando scanner")
        } catch (e: Exception) {
            _message.update { "Ocorreu um erro ao iniciar o scanner: ${e.message}" }
        }
    }

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
            onMessageReceived = ::handleQuickWayMessage
        )
        apiSC501.connect()
    }


    private fun handleQuickWayMessage(message: String) {
        when {
            message.startsWith("L") -> {
                val result = message.removePrefix("L")
                val regex = Regex("""\b\d{1,3}(?:\.\d{3})*,\d{2}\b""")
                val match = regex.find(result)
                if (match != null) {
                    var clean = result.replace("\n", "")
                    clean = clean.replace(Regex("\\s{2,}"), " ")
                    _linhaProduto.update { clean }
                } else {
                    _linhaProduto.update { result }
                }

                apiSC501.send("1")
            }

            else -> apiSC501.send("1")
        }
    }

    fun disconnect() {
        apiSC501.close()
    }

    fun sendMessage(message: String) = apiSC501.send(message)

    override fun onCleared() {
        super.onCleared()
        apiSC501.close()
        usbSerial.closePortConnection(usbSerialPort)
    }

    override fun onNewData(data: ByteArray?) {
        Log.i("MainViewModel", "onNewData: $data")
        if (data != null) {
            val stringData = String(data, StandardCharsets.ISO_8859_1)
            val newData = "2$stringData"
            Log.i("MainViewModel", "onNewData: Buscando item: $newData")
            if (this::apiSC501.isInitialized) {
                apiSC501.send(newData)
            }
        }
    }

    override fun onRunError(e: Exception?) {
        Log.i("MainViewModel", "erro: ${e?.message}")
    }

}