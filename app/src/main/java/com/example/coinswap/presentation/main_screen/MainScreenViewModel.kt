package com.example.coinswap.presentation.main_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinswap.domain.model.CurrencyRate
import com.example.coinswap.domain.model.LengthUnit
import com.example.coinswap.domain.model.Resource
import com.example.coinswap.domain.model.VolumeUnit
import com.example.coinswap.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val repository: CurrencyRepository
): ViewModel() {

    var state by mutableStateOf(MainScreenState())

    init {
        getCurrencyRatesList()
        initializeLengthUnits()
        initializeVolumeUnits()
    }

    fun onEvent(event: MainScreenEvent) {
        when(event) {
            MainScreenEvent.FromCurrencySelect -> {
                state = state.copy(selection = SelectionState.FROM)
            }
            MainScreenEvent.ToCurrencySelect -> {
                state = state.copy(selection = SelectionState.TO)
            }
            MainScreenEvent.SwapIconClicked -> {
                state = state.copy(
                    fromCurrencyCode = state.toCurrencyCode,
                    fromCurrencyValue = state.toCurrencyValue,
                    toCurrencyCode = state.fromCurrencyCode,
                    toCurrencyValue = state.fromCurrencyValue
                )
            }
            is MainScreenEvent.NumberButtonClicked -> {
                updateValue(value = event.value)
            }
            is MainScreenEvent.BottomSheetItemClicked -> {
                if (state.selection == SelectionState.FROM) {
                    state = state.copy(fromCurrencyCode = event.value)
                } else if (state.selection == SelectionState.TO) {
                    state = state.copy(toCurrencyCode = event.value)
                }
                updateValue("")
            }
            MainScreenEvent.LengthModeSelected -> {
                state = state.copy(
                    mode = ConverterMode.LENGTH,
                    fromCurrencyCode = state.defaultLengthFrom,
                    toCurrencyCode = state.defaultLengthTo,
                    fromCurrencyValue = "0.00",
                    toCurrencyValue = "0.00"
                )
            }
            MainScreenEvent.VolumeModeSelected -> {
                state = state.copy(
                    mode = ConverterMode.VOLUME,
                    fromCurrencyCode = state.defaultVolumeFrom,
                    toCurrencyCode = state.defaultVolumeTo,
                    fromCurrencyValue = "0.00",
                    toCurrencyValue = "0.00"
                )
            }
            MainScreenEvent.CurrencyModeSelected -> {
                state = state.copy(
                    mode = ConverterMode.CURRENCY,
                    fromCurrencyCode = state.defaultCurrencyFrom,
                    toCurrencyCode = state.defaultCurrencyTo,
                    fromCurrencyValue = "0.00",
                    toCurrencyValue = "0.00"
                )
            }
        }
    }

    private fun getCurrencyRatesList() {
        viewModelScope.launch {
            repository
                .getCurrencyRatesList()
                .collectLatest { result ->
                    state = when(result) {
                        is Resource.Success -> {
                            state.copy(
                                currencyRates = result.data?.associateBy { it.code } ?: emptyMap(),
                                error = null
                            )
                        }

                        is Resource.Error -> {
                            state.copy(
                                currencyRates = result.data?.associateBy { it.code } ?: emptyMap(),
                                error = result.message
                            )
                        }
                    }
                }
        }
    }

    private fun initializeLengthUnits() {
        val lengthUnits = listOf(
            LengthUnit("METER", "Meter", 1.0),
            LengthUnit("KM", "Kilometer", 1000.0),
            LengthUnit("CM", "Centimeter", 0.01),
            LengthUnit("MM", "Millimeter", 0.001),
            LengthUnit("MILE", "Mile", 1609.34),
            LengthUnit("YARD", "Yard", 0.9144),
            LengthUnit("FOOT", "Foot", 0.3048),
            LengthUnit("INCH", "Inch", 0.0254)
        )
        state = state.copy(lengthUnits = lengthUnits)
    }

    private fun initializeVolumeUnits() {
        val volumeUnits = listOf(
            VolumeUnit("LITER", "Liter", 1.0),
            VolumeUnit("ML", "Milliliter", 0.001),
            VolumeUnit("GALLON", "Gallon", 3.78541),
            VolumeUnit("QUART", "Quart", 0.946353),
            VolumeUnit("PINT", "Pint", 0.473176),
            VolumeUnit("CUP", "Cup", 0.24),
            VolumeUnit("OZ", "Ounce", 0.0295735)
        )
        state = state.copy(volumeUnits = volumeUnits)
    }

    private fun updateValue(value: String) {
        when (state.mode) {
            ConverterMode.CURRENCY -> updateCurrencyValue(value)
            ConverterMode.LENGTH -> updateLengthValue(value)
            ConverterMode.VOLUME -> updateVolumeValue(value)
        }
    }

    private fun updateCurrencyValue(value: String) {
        val currentCurrencyValue = when(state.selection) {
            SelectionState.FROM -> state.fromCurrencyValue
            SelectionState.TO -> state.toCurrencyValue
        }
        val fromCurrencyRate = state.currencyRates[state.fromCurrencyCode]?.rate ?: 0.0
        val toCurrencyRate = state.currencyRates[state.toCurrencyCode]?.rate ?: 0.0

        val updatedCurrencyValue = when(value) {
            "C" -> "0.00"
            else -> if (currentCurrencyValue == "0.00") value else currentCurrencyValue + value
        }

        val numberFormat = DecimalFormat("#.00")

        when(state.selection) {
            SelectionState.FROM -> {
                val fromValue = updatedCurrencyValue.toDoubleOrNull() ?: 0.0
                val toValue = fromValue / fromCurrencyRate * toCurrencyRate
                state = state.copy(
                    fromCurrencyValue = updatedCurrencyValue,
                    toCurrencyValue = numberFormat.format(toValue)
                )
            }
            SelectionState.TO -> {
                val toValue = updatedCurrencyValue.toDoubleOrNull() ?: 0.0
                val fromValue = toValue / toCurrencyRate * fromCurrencyRate
                state = state.copy(
                    toCurrencyValue = updatedCurrencyValue,
                    fromCurrencyValue = numberFormat.format(fromValue)
                )
            }
        }
    }

    private fun updateLengthValue(value: String) {
        val currentLengthValue = when(state.selection) {
            SelectionState.FROM -> state.fromCurrencyValue
            SelectionState.TO -> state.toCurrencyValue
        }
        val fromLengthUnit = state.lengthUnits.find { it.code == state.fromCurrencyCode }?.conversionFactor ?: 1.0
        val toLengthUnit = state.lengthUnits.find { it.code == state.toCurrencyCode }?.conversionFactor ?: 1.0

        val updatedLengthValue = when(value) {
            "C" -> "0.00"
            else -> if (currentLengthValue == "0.00") value else currentLengthValue + value
        }

        val numberFormat = DecimalFormat("#.00")

        when(state.selection) {
            SelectionState.FROM -> {
                val fromValue = updatedLengthValue.toDoubleOrNull() ?: 0.0
                val toValue = fromValue * fromLengthUnit / toLengthUnit
                state = state.copy(
                    fromCurrencyValue = updatedLengthValue,
                    toCurrencyValue = numberFormat.format(toValue)
                )
            }
            SelectionState.TO -> {
                val toValue = updatedLengthValue.toDoubleOrNull() ?: 0.0
                val fromValue = toValue * toLengthUnit / fromLengthUnit
                state = state.copy(
                    toCurrencyValue = updatedLengthValue,
                    fromCurrencyValue = numberFormat.format(fromValue)
                )
            }
        }
    }

    private fun updateVolumeValue(value: String) {
        val currentVolumeValue = when(state.selection) {
            SelectionState.FROM -> state.fromCurrencyValue
            SelectionState.TO -> state.toCurrencyValue
        }
        val fromVolumeUnit = state.volumeUnits.find { it.code == state.fromCurrencyCode }?.conversionFactor ?: 1.0
        val toVolumeUnit = state.volumeUnits.find { it.code == state.toCurrencyCode }?.conversionFactor ?: 1.0

        val updatedVolumeValue = when(value) {
            "C" -> "0.00"
            else -> if (currentVolumeValue == "0.00") value else currentVolumeValue + value
        }

        val numberFormat = DecimalFormat("#.00")

        when(state.selection) {
            SelectionState.FROM -> {
                val fromValue = updatedVolumeValue.toDoubleOrNull() ?: 0.0
                val toValue = fromValue * fromVolumeUnit / toVolumeUnit
                state = state.copy(
                    fromCurrencyValue = updatedVolumeValue,
                    toCurrencyValue = numberFormat.format(toValue)
                )
            }
            SelectionState.TO -> {
                val toValue = updatedVolumeValue.toDoubleOrNull() ?: 0.0
                val fromValue = toValue * toVolumeUnit / fromVolumeUnit
                state = state.copy(
                    toCurrencyValue = updatedVolumeValue,
                    fromCurrencyValue = numberFormat.format(fromValue)
                )
            }
        }
    }
}