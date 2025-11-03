package br.com.gabrielmorais.terminalgertec.http.routes

import android.content.Context

fun loadHtmlFromAssets(context: Context): String {
  return context.assets.open("index.html").use { inputStream ->
    inputStream.bufferedReader().use { reader ->
      reader.readText()
    }
  }
}