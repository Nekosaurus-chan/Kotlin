package com.example.coinswap.domain.model

data class LengthUnit(
    val code: String,
    val name: String,
    val conversionFactor: Double // Conversion factor to meters
)