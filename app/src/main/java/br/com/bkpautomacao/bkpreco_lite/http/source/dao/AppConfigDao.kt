package br.com.bkpautomacao.bkpreco_lite.http.source.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import br.com.bkpautomacao.bkpreco_lite.http.source.entities.AppConfig

@Dao
interface AppConfigDao {
  @Upsert
  suspend fun saveConfig(appConfig: AppConfig)

  @Query("SELECT * FROM configuracoes WHERE id = :id")
  suspend fun getConfig(id: Int): AppConfig
}