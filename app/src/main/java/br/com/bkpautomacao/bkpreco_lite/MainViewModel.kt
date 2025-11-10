package br.com.bkpautomacao.bkpreco_lite

import android.util.Log
import androidx.lifecycle.ViewModel
import br.com.bkpautomacao.bkpreco_lite.http.source.preferences.Preferences
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.util.Locale

class MainViewModel(
  private val usbSerial: UsbSerial,
  private val prefs: Preferences
) : ViewModel(), SerialInputOutputManager.Listener {

  private val _product = MutableSharedFlow<Produto?>()
  val product = _product.asSharedFlow()
  private val _isConnected = MutableStateFlow(false)
  val isConnected = _isConnected.asSharedFlow()
  private val _message = MutableStateFlow<String?>(null)
  val message = _message.asStateFlow()
  private val _linhaProduto = MutableStateFlow("")
  val terminalMessages = _linhaProduto.asSharedFlow()
  private lateinit var usbSerialPort: UsbSerialPort
  private lateinit var apiQWChecker: ApiQWChecker

  fun startSerialScanner() {
    try {
      val driver = usbSerial.getAvailableDevice()
      usbSerialPort = usbSerial.openPortConnection(driver)
      val usbIoManager = SerialInputOutputManager(usbSerialPort, this)
      usbIoManager.start()
      Log.i("MainViewModel", "startSerialScanner: Iniciando scanner")
    } catch (e: Exception) {
      throw e
//      showMessage("Ocorreu um erro ao iniciar o scanner: ${e.message}")
    }
  }

  fun showMessage(text: String) {
    _message.update { text }
  }

  fun getIp(): String {
    return prefs.getTextConfig(Preferences.IP_ADDRESS_KEY)
  }

  fun getPort(): Int {
    return prefs.getIntConfig(Preferences.PORT_KEY)
  }

  fun connect() {
    val ip = getIp()
    if (ip.isEmpty()) {
      _linhaProduto.update { "Configure o ip do servidor" }
      return
    }
    val port = getPort()

    apiQWChecker = ApiQWChecker(
      address = ip,
      port = port,
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
    apiQWChecker.connect()
  }

  private suspend fun handleQuickWayMessage(message: String) {
    when {
      message.startsWith("L") -> {
        val result = message.removePrefix("L")
        val regex = Regex("""\b\d{1,3}(?:\.\d{3})*,\d{2}\b""")
        val match = regex.find(result)
        Log.i("MainViewModel", "handleQuickWayMessage: $result | Match: $match")
        if (match != null) {
          var clean = result.replace("\n", "")
          clean = clean.replace(Regex("\\s{2,}"), " ")
          val wordsList = clean.split(" ").filter { it.isNotBlank() }
          val priceIndex = wordsList.size - 1
          val price = wordsList[priceIndex]

          val description = wordsList
            .subList(0, priceIndex)
            .joinToString(separator = " ")

          val priceFormatted = formatPortugueseCurrency(price)
          val produto = Produto(
            price = priceFormatted,
            description = description
          )

          _product.emit(produto)

        } else {
          val cleaned = result.split("\n")
            .filter { it.isNotBlank() }
            .map { it.trim() }
          Log.i("MainViewModel", "handleQuickWayMessage: $cleaned")
          _linhaProduto.update { result }
        }

        apiQWChecker.send("1")
      }

      else -> apiQWChecker.send("1")
    }
  }

  fun formatPortugueseCurrency(value: String?): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formatter.format(value?.replace(",", ".")?.toDouble())
  }

  override fun onCleared() {
    super.onCleared()
    apiQWChecker.close()
    if (this::usbSerialPort.isInitialized) {
      usbSerial.closePortConnection(usbSerialPort)
    }
  }

  override fun onNewData(data: ByteArray?) {
    if (data != null) {
      val stringData = String(data, StandardCharsets.ISO_8859_1)
      val newData = removeNonAlphaCharacters("2$stringData")
      Log.i("MainViewModel", "onNewData: Buscando item: $newData")
      if (this::apiQWChecker.isInitialized) {
        apiQWChecker.send(newData)
      }
    }
  }

  fun removeNonAlphaCharacters(s: String): String {
    val regex = "[^a-zA-Z0-9.]".toRegex()
    return s.replace(regex, "")
  }

  override fun onRunError(e: Exception?) {
    Log.i("MainViewModel", "erro: ${e?.message}")
  }

}