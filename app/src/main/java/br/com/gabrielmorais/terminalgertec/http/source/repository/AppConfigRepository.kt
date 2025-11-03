package br.com.gabrielmorais.terminalgertec.http.source.repository

import br.com.gabrielmorais.terminalgertec.http.source.BkpWayDatabase
import br.com.gabrielmorais.terminalgertec.http.source.entities.AppConfig

class AppConfigRepository(
  val database: BkpWayDatabase
) {
  fun saveConfig(config: AppConfig) = try {
    database.getAppConfigDao().saveConfig(config)
  } catch (e: Exception) {
    throw e
  }

  fun getConfig(id: Int): AppConfig = try {
    database.getAppConfigDao().getConfig(id)
  } catch (e: Exception) {
    throw e
  }
}