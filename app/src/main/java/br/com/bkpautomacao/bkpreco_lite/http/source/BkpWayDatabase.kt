package br.com.bkpautomacao.bkpreco_lite.http.source

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.bkpautomacao.bkpreco_lite.http.source.dao.AppConfigDao
import br.com.bkpautomacao.bkpreco_lite.http.source.entities.AppConfig

@Database(entities = [AppConfig::class], version = 1)
abstract class BkpWayDatabase : RoomDatabase() {
  abstract fun getAppConfigDao(): AppConfigDao
}