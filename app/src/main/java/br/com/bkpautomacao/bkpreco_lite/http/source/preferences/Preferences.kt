package br.com.bkpautomacao.bkpreco_lite.http.source.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val FILENAME = "configuracoes"

class Preferences(context: Context) {

  companion object {
    const val IP_ADDRESS_KEY = "ip"
    const val PORT_KEY = "port"
  }

  private val prefs: SharedPreferences by lazy {
    context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
  }

  fun setTextConfig(key: String, value: String) {
    prefs.edit { putString(key, value) }
  }

  fun getTextConfig(key: String): String {
    return prefs.getString(key, null) ?: ""
  }

  fun setIntConfig(key: String, value: Int) {
    prefs.edit { putInt(key, value) }
  }

  fun getIntConfig(key: String): Int {
    return prefs.getInt(key, 1007)
  }
}