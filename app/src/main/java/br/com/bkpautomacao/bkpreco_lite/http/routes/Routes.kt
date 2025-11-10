package br.com.bkpautomacao.bkpreco_lite.http.routes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import br.com.bkpautomacao.bkpreco_lite.http.dto.Response
import br.com.bkpautomacao.bkpreco_lite.http.source.preferences.Preferences
import br.com.bkpautomacao.bkpreco_lite.ui.MainActivity
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.restartApplication(context: Context) {
  post("/restart") {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)

    if (context is Activity) {
      context.finish()
    }

    call.respond(Response("ok", "Aplicação reiniciada com sucesso"))
    Runtime.getRuntime().exit(0)
    Log.i("RestartApplication", "Reiniciando aplicação")

  }
}

fun Route.mainPage(context: Context) {
  get("/config") {
    val htmlContent = loadHtmlFromAssets(context)
    call.respondText(htmlContent, ContentType.Text.Html)
  }
}

fun Route.configServer(prefs: Preferences) {
  post("/config/server") {
    try {
      val config = call.receive<ServerConfig>()
      Log.i(
        "BKPWayApplication",
        "onCreate: IP: ${config.ip} | PORT: ${config.port}"
      )

      val ip = config.ip
      val port = config.port
      prefs.setTextConfig(Preferences.IP_ADDRESS_KEY, ip)
      prefs.setIntConfig(Preferences.PORT_KEY, port)
      call.respond(HttpStatusCode.OK, mapOf("success" to "OK"))
    } catch (e: Exception) {
      call.respond(
        HttpStatusCode.BadRequest,
        mapOf("error" to "${e.message}")
      )
    }
  }
}