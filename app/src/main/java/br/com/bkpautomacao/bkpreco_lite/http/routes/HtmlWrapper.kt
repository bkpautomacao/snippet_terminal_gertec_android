package br.com.bkpautomacao.bkpreco_lite.http.routes

import android.content.Context

fun loadHtmlFromAssets(context: Context): String {
  return context.assets.open("index.html").use { inputStream ->
    inputStream.bufferedReader().use { reader ->
      reader.readText()
    }
  }
}