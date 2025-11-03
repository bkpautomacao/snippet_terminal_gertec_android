package br.com.gabrielmorais.terminalgertec.http.source

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.gabrielmorais.terminalgertec.http.source.dao.AppConfigDao
import br.com.gabrielmorais.terminalgertec.http.source.entities.AppConfig

@Database(entities = [AppConfig::class], version = 1)
abstract class BkpWayDatabase : RoomDatabase() {
  abstract fun getAppConfigDao(): AppConfigDao
}