@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.ui.component.shared.BackButton
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.component.shared.TextRadioButton
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import kotlinx.coroutines.delay

@Composable
fun SettingsProxyCategoryDialog(
    modifier: Modifier = Modifier,
    onClickBack: () -> Unit = {},
    proxyChoice: String = ProxySetting.NO_PROXY.name,
    proxyHostValue: TextFieldValue = TextFieldValue(""),
    onProxyHostValueChange: (TextFieldValue) -> Unit = {},
    proxyPortValue: TextFieldValue = TextFieldValue("80"),
    onProxyPortValueChange: (TextFieldValue) -> Unit = {},
    proxyUsernameValue: TextFieldValue = TextFieldValue(""),
    onProxyUsernameValueChange: (TextFieldValue) -> Unit = {},
    proxyPasswordValue: TextFieldValue = TextFieldValue(""),
    onProxyPasswordValueChange: (TextFieldValue) -> Unit = {},
    onClickNoProxy: () -> Unit = {},
    onClickManualProxy: () -> Unit = {},
    onClickSystemProxy: () -> Unit = {},
    checkConnectionClick: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        delay(200)
        focusManager.clearFocus()
        delay(200)
        focusRequester.requestFocus()
    }

    val proxyDialogTitle = stringResource(id = R.string.main_settings_proxy_title)

    Column(
        modifier = modifier.padding(itemSpacingPadding),
    ) {
        BackButton(
            onClickBack = onClickBack,
        )
        Text(
            modifier =
                modifier
                    .padding(horizontal = screenViewLargePadding, vertical = screenViewLargePadding)
                    .fillMaxWidth()
                    .semantics {
                        heading()
                        this.contentDescription = proxyDialogTitle.lowercase()
                    }
                    .focusRequester(focusRequester)
                    .focusable(),
            text = stringResource(id = R.string.main_settings_proxy_title),
            style = MaterialTheme.typography.titleLarge,
        )
        TextRadioButton(
            title = stringResource(id = R.string.main_settings_proxy_no_proxy),
            contentDescription = stringResource(id = R.string.main_settings_proxy_no_proxy).lowercase(),
            selected = proxyChoice == ProxySetting.NO_PROXY.name,
            onClick = onClickNoProxy,
        )
        TextRadioButton(
            title = stringResource(id = R.string.main_settings_proxy_use_system),
            contentDescription = stringResource(id = R.string.main_settings_proxy_use_system).lowercase(),
            selected = proxyChoice == ProxySetting.SYSTEM_PROXY.name,
            onClick = onClickSystemProxy,
        )
        TextRadioButton(
            title = stringResource(id = R.string.main_settings_proxy_manual),
            contentDescription = stringResource(id = R.string.main_settings_proxy_manual).lowercase(),
            selected = proxyChoice == ProxySetting.MANUAL_PROXY.name,
            onClick = onClickManualProxy,
        )
        TextField(
            enabled = proxyChoice == ProxySetting.MANUAL_PROXY.name,
            modifier =
                modifier
                    .padding(vertical = screenViewLargePadding)
                    .fillMaxWidth(),
            shape = RectangleShape,
            value = proxyHostValue,
            onValueChange = onProxyHostValueChange,
            label = {
                Text(text = stringResource(id = R.string.main_settings_proxy_host))
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Ascii,
                ),
        )
        TextField(
            enabled = proxyChoice == ProxySetting.MANUAL_PROXY.name,
            modifier =
                modifier
                    .padding(vertical = screenViewLargePadding)
                    .fillMaxWidth(),
            shape = RectangleShape,
            value = proxyPortValue,
            onValueChange = onProxyPortValueChange,
            label = {
                Text(text = stringResource(id = R.string.main_settings_proxy_port))
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Decimal,
                ),
        )
        TextField(
            enabled = proxyChoice == ProxySetting.MANUAL_PROXY.name,
            modifier =
                modifier
                    .padding(vertical = screenViewLargePadding)
                    .fillMaxWidth(),
            shape = RectangleShape,
            value = proxyUsernameValue,
            onValueChange = onProxyUsernameValueChange,
            label = {
                Text(text = stringResource(id = R.string.main_settings_proxy_username))
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Ascii,
                ),
        )
        var passwordVisible by rememberSaveable { mutableStateOf(false) }
        TextField(
            enabled = proxyChoice == ProxySetting.MANUAL_PROXY.name,
            modifier =
                modifier
                    .padding(vertical = screenViewLargePadding)
                    .fillMaxWidth(),
            shape = RectangleShape,
            value = proxyPasswordValue,
            onValueChange = onProxyPasswordValueChange,
            label = {
                Text(text = stringResource(id = R.string.main_settings_proxy_password))
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Password,
                ),
            trailingIcon = {
                val image =
                    if (passwordVisible) {
                        ImageVector.vectorResource(id = R.drawable.ic_visibility)
                    } else {
                        ImageVector.vectorResource(id = R.drawable.ic_visibility_off)
                    }
                val description =
                    if (passwordVisible) {
                        stringResource(
                            id = R.string.hide_password,
                        )
                    } else {
                        stringResource(id = R.string.show_password)
                    }
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            },
        )
        PrimaryButton(
            modifier =
                modifier.fillMaxWidth().wrapContentHeight().padding(
                    horizontal = screenViewLargePadding,
                ),
            contentDescription =
                stringResource(
                    id = R.string.main_settings_proxy_check_connection,
                ).lowercase(),
            title = R.string.main_settings_proxy_check_connection,
            onClickItem = checkConnectionClick,
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
