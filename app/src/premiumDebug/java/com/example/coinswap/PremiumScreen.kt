package com.example.coinswap

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.coinswap.R
import com.example.coinswap.domain.model.CurrencyRate
import com.example.coinswap.domain.model.LengthUnit
import com.example.coinswap.domain.model.VolumeUnit
import com.example.coinswap.presentation.main_screen.BottomSheetContent
import com.example.coinswap.presentation.main_screen.ConverterMode
import com.example.coinswap.presentation.main_screen.MainScreenEvent
import com.example.coinswap.presentation.main_screen.MainScreenState
import com.example.coinswap.presentation.main_screen.SelectionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumMainScreen(
    state: MainScreenState,
    onEvent: (MainScreenEvent) -> Unit
) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "C")

    val context = LocalContext.current

    LaunchedEffect(key1 = state.error) {
        if (state.error != null) {
            Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
        }
    }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var shouldBottomSheetShow by remember { mutableStateOf(false) }

    if (shouldBottomSheetShow) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { shouldBottomSheetShow = false },
            dragHandle = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BottomSheetDefaults.DragHandle()
                    Text(
                        text = "Select Unit",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider()
                }
            },
            content = {
                BottomSheetContent(
                    onItemClicked = { unitCode ->
                        onEvent(MainScreenEvent.BottomSheetItemClicked(unitCode))
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) shouldBottomSheetShow = false
                        }
                    },
                    itemsList = when (state.mode) {
                        ConverterMode.CURRENCY -> state.currencyRates.keys.toList()
                        ConverterMode.LENGTH -> state.lengthUnits.map { it.code }
                        ConverterMode.VOLUME -> state.volumeUnits.map { it.code }
                    }
                )
            }
        )
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == 2

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val (title, modeButtons, contentBox, keyboard) = createRefs()

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    if (isLandscape) {
                        end.linkTo(contentBox.end)
                        width = Dimension.percent(0.5f)
                    } else {
                        end.linkTo(parent.end)
                    }
                },
            text = "Converter",
            fontFamily = FontFamily.SansSerif,
            fontSize = 40.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(modeButtons) {
                    top.linkTo(title.bottom, margin = 8.dp)
                    start.linkTo(parent.start)
                    if (isLandscape) {
                        end.linkTo(contentBox.end)
                        width = Dimension.percent(0.5f)
                    } else {
                        end.linkTo(parent.end)
                    }
                },
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ModeButton(
                text = "Currency",
                isSelected = state.mode == ConverterMode.CURRENCY,
                onClick = {
                    onEvent(MainScreenEvent.CurrencyModeSelected)
                    onEvent(MainScreenEvent.BottomSheetItemClicked("EUR")) // Default from unit
                    onEvent(MainScreenEvent.BottomSheetItemClicked("USD")) // Default to unit
                }

            )
            ModeButton(
                text = "Length",
                isSelected = state.mode == ConverterMode.LENGTH,
                onClick = {
                    onEvent(MainScreenEvent.LengthModeSelected)
                    onEvent(MainScreenEvent.BottomSheetItemClicked("METER")) // Default from unit
                    onEvent(MainScreenEvent.BottomSheetItemClicked("KM")) // Default to unit
                }
            )
            ModeButton(
                text = "Volume",
                isSelected = state.mode == ConverterMode.VOLUME,
                onClick = {
                    onEvent(MainScreenEvent.VolumeModeSelected)
                    onEvent(MainScreenEvent.BottomSheetItemClicked("LITER")) // Default from unit
                    onEvent(MainScreenEvent.BottomSheetItemClicked("ML")) // Default to unit
                }
            )
        }

        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .constrainAs(contentBox) {
                    top.linkTo(modeButtons.bottom, margin = 8.dp)
                    start.linkTo(parent.start)
                    if (isLandscape) {
                        end.linkTo(keyboard.start)
                        width = Dimension.percent(0.5f)
                    } else {
                        end.linkTo(parent.end)
                    }
                }
        ) {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        UnitRow(
                            modifier = Modifier.fillMaxWidth(),
                            unitCode = state.fromCurrencyCode,
                            unitName = getUnitName(state, state.fromCurrencyCode),
                            onDropDownIconClicked = {
                                shouldBottomSheetShow = true
                                onEvent(MainScreenEvent.FromCurrencySelect)
                            }
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.fromCurrencyValue,
                                fontSize = 40.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = MutableInteractionSource(),
                                        indication = null,
                                        onClick = { onEvent(MainScreenEvent.FromCurrencySelect) }
                                    ),
                                color = if (state.selection == SelectionState.FROM) {
                                    MaterialTheme.colorScheme.primary
                                } else MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = {
                                copyToClipboard(context, state.fromCurrencyValue)
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_copy),
                                    contentDescription = "Copy to Clipboard",
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(25.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.toCurrencyValue,
                                fontSize = 40.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = MutableInteractionSource(),
                                        indication = null,
                                        onClick = { onEvent(MainScreenEvent.ToCurrencySelect) }
                                    ),
                                color = if (state.selection == SelectionState.TO) {
                                    MaterialTheme.colorScheme.primary
                                } else MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = {
                                copyToClipboard(context, state.toCurrencyValue)
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_copy),
                                    contentDescription = "Copy to Clipboard",
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(25.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        UnitRow(
                            modifier = Modifier.fillMaxWidth(),
                            unitCode = state.toCurrencyCode,
                            unitName = getUnitName(state, state.toCurrencyCode),
                            onDropDownIconClicked = {
                                shouldBottomSheetShow = true
                                onEvent(MainScreenEvent.ToCurrencySelect)
                            }
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .padding(start = 40.dp)
                    .clip(CircleShape)
                    .clickable { onEvent(MainScreenEvent.SwapIconClicked) }
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sync),
                    contentDescription = "Swap Units",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(25.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        LazyVerticalGrid(
            modifier = Modifier
                .padding(horizontal = 35.dp)
                .constrainAs(keyboard) {
                    //top.linkTo(contentBox.top)
                    bottom.linkTo(parent.bottom)
                    if (isLandscape) {
                        start.linkTo(contentBox.end)
                        end.linkTo(parent.end)
                        width = Dimension.percent(0.42f)
                    } else {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                },
            columns = GridCells.Fixed(3)
        ) {
            items(keys) { key ->
                KeyboardButton(
                    modifier = Modifier.aspectRatio(1f),
                    key = key,
                    backgroundColor = if (key == "C") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    onClick = {
                        onEvent(MainScreenEvent.NumberButtonClicked(key))
                    }
                )
            }
        }
    }
}

@Composable
fun UnitRow(
    modifier: Modifier = Modifier,
    unitCode: String,
    unitName: String,
    onDropDownIconClicked: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = unitCode, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = onDropDownIconClicked) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Open Bottom Sheet"
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(text = unitName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun KeyboardButton(
    modifier: Modifier = Modifier,
    key: String,
    backgroundColor: Color,
    onClick: (String) -> Unit
) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .clip(CircleShape)
            .background(color = backgroundColor)
            .clickable { onClick(key) },
        contentAlignment = Alignment.Center
    ) {
        Text(text = key, fontSize = 32.sp)
    }
}

@Composable
fun ModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(8.dp)
    )
}

fun getUnitName(state: MainScreenState, unitCode: String): String {
    return when (state.mode) {
        ConverterMode.CURRENCY -> state.currencyRates[unitCode]?.name ?: ""
        ConverterMode.LENGTH -> state.lengthUnits.find { it.code == unitCode }?.name ?: ""
        ConverterMode.VOLUME -> state.volumeUnits.find { it.code == unitCode }?.name ?: ""
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PremiumMainScreen(
        state = MainScreenState(
            currencyRates = mapOf(
                "USD" to CurrencyRate("USD", "United States Dollar", 1.0),
                "EUR" to CurrencyRate("EUR", "Euro", 0.85)
            ),
            lengthUnits = listOf(
                LengthUnit("METER", "Meter", 1.0),
                LengthUnit("KM", "Kilometer", 1000.0)
            ),
            volumeUnits = listOf(
                VolumeUnit("LITER", "Liter", 1.0),
                VolumeUnit("ML", "Milliliter", 0.001)
            )
        ),
        onEvent = {}
    )
}