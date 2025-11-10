package br.com.bkpautomacao.bkpreco_lite

data class Produto(
    val ean: String = "",
    val price: String,
    val pricePromotional: String = "",
    val description: String
)
