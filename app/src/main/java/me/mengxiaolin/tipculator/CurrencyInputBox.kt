/**
 * Copyright Mengxiao Lin, all rights reserved.
 *
 *
 */
package me.mengxiaolin.tipculator

import android.widget.Toast
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.mengxiaolin.tipculator.ui.theme.TipculatorTheme
import me.mengxiaolin.tipculator.ui.theme.Typography
import java.math.BigDecimal

/**
 *  The input box with label for currency numbers.
 *
 *  * `label`: the label of the input box
 *  * `valueInCents`: the number value in cents.
 *  * `onValueChange`: the callback to call when the new value is available
 *  * `isEditable`: whether the input box is editable
 */
@Composable
fun CurrencyInputBox(
    label: String,
    valueInCents: Int,
    onValueChange: (Int) -> Unit,
    isEditable: Boolean = true
) {
    var bufferValue by rememberSaveable{ mutableStateOf("") }
    var isError by rememberSaveable {mutableStateOf(false) }
    LaunchedEffect(key1 = valueInCents, block = {
        bufferValue = (valueInCents.toBigDecimal().setScale(2) / BigDecimal.valueOf(100.0)).toString()
    })
    val androidContext = LocalContext.current

    val onInputConfirmed = {
        val pureNumber = bufferValue.filter { c -> c.isDigit() || c == '.' }
        if (pureNumber.isEmpty()) {
            onValueChange(0)
        } else {
            try {
                val newValue = (pureNumber.toBigDecimal() * BigDecimal.valueOf(100)).toInt()
                onValueChange(newValue)
            } catch (_: NumberFormatException){
                isError = true
                Toast.makeText(androidContext, R.string.invalid_number_format_warn, Toast.LENGTH_SHORT).show()
            }
        }
    }
    Column (modifier = Modifier.padding(12.dp)) {
        Text(label, style= Typography.h5, modifier = Modifier.padding(end = 4.dp))
        TextField(
            value = bufferValue,
            onValueChange = {
                if (!isEditable) return@TextField
                isError = false
                if (it.contains('\n')) {
                    onInputConfirmed()
                }else {
                    bufferValue = it
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType= KeyboardType.Decimal),
            modifier = Modifier
                .onFocusChanged {
                    if (!it.isFocused) {
                        onInputConfirmed()
                    }
                }
                .fillMaxWidth()
                .focusable(isEditable),
            isError = isError,
            readOnly = !isEditable,
            textStyle = Typography.h6
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyInputBoxPreview() {
    var valueInCents by remember { mutableStateOf(12345) }
    TipculatorTheme {
        Column {
            CurrencyInputBox("Total", valueInCents, onValueChange = {
                valueInCents = it
            })
            CurrencyInputBox("After tax", valueInCents + 100, onValueChange = {
                valueInCents = it - 100
            })
        }
    }
}