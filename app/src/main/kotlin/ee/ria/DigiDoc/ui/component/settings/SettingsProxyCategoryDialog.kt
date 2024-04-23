@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.BackButton
import ee.ria.DigiDoc.ui.component.shared.TextRadioButton
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.Dimensions.settingsItemEndPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.settingsItemStartPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.textFieldHeight
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SettingsProxyCategoryDialog(
    modifier: Modifier = Modifier,
    onClickBack: () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(Dimensions.alertDialogInnerPadding),
    ) {
        BackButton(
            onClickBack = onClickBack,
        )
        Text(
            modifier =
                modifier
                    .padding(horizontal = settingsItemStartPadding, vertical = settingsItemEndPadding)
                    .fillMaxWidth(),
            text = stringResource(id = R.string.main_settings_proxy_title),
            style = MaterialTheme.typography.titleLarge,
        )
        val settingsProxyChoice = remember { mutableStateOf("proxyNoProxy") }
        TextRadioButton(
            title = stringResource(id = R.string.main_settings_proxy_no_proxy),
            contentDescription = stringResource(id = R.string.main_settings_proxy_no_proxy).lowercase(),
            selected = settingsProxyChoice.value == "proxyNoProxy",
            onClick = { settingsProxyChoice.value = "proxyNoProxy" },
        )
        TextRadioButton(
            title = stringResource(id = R.string.main_settings_proxy_use_system),
            contentDescription = stringResource(id = R.string.main_settings_proxy_use_system).lowercase(),
            selected = settingsProxyChoice.value == "proxyUseSystem",
            onClick = { settingsProxyChoice.value = "proxyUseSystem" },
        )
        TextRadioButton(
            title = stringResource(id = R.string.main_settings_proxy_manual),
            contentDescription = stringResource(id = R.string.main_settings_proxy_manual).lowercase(),
            selected = settingsProxyChoice.value == "proxyManual",
            onClick = { settingsProxyChoice.value = "proxyManual" },
        )
        var settingsProxyHost by remember { mutableStateOf(TextFieldValue(text = "")) }
        TextField(
            modifier =
                modifier
                    .padding(vertical = settingsItemEndPadding)
                    .fillMaxWidth()
                    .height(textFieldHeight),
            value = settingsProxyHost,
            shape = RectangleShape,
            onValueChange = {
                settingsProxyHost = it
            },
            label = {
                Text(text = stringResource(id = R.string.main_settings_proxy_host))
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        var settingsProxyPort by remember { mutableStateOf(TextFieldValue(text = "")) }
        TextField(
            modifier =
                modifier
                    .padding(vertical = settingsItemEndPadding)
                    .fillMaxWidth()
                    .height(textFieldHeight),
            value = settingsProxyPort,
            shape = RectangleShape,
            onValueChange = {
                settingsProxyPort = it
            },
            label = {
                Text(text = stringResource(id = R.string.main_settings_proxy_port))
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        var settingsProxyUsername by remember { mutableStateOf(TextFieldValue(text = "")) }
        TextField(
            modifier =
                modifier
                    .padding(vertical = settingsItemEndPadding)
                    .fillMaxWidth()
                    .height(textFieldHeight),
            value = settingsProxyUsername,
            shape = RectangleShape,
            onValueChange = {
                settingsProxyUsername = it
            },
            label = {
                Text(text = stringResource(id = R.string.main_settings_proxy_username))
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        var settingsProxyPassword by remember { mutableStateOf(TextFieldValue(text = "")) }
        TextField(
            modifier =
                modifier
                    .padding(vertical = settingsItemEndPadding)
                    .fillMaxWidth()
                    .height(textFieldHeight),
            value = settingsProxyPassword,
            shape = RectangleShape,
            onValueChange = {
                settingsProxyPassword = it
            },
            label = {
                Text(text = stringResource(id = R.string.main_settings_proxy_password))
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsProxyCategoryDialogPreview() {
    RIADigiDocTheme {
        SettingsProxyCategoryDialog()
    }
}
