package br.com.gabrielmorais.terminalgertec

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
  private val viewModel by viewModel<MainViewModel>()
  private val ACTION_USB_PERMISSION = "br.com.gabrielmorais.terminalgertec.USB_PERMISSION"
  private val USB_PERMISSION_REQUEST_CODE = 562

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val filter = IntentFilter(ACTION_USB_PERMISSION)
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
      registerReceiver(usbReceiver, filter)
    } else {
      registerReceiver(usbReceiver, filter, RECEIVER_EXPORTED)
    }

    requestUsbPermission()
    val edtIp = findViewById<TextInputEditText>(R.id.edtIp)
    edtIp.setText("182.17.10.245")

    configureViews()
    handleObserver()
  }

  private fun requestUsbPermission() {
    val usbManager = getSystemService(USB_SERVICE) as UsbManager
    val devices = usbManager.deviceList.values
    if (devices.isEmpty()) {
      return
    }

    val device = devices.elementAt(0)

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

    viewModel.startSerialScanner()

    Log.d("MainActivity", "Permissão já concedida ao dispositivo: ${device?.deviceName}")
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
              "Permissão concedida para o dispositivo: ${device?.deviceName}"
            )
            viewModel.startSerialScanner()
          } else {
            Log.d("MainActivity", "Permissão negada para o dispositivo USB")
          }
        }
      }
    }
  }

  private fun handleObserver() {
    viewModel.message.onEach { text ->
      Toast.makeText(this, text, Toast.LENGTH_SHORT)
        .show()
    }.launchIn(lifecycleScope)
    viewModel.isConnected.onEach { isConnected ->
      val tvStatus = findViewById<TextView>(R.id.status)
      if (isConnected) {
        tvStatus.background = Color.GREEN.toDrawable()
      } else {
        tvStatus.background = Color.RED.toDrawable()
      }
    }.launchIn(lifecycleScope)

    viewModel.product.onEach { produto ->
      val tvDescription = findViewById<TextView>(R.id.tvDescription)
      val tvPrice = findViewById<TextView>(R.id.tvPrice)
      tvDescription.text = produto?.description
      tvPrice.text = "Preco: ${produto?.price} | Promoção: ${produto?.pricePromotional}"
    }.launchIn(lifecycleScope)

    viewModel.linhaProduto.onEach {
      val edtlinhaProduto = findViewById<TextView>(R.id.tvBarcode)
      edtlinhaProduto.text = it
    }.launchIn(lifecycleScope)

  }

  private fun configureViews() {
    val edtCodigo = findViewById<TextInputEditText>(R.id.edtCodigo)
    edtCodigo.setOnKeyListener { view, i, keyEvent ->
      if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {
        buscarProduto()
        return@setOnKeyListener true
      }
      return@setOnKeyListener false
    }

    val btnSearch = findViewById<Button>(R.id.buscaProduto)
    btnSearch.setOnClickListener {
      buscarProduto()
    }

    val btnConnect = findViewById<Button>(R.id.btnConnect)
    btnConnect.setOnClickListener {
      val edtIp = findViewById<TextInputEditText>(R.id.edtIp)
      val ip = edtIp.text.toString()
      viewModel.connect(ip)
    }

    val btnDisconnect = findViewById<Button>(R.id.btnDisconnect)
    btnDisconnect.setOnClickListener {
      viewModel.disconnect()
    }

  }

  private fun buscarProduto() {
    val edtCodigo = findViewById<TextInputEditText>(R.id.edtCodigo)
    val codigo = "2${edtCodigo.text}"
    viewModel.sendMessage(codigo)
    edtCodigo.setText("")
  }
}