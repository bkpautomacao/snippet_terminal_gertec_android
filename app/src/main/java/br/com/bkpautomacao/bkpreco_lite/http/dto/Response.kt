package br.com.bkpautomacao.bkpreco_lite.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class Response(
  val status: String,
  val message: String
)
