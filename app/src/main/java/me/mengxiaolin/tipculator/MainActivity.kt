package me.mengxiaolin.tipculator

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import me.mengxiaolin.tipculator.ui.theme.Typography
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.coroutines.launch
import me.mengxiaolin.tipculator.repository.PreferencesRepository
import me.mengxiaolin.tipculator.ui.theme.TipculatorTheme
import java.io.File
import kotlin.math.roundToInt

@OptIn(SavedStateHandleSaveableApi::class)
class MainActivityViewModel(private val savedStateHandle: SavedStateHandle): ViewModel() {
    // sub total price before tax
    var subTotal: Int by savedStateHandle.saveable {
        mutableIntStateOf(0)
    }

    // all tax, which is not subject to tips
    var tax: Int by savedStateHandle.saveable {
        mutableIntStateOf(0)
    }

    // How many persons are splitting the bill. A null value will hide the input.
    var splitPersonCount: Int? by savedStateHandle.saveable{
        mutableStateOf<Int?>(null)
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var receiptImageDirectory: File
    private val preferences: PreferencesRepository by lazy {
        PreferencesRepository(this.application)
    }
    private val viewModel: MainActivityViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val captureReceiptAction = registerCaptureReceiptActionCallback()

        setContent {
            // tips rate you want to grant
            val tipsRate by preferences.tipsRate.collectAsState(initial = 15)
            // whether to round the total number to an integer
            val isRoundToDollar by preferences.isRoundToZero.collectAsState(initial = false)

            // Sub total after split
            val splitSubTotal = if (viewModel.splitPersonCount != null) {
                (viewModel.subTotal.toDouble() / (viewModel.splitPersonCount?:1)).roundToInt()
            } else {
                viewModel.subTotal
            }
            // tax after split
            val splitTax = if (viewModel.splitPersonCount != null) {
                (viewModel.tax.toDouble() / (viewModel.splitPersonCount?:1)).roundToInt()
            } else {
                viewModel.tax
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
                                    DropdownMenuItem(
                                        onClick = {
                                            captureReceiptAction.launch(receiptImageFileUri)
                                            isMenuExpanded = false
                                        },
                                        text = {
                                            Text(stringResource(id = R.string.receipt_capture_label), style=Typography.bodyMedium)
                                        }
                                    )
                                    DropdownMenuItem(onClick = {
                                        viewModel.splitPersonCount = if (viewModel.splitPersonCount == null) {
                                            2
                                        } else {
                                            null
                                        }
                                        isMenuExpanded = false
                                    }, text = {
                                        if (viewModel.splitPersonCount == null) {
                                            Text(stringResource(id = R.string.enable_split_label), style=Typography.bodyMedium)
                                        } else {
                                            Text(stringResource(id = R.string.disable_split_label), style=Typography.bodyMedium)
                                        }
                                    })
                                    DropdownMenuItem(onClick = {
                                        isAboutDialogOpen = true
                                        isMenuExpanded = false
                                    }, text = {
                                        Text(stringResource(id = R.string.about_label), style=Typography.bodyMedium)
                                    })
                                }
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                    ){
                        CurrencyInputBox(
                            label = stringResource(R.string.subtotal_label),
                            valueInCents = viewModel.subTotal,
                            onValueChange = { viewModel.subTotal = it },
                            onClickEnter = {
                                taxInputBoxFocusRequester.requestFocus()
                            }
                        )
                        CurrencyInputBox(
                            label = stringResource(R.string.tax_label),
                            valueInCents = viewModel.tax,
                            onValueChange = { viewModel.tax = it },
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
                        if (viewModel.splitPersonCount != null) {
                            PositiveIntegerInputBox(
                                label = stringResource(id = R.string.split_label),
                                value = viewModel.splitPersonCount!!,
                                onValueChange = {
                                    viewModel.splitPersonCount = it
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

    private fun registerCaptureReceiptActionCallback(): ActivityResultLauncher<Uri> {
        receiptImageDirectory = File(filesDir, "receipt_images")
        if (!receiptImageDirectory.exists()) {
            receiptImageDirectory.mkdirs()
        }
        val file = File(receiptImageDirectory,"receipt.png")

        // capture receipt action declaration
        return  registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                receiptImageTextRecognition(this@MainActivity, file) { result, exception ->
                    if (exception != null) {
                        Toast.makeText(this@MainActivity, "Error during text recognition.", Toast.LENGTH_LONG).show()
                        return@receiptImageTextRecognition
                    }
                    if (result == null){
                        Toast.makeText(this@MainActivity, "Subtotal and tax information not found.", Toast.LENGTH_LONG).show()
                        return@receiptImageTextRecognition
                    }
                    viewModel.subTotal = result.subTotal
                    viewModel.tax = result.tax
                }

            }
        }
    }

    private val receiptImageFileUri : Uri
        get() {
            val file = File(receiptImageDirectory,"receipt.png")
            return FileProvider.getUriForFile(
                this@MainActivity,
                applicationContext.packageName + ".file_provider",
                file
            )
        }
}