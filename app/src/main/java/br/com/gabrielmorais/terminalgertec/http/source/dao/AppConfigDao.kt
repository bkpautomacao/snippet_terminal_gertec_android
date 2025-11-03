package br.com.gabrielmorais.terminalgertec.http.source.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import br.com.gabrielmorais.terminalgertec.http.source.entities.AppConfig

@Dao
interface AppConfigDao {
  @Upsert
  fun saveConfig(appConfig: AppConfig)

  @Query("SELECT * FROM configuracoes WHERE id = :id")
  fun getConfig(id: Int): AppConfig
}