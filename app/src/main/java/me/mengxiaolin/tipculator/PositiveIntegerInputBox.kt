package me.mengxiaolin.tipculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import me.mengxiaolin.tipculator.ui.theme.Typography

/**
 * An input box only allows positive integers.
 */
@Composable
fun PositiveIntegerInputBox(label: String,
                            value: Int,
                            onValueChange: (Int) -> Unit,
                            isEditable: Boolean = true,
                            onClickEnter: () -> Unit = {},
                            focusRequester: FocusRequester = FocusRequester()
) {
    var bufferValue by rememberSaveable{ mutableStateOf("") }
    var isError by rememberSaveable {mutableStateOf(false) }

    LaunchedEffect(key1 = value, block = {
        bufferValue = value.toString()
        isError = false
    })

    val onInputConfirmed = {
        val pureNumberStr = bufferValue.filter { c -> c.isDigit()}
        if (pureNumberStr.isEmpty()) {
            isError = true
        } else {
            val number = pureNumberStr.toInt()
            if (number > 0) {
                onValueChange(number)
            } else {
                isError = true
            }
        }
    }

    Column (modifier = Modifier.padding(12.dp)) {
        Text(label, style= Typography.headlineSmall, modifier = Modifier.padding(end = 4.dp))
        TextField(
            value = bufferValue,
            onValueChange = {
                if (!isEditable) return@TextField
                isError = false
                if (it.contains('\n')) {
                    onInputConfirmed()
                    onClickEnter()
                }else {
                    bufferValue = it
                    // terminate if illegal character is found
                    if (it.any { c -> !c.isDigit()}) {
                        isError = true
                    }
                    if (it.isNotEmpty() && it.isDigitsOnly()) {
                        onInputConfirmed()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType= KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                onInputConfirmed()
                onClickEnter()
            }),
            modifier = Modifier
                .onFocusChanged {
                    if (!it.isFocused) {
                        // on losing the focus: likely the input is confirmed
                        onInputConfirmed()
                    }
                }
                .fillMaxWidth()
                .focusRequester(focusRequester),
            isError = isError,
            readOnly = !isEditable,
            textStyle = Typography.headlineSmall,
        )
    }
}