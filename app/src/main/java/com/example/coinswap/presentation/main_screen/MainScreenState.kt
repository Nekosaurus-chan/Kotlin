package com.example.coinswap.presentation.main_screen

import com.example.coinswap.domain.model.CurrencyRate
import com.example.coinswap.domain.model.LengthUnit
import com.example.coinswap.domain.model.VolumeUnit

data class MainScreenState(
    val fromCurrencyCode: String = "EUR",
    val toCurrencyCode: String = "USD",
    val fromCurrencyValue: String = "0.00",
    val toCurrencyValue: String = "0.00",
    val selection: SelectionState = SelectionState.FROM,
    val currencyRates: Map<String, CurrencyRate> = emptyMap(),
    val lengthUnits: List<LengthUnit> = emptyList(),
    val volumeUnits: List<VolumeUnit> = emptyList(),
    val error: String? = null,
    val mode: ConverterMode = ConverterMode.CURRENCY,
    val defaultCurrencyFrom: String = "EUR",
    val defaultCurrencyTo: String = "USD",
    val defaultLengthFrom: String = "METER",
    val defaultLengthTo: String = "KM",
    val defaultVolumeFrom: String = "LITER",
    val defaultVolumeTo: String = "ML"
)

enum class SelectionState {
    FROM,
    TO
}

enum class ConverterMode {
    CURRENCY,
    LENGTH,
    VOLUME
}