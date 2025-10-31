package br.com.gabrielmorais.terminalgertec

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.Socket
import java.nio.charset.StandardCharsets

class ApiSC501(
    private val address: String,
    private val port: Int = 1007,
    private val onMessageReceived: (String) -> Unit,
    private val onConnected: () -> Unit,
    private val onDisconnected: () -> Unit
) {

    private var socket: Socket? = null
    private var scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TIMEOUT_CONNECTION = 5000
    private val DEFAULT_DELAY = 300L
    private val BUFFER_SIZE = 1024

    fun connect() {
        scope.launch {
            try {
                socket = Socket(address, port).apply { soTimeout = TIMEOUT_CONNECTION }
                onConnected()
                startReceiving()
            } catch (_: Exception) {
                reconnect()
            }
        }
    }

    fun send(message: String) {
        scope.launch {
            try {
                Log.i("ApiGertec", "Gertec Mensagem Enviada: $message")
                socket?.outputStream?.write(message.toByteArray())
            } catch (_: Exception) {
                reconnect()
            }
        }
    }

    private suspend fun reconnect() {
        onDisconnected()
        delay(DEFAULT_DELAY)
        socket = null
        connect()
    }

    private fun startReceiving() {
        scope.launch {
            try {
                while (true) {
                    delay(DEFAULT_DELAY)
                    val buffer = ByteArray(BUFFER_SIZE)
                    val result = socket?.inputStream?.read(buffer) ?: -1
                    if (result < 0) throw IOException("Conexão perdida: leitura retornou $result")
                    val cleanMessage = buffer.filter { it > 0 }.toByteArray()
                    val message = String(cleanMessage, StandardCharsets.ISO_8859_1)
                    Log.i("ApiGertec", "Gertec: Bytes Recebidos: $result")
                    Log.i("ApiGertec", "Gertec: Raw Message - ${cleanMessage.toList()}")
                    Log.i("ApiGertec", "Gertec: Mensagem recebida - $message")
                    onMessageReceived(message)
                }
            } catch (_: Exception) {
                reconnect()
            }
        }
    }

    fun close() {
        scope.cancel()
        socket?.close()
        socket = null
    }
}