package br.com.bkpautomacao.bkpreco_lite.http.source.repository

import br.com.bkpautomacao.bkpreco_lite.http.source.BkpWayDatabase
import br.com.bkpautomacao.bkpreco_lite.http.source.entities.AppConfig

class AppConfigRepository(
  val database: BkpWayDatabase
) {
  companion object {
    const val CONFIG_IP = 1
    const val CONFIG_PORT = 2
  }

  suspend fun saveConfig(config: AppConfig) = try {
    database.getAppConfigDao().saveConfig(config)
  } catch (e: Exception) {
    throw e
  }

  suspend fun getConfig(id: Int): AppConfig = try {
    database.getAppConfigDao().getConfig(id)
  } catch (e: Exception) {
    throw e
  }
}