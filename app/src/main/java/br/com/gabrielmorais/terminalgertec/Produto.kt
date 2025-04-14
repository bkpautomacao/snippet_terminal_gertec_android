package br.com.gabrielmorais.terminalgertec

data class Produto(
    val ean: String = "",
    val price: String,
    val pricePromotional: String = "",
    val description: String
)
