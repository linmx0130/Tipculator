package me.mengxiaolin.tipculator

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import me.mengxiaolin.tipculator.ui.theme.Typography

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val androidContext = LocalContext.current
    val githubUrl = stringResource(id = R.string.github_url)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(id = R.string.dialog_confirmation_button_label))
            }
        },
        title= {Text(stringResource(id = R.string.app_name), style = Typography.headlineSmall)},
        text= {
            Column {
                Text("${stringResource(R.string.version_name_label)} ${BuildConfig.VERSION_NAME}")
                Text(stringResource(id = R.string.copyright_declaration))
                Text(githubUrl, modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                    androidContext.startActivity(intent)
                })
            }
        }
    )
}