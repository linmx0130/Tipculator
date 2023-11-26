package me.mengxiaolin.tipculator

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import me.mengxiaolin.tipculator.ui.theme.TipculatorTheme
import me.mengxiaolin.tipculator.ui.theme.Typography

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

            val taxInputBoxFocusRequester = remember { FocusRequester() }
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
                            }
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
                    if (isAboutDialogOpen) {
                        val githubUrl = stringResource(id = R.string.github_url)
                        AlertDialog(
                            onDismissRequest = {isAboutDialogOpen = false},
                            confirmButton = {
                                Button(onClick = {isAboutDialogOpen = false}) {
                                    Text(stringResource(id = R.string.dialog_confirmation_button_label))
                                }
                            },
                            title = {Text(stringResource(id = R.string.about_label))},
                            text = {
                                Column {
                                    Text(stringResource(id = R.string.app_name), style = Typography.h6)
                                    Text("${stringResource(R.string.version_name_label)} ${BuildConfig.VERSION_NAME}")
                                    Text(stringResource(id = R.string.copyright_declaration))
                                    Text(githubUrl, modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                                        startActivity(intent)
                                    })
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun AppTopBar() {

}