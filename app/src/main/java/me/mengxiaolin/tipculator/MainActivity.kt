package me.mengxiaolin.tipculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.mengxiaolin.tipculator.ui.theme.TipculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var subTotal by rememberSaveable { mutableStateOf(0) }
            var tax by rememberSaveable { mutableStateOf(0) }
            var tipsRate by rememberSaveable { mutableStateOf(15) }
            var isRoundToDollar by rememberSaveable { mutableStateOf(false) }
            val scrollState = rememberScrollState(0)
            val tipsInCent = calculateTips(subTotal, tax, tipsRate, isRoundToDollar)

            TipculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(scrollState)
                    ){
                        CurrencyInputBox(
                            label = stringResource(R.string.subtotal_label),
                            valueInCents = subTotal,
                            onValueChange = { subTotal = it }
                        )
                        CurrencyInputBox(
                            label = stringResource(R.string.tax_label),
                            valueInCents = tax,
                            onValueChange = { tax = it }
                        )
                        TipsRateSelector(
                            value = tipsRate,
                            onValueChanged = {
                                tipsRate = it
                            },
                            isRoundToDollar = isRoundToDollar,
                            onIsRoundToDollarChanged = {isRoundToDollar = it}
                        )
                        CurrencyInputBox(
                            label = stringResource(R.string.tips_label),
                            valueInCents = tipsInCent,
                            onValueChange = {},
                            isEditable = false
                        )
                        CurrencyInputBox(
                            label = stringResource(R.string.total_label),
                            valueInCents = subTotal + tax + tipsInCent,
                            onValueChange = {},
                            isEditable = false
                        )
                    }
                }
            }
        }
    }
}
