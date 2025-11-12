package br.com.bkpautomacao.bkpreco_lite.ui

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.com.bkpautomacao.bkpreco_lite.MainViewModel
import br.com.bkpautomacao.bkpreco_lite.R
import br.com.bkpautomacao.bkpreco_lite.UiUtils
import br.com.bkpautomacao.bkpreco_lite.admin.policy.DevicePolicyManagerHelper
import br.com.bkpautomacao.bkpreco_lite.http.HttpServerManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
  private lateinit var tts: TextToSpeech
  private val viewModel by viewModel<MainViewModel>()
  private val ACTION_USB_PERMISSION = "br.com.gabrielmorais.terminalgertec.USB_PERMISSION"
  private val USB_PERMISSION_REQUEST_CODE = 562
  private val devicePolicy: DevicePolicyManagerHelper by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    configureTTS()
    UiUtils.hideSystemUi(window)
    devicePolicy.enableLockTask(this)
    val filter = IntentFilter(ACTION_USB_PERMISSION)
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
      registerReceiver(usbReceiver, filter)
    } else {
      registerReceiver(usbReceiver, filter, RECEIVER_EXPORTED)
    }
    viewModel.connect()
    requestUsbPermission()
    handleObserver()
  }

  override fun onResume() {
    super.onResume()
    HttpServerManager.start(this)
  }

  override fun onPause() {
    super.onPause()
    HttpServerManager.stop()
  }

  private fun configureTTS() {
    tts = TextToSpeech(applicationContext) { result ->
      if (result == TextToSpeech.SUCCESS) {
        val language = tts.setLanguage(Locale("pt", "BR"))
        if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {
          Log.i("MainActivity", "configureAdapter: Linguagem não suportada")
        }
      }
    }
  }

  private fun requestUsbPermission() {
    val usbManager = getSystemService(USB_SERVICE) as UsbManager
    val devices = usbManager.deviceList.values
    if (devices.isEmpty()) {
      Log.i("MainActivity", "requestUsbPermission: Nenhum dispositivo usb encontrado")
      return
    }

    devices.forEach { device ->
      Log.i("MainActivity", "requestUsbPermission: ${device.productName}")
    }

    val device = try {
      viewModel.findDevice(devices)
    } catch (e: Exception) {
      Toast.makeText(
        this,
        e.message ?: "ocorreu um erro",
        Toast.LENGTH_SHORT
      ).show()
      return
    }

    val permissionIntent = PendingIntent.getBroadcast(
      this,
      USB_PERMISSION_REQUEST_CODE,
      Intent(ACTION_USB_PERMISSION),
      PendingIntent.FLAG_IMMUTABLE
    )

    if (!usbManager.hasPermission(device)) {
      usbManager.requestPermission(device, permissionIntent)
      return
    }

    try {
      viewModel.startSerialScanner()
    } catch (e: Exception) {
      e.printStackTrace()
    }

    Log.d("MainActivity", "Permissão já concedida ao dispositivo: ${device.deviceName}")
  }

  private val usbReceiver = object : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(context: Context, intent: Intent) {
      when (intent.action) {
        ACTION_USB_PERMISSION -> {
          val device = intent.getParcelableExtra(
            UsbManager.EXTRA_DEVICE,
            UsbDevice::class.java
          )
          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            Log.d(
              "MainActivity",
              "Permissão concedida para o dispositivo: ${device?.productName}"
            )
            viewModel.startSerialScanner()
          } else {
            Log.d("MainActivity", "Permissão negada para o dispositivo USB")
          }
        }
      }
    }
  }

  private fun terminateApp() {
    Log.d("MainActivity", "Terminating app")
    finishActivity(0)
    exitProcess(0)
  }

  private fun handleObserver() {

    viewModel.exitApp.onEach { isExit ->
      if (isExit) {
        devicePolicy.stopLockTask(this)
        terminateApp()
      }
    }.launchIn(lifecycleScope)

    viewModel.message.onEach { text ->
      if (text != null && text.isNotBlank()) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT)
          .show()
      }
    }.launchIn(lifecycleScope)

    viewModel.isConnected.onEach { isConnected ->
      val tvStatus = findViewById<ImageView>(R.id.status)
      if (isConnected) {
        Log.i("MainActivity", "Terminal Status: Conectado")
        tvStatus.setImageResource(R.drawable.icon_connected)
      } else {
        Log.i("MainActivity", "Terminal Status: Desconectado")
        tvStatus.setImageResource(R.drawable.icon_disconnected)
      }
    }.launchIn(lifecycleScope)

    viewModel.terminalMessages.onEach {
      val edtProduto = findViewById<TextView>(R.id.tvProduto)
      val edtTerminalMessages = findViewById<TextView>(R.id.tv_terminal_messages)
      edtProduto.text = ""
      edtTerminalMessages.text = it
    }.launchIn(lifecycleScope)

    viewModel.product.onEach { product ->
      if (product != null) {
        val edtProduto = findViewById<TextView>(R.id.tvProduto)
        val edtTerminalMessages = findViewById<TextView>(R.id.tv_terminal_messages)
        edtTerminalMessages.text = ""
        edtProduto.text = "${product.description}\n${product.price}"
        val params = Bundle()
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 0.9F)
        tts.setSpeechRate(1.3F)
        tts.speak(product.price, TextToSpeech.QUEUE_ADD, params, null)
      }
    }.launchIn(lifecycleScope)

  }
}