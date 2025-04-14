package br.com.gabrielmorais.terminalgertec

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {
    private val viewModel = MainViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureViews()

        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)

        viewModel.product.onEach { produto ->
            tvDescription.text = produto?.description
            tvPrice.text = "Preco: ${produto?.price} | Promoção: ${produto?.pricePromotional}"
        }.launchIn(lifecycleScope)

        viewModel.isConnected.onEach { isConnected ->
            val tvStatus = findViewById<TextView>(R.id.status)
            if (isConnected) {
                tvStatus.background = Color.GREEN.toDrawable()
            } else {
                tvStatus.background = Color.RED.toDrawable()
            }
        }.launchIn(lifecycleScope)

        viewModel.linhaProduto.onEach {
            val edtlinhaProduto = findViewById<TextView>(R.id.tvBarcode)
            edtlinhaProduto.text = it
        }.launchIn(lifecycleScope)

        val edtIp = findViewById<TextInputEditText>(R.id.edtIp)
        edtIp.setText("182.17.10.149")
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
        val codigo = "#${edtCodigo.text}\u0000"
        viewModel.sendMessage(codigo)
        edtCodigo.setText("")
    }
}