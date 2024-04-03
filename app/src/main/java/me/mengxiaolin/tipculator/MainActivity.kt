package me.mengxiaolin.tipculator

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.mengxiaolin.tipculator.repository.PreferencesRepository
import me.mengxiaolin.tipculator.ui.theme.TipculatorTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val preferences: PreferencesRepository by lazy {
        PreferencesRepository(this.application)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // sub total price before tax
            var subTotal by rememberSaveable { mutableStateOf(0) }
            // all tax, which is not subject to tips
            var tax by rememberSaveable { mutableStateOf(0) }
            // How many persons are splitting the bill. A null value will hide the input.
            var splitPersonCount by rememberSaveable {
                mutableStateOf<Int?>(null)
            }
            // tips rate you want to grant
            val tipsRate by preferences.tipsRate.collectAsState(initial = 15)
            // whether to round the total number to an integer
            val isRoundToDollar by preferences.isRoundToZero.collectAsState(initial = false)

            // Sub total after split
            val splitSubTotal = if (splitPersonCount != null) {
                (subTotal.toDouble() / (splitPersonCount?:1)).roundToInt()
            } else {
                subTotal
            }
            // tax after split
            val splitTax = if (splitPersonCount != null) {
                (tax.toDouble() / (splitPersonCount?:1)).roundToInt()
            } else {
                tax
            }

            // tips in cent
            val tipsInCent = calculateTips(
                subTotal = splitSubTotal,
                tax = splitTax,
                tipsRate = tipsRate,
                isRoundToDollar = isRoundToDollar
            )

            // UI control states
            val scrollState = rememberScrollState(0)
            val taxInputBoxFocusRequester = remember { FocusRequester() }
            val totalInputBoxFocusRequester = remember { FocusRequester() }
            var isAboutDialogOpen by remember { mutableStateOf(false) }

            TipculatorTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {Text(
                                stringResource(id = R.string.app_name)
                            )},
                            actions = {
                                var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
                                IconButton(onClick = { isMenuExpanded = !isMenuExpanded}) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = stringResource(id = R.string.menu_description)
                                    )
                                }
                                DropdownMenu(
                                    expanded = isMenuExpanded,
                                    onDismissRequest = { isMenuExpanded = false },
                                ) {
                                    DropdownMenuItem(onClick = {
                                        splitPersonCount = if (splitPersonCount == null) {
                                            2
                                        } else {
                                            null
                                        }
                                        isMenuExpanded = false
                                    }) {
                                        if (splitPersonCount == null) {
                                            Text(stringResource(id = R.string.enable_split_label))
                                        } else {
                                            Text(stringResource(id = R.string.disable_split_label))
                                        }
                                    }
                                    DropdownMenuItem(onClick = {
                                        isAboutDialogOpen = true
                                        isMenuExpanded = false
                                    }) {
                                        Text(stringResource(id = R.string.about_label))
                                    }
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    Column(modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                    ){
                        CurrencyInputBox(
                            label = stringResource(R.string.subtotal_label),
                            valueInCents = subTotal,
                            onValueChange = { subTotal = it },
                            onClickEnter = {
                                taxInputBoxFocusRequester.requestFocus()
                            }
                        )
                        CurrencyInputBox(
                            label = stringResource(R.string.tax_label),
                            valueInCents = tax,
                            onValueChange = { tax = it },
                            focusRequester = taxInputBoxFocusRequester,
                            onClickEnter = {
                                // hide the soft keyboard when user finishes input
                                val view = currentFocus
                                if (view != null) {
                                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                                }
                                totalInputBoxFocusRequester.requestFocus()
                            }
                        )
                        TipsRateSelector(
                            value = tipsRate,
                            onValueChanged = {
                                lifecycleScope.launch {
                                    preferences.setTipsRate(it)
                                }
                            },
                            isRoundToDollar = isRoundToDollar,
                            onIsRoundToDollarChanged = {
                                lifecycleScope.launch {
                                    preferences.setIsRoundToZero(it)
                                }
                            }
                        )
                        if (splitPersonCount != null) {
                            PositiveIntegerInputBox(
                                label = stringResource(id = R.string.split_label),
                                value = splitPersonCount!!,
                                onValueChange = {
                                    splitPersonCount = it
                                }
                            )
                            CurrencyInputBox(
                                label = stringResource(R.string.subtotal_after_split_label),
                                valueInCents = splitSubTotal,
                                onValueChange = {},
                                isEditable = false
                            )
                            CurrencyInputBox(
                                label = stringResource(R.string.tax_after_split_label),
                                valueInCents = splitTax,
                                onValueChange = {},
                                isEditable = false
                            )
                        }
                        CurrencyInputBox(
                            label = stringResource(R.string.tips_label),
                            valueInCents = tipsInCent,
                            onValueChange = {},
                            isEditable = false
                        )
                        CurrencyInputBox(
                            label = stringResource(R.string.total_label),
                            valueInCents = splitSubTotal +splitTax + tipsInCent,
                            focusRequester = totalInputBoxFocusRequester,
                            onValueChange = {},
                            isEditable = false
                        )
                    }
                    if (isAboutDialogOpen) {
                        AboutDialog {isAboutDialogOpen = false}
                    }
                }
            }
        }
    }
}