package br.com.gabrielmorais.terminalgertec.http.source.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracoes")
data class AppConfig(
  @PrimaryKey(autoGenerate = true)
  val id: Int,
  @ColumnInfo(name = "ip")
  val ip: String,
  @ColumnInfo(name = "porta")
  val port: Int
)