package br.com.bkpautomacao.bkpreco_lite.http

import android.content.Context
import android.util.Log
import br.com.bkpautomacao.bkpreco_lite.http.routes.configServer
import br.com.bkpautomacao.bkpreco_lite.http.routes.mainPage
import br.com.bkpautomacao.bkpreco_lite.http.routes.restartApplication
import br.com.bkpautomacao.bkpreco_lite.http.source.preferences.Preferences
import io.ktor.serialization.gson.gson
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.DateFormat

object HttpServerManager {
  private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
    null

  private var scope: CoroutineScope? = null

  fun start(context: Context) {
    scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    scope?.launch {
      try {
        Log.i("HttpServerManager", "start: Iniciando servidor!!!")
        server = embeddedServer(Netty, port = 9999) {
          install(ContentNegotiation) {
            gson {
              setDateFormat(DateFormat.LONG, DateFormat.SHORT)
              setPrettyPrinting()
            }
          }

          routing {

            val prefs = Preferences(context)
            mainPage(context)
            configServer(prefs = prefs)
            restartApplication(context)

          }
        }.start(wait = true)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun stop() {
    Log.i("HttpServerManager", "Encerrando servidor!!!")
    server?.stop(500, 1000)
    scope?.cancel()
    server = null
    scope = null
  }
}