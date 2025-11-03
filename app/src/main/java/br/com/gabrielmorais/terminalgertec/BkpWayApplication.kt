package br.com.gabrielmorais.terminalgertec

import android.app.Application
import android.util.Log
import br.com.gabrielmorais.terminalgertec.di.mainModule
import br.com.gabrielmorais.terminalgertec.di.viewModelModule
import br.com.gabrielmorais.terminalgertec.http.routes.ServerConfig
import br.com.gabrielmorais.terminalgertec.http.routes.configServer
import br.com.gabrielmorais.terminalgertec.http.routes.loadHtmlFromAssets
import br.com.gabrielmorais.terminalgertec.http.routes.mainPage
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.contentType
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.text.DateFormat

class BkpWayApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger(Level.DEBUG)
      androidContext(this@BkpWayApplication)
      modules(mainModule, viewModelModule)
    }

    CoroutineScope(Dispatchers.IO).launch {
      embeddedServer(Netty, port = 9999) {
        install(ContentNegotiation) {
          gson {
            setDateFormat(DateFormat.LONG, DateFormat.SHORT)
            setPrettyPrinting()
          }
        }
        routing {
          mainPage(this@BkpWayApplication)
          configServer()
        }
      }.start(wait = true)
    }
  }
}