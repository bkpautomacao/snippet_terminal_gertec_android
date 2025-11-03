package br.com.gabrielmorais.terminalgertec

import android.util.Log
import androidx.lifecycle.ViewModel
import br.com.gabrielmorais.terminalgertec.http.source.repository.AppConfigRepository
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.util.Locale

class MainViewModel(
  private val usbSerial: UsbSerial,
  private val repository: AppConfigRepository
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
  private lateinit var apiQWChecker: ApiQWChecker

  fun startSerialScanner() {
    try {
      val driver = usbSerial.getAvailableDevice()
      usbSerialPort = usbSerial.openPortConnection(driver)
      val usbIoManager = SerialInputOutputManager(usbSerialPort, this)
      usbIoManager.start()
      Log.i("MainViewModel", "startSerialScanner: Iniciando scanner")
    } catch (e: Exception) {
      showMessage("Ocorreu um erro ao iniciar o scanner: ${e.message}")
    }
  }

  fun showMessage(text: String) {
    if (text.isEmpty()) {
      _message.update { text }
    }
  }

  fun connect(ip: String) {
    apiQWChecker = ApiQWChecker(
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
    apiQWChecker.connect()
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
          val wordsList = clean.split(" ")
          val priceIndex = wordsList.size - 1
          val price = wordsList[priceIndex]

          val description = wordsList
            .subList(0, priceIndex)
            .joinToString { " " }

          val priceFormatted = formatPortugueseCurrency(price)
          val produto = Produto(
            price = priceFormatted,
            description = description
          )
          _product.update { produto }
          _linhaProduto.update { clean }
        } else {
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
    usbSerial.closePortConnection(usbSerialPort)
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