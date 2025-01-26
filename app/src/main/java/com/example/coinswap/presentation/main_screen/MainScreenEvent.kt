package com.example.coinswap.presentation.main_screen

sealed class MainScreenEvent {
    object FromCurrencySelect: MainScreenEvent()
    object ToCurrencySelect: MainScreenEvent()
    object SwapIconClicked: MainScreenEvent()
    data class BottomSheetItemClicked(val value: String): MainScreenEvent()
    data class NumberButtonClicked(val value: String): MainScreenEvent()
    object LengthModeSelected: MainScreenEvent()
    object VolumeModeSelected: MainScreenEvent()
    object CurrencyModeSelected: MainScreenEvent()
}
