package com.example.coinswap.domain.model

data class VolumeUnit(
    val code: String,
    val name: String,
    val conversionFactor: Double // Conversion factor to liters
)