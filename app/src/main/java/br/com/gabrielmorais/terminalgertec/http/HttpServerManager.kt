package br.com.gabrielmorais.terminalgertec.http

import android.content.Context
import br.com.gabrielmorais.terminalgertec.http.routes.configServer
import br.com.gabrielmorais.terminalgertec.http.routes.mainPage
import br.com.gabrielmorais.terminalgertec.http.routes.restartApplication
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

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  fun start(context: Context) {
    scope.launch {
      server = embeddedServer(Netty, port = 9999) {
        install(ContentNegotiation) {
          gson {
            setDateFormat(DateFormat.LONG, DateFormat.SHORT)
            setPrettyPrinting()
          }
        }

        routing {
          mainPage(context)
          configServer()
          restartApplication(context)
        }
      }.start(wait = true)
    }
  }

  fun stop() {
    server?.stop(1000, 2000)
    scope.cancel()
    server = null
  }
}