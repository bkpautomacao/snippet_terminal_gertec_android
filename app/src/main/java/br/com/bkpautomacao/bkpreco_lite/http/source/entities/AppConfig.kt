package br.com.bkpautomacao.bkpreco_lite.http.source.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracoes")
data class AppConfig(
  @PrimaryKey
  @ColumnInfo(name = "id")
  val id: Int,
  @ColumnInfo(name = "value")
  val value: String,
)