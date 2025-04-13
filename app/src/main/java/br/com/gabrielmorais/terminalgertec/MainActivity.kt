package br.com.gabrielmorais.terminalgertec

import android.graphics.Color
import android.os.Bundle
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
//        viewModel.connect()
        configureViews()
        val tvBarcode = findViewById<TextView>(R.id.tvBarcode)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)
        viewModel.product.onEach { produto ->
            tvBarcode.text = produto?.ean
            tvDescription.text = produto?.description
            tvPrice.text = produto?.price
        }.launchIn(lifecycleScope)

        viewModel.isConnected.onEach { isConnected ->
            val tvStatus = findViewById<TextView>(R.id.status)
            if (isConnected) {
                tvStatus.background = Color.GREEN.toDrawable()
            } else {
                tvStatus.background = Color.RED.toDrawable()
            }
        }.launchIn(lifecycleScope)
    }

    private fun configureViews() {
        val btnSearch = findViewById<Button>(R.id.buscaProduto)
        btnSearch.setOnClickListener {
            val edtCodigo = findViewById<TextInputEditText>(R.id.edtCodigo).text
            val
                    codigo = "#$edtCodigo"
//            viewModel.sendMessage(0x1003)
        }
    }
}