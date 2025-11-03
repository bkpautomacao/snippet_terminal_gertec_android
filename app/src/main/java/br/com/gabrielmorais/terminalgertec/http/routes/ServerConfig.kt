package br.com.gabrielmorais.terminalgertec.http.routes

import kotlinx.serialization.Serializable


@Serializable
data class ServerConfig(
  val ip: String,
  val port: Int = 1007
)