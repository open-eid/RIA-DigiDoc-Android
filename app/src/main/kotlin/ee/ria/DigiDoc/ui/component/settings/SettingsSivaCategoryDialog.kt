@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.component.shared.TextRadioButton
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SettingsSivaCategoryDialog(
    modifier: Modifier = Modifier,
    onClickBack: () -> Unit = {},
) {
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
                    .fillMaxWidth(),
            text = stringResource(id = R.string.main_settings_siva_service_title),
            style = MaterialTheme.typography.titleLarge,
        )
        val settingsSivaServiceChoice = remember { mutableStateOf("defaultChoice") }
        TextRadioButton(
            title = stringResource(id = R.string.main_settings_siva_default_access_title),
            contentDescription = stringResource(id = R.string.main_settings_siva_default_access_title).lowercase(),
            selected = settingsSivaServiceChoice.value == "defaultChoice",
            onClick = { settingsSivaServiceChoice.value = "defaultChoice" },
        )
        TextRadioButton(
            title = stringResource(id = R.string.main_settings_siva_default_manual_access_title),
            contentDescription =
                stringResource(
                    id = R.string.main_settings_siva_default_manual_access_title,
                ).lowercase(),
            selected = settingsSivaServiceChoice.value == "manualChoice",
            onClick = { settingsSivaServiceChoice.value = "manualChoice" },
        )
        var settingsSivaServiceUrl by remember { mutableStateOf(TextFieldValue(text = "")) }
        TextField(
            modifier =
                modifier
                    .padding(vertical = screenViewLargePadding)
                    .fillMaxWidth(),
            value = settingsSivaServiceUrl,
            shape = RectangleShape,
            onValueChange = {
                settingsSivaServiceUrl = it
            },
            label = {
                Text(text = stringResource(id = R.string.main_settings_siva_service_url))
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        Text(
            modifier =
                modifier
                    .padding(horizontal = screenViewLargePadding, vertical = screenViewLargePadding)
                    .fillMaxWidth(),
            text = stringResource(id = R.string.main_settings_siva_certificate_title),
        )
        Text(
            modifier =
                modifier
                    .padding(horizontal = screenViewLargePadding, vertical = screenViewLargePadding)
                    .fillMaxWidth(),
            text = stringResource(id = R.string.main_settings_timestamp_cert_issued_to_title),
        )
        Text(
            modifier =
                modifier
                    .padding(horizontal = screenViewLargePadding, vertical = screenViewLargePadding)
                    .fillMaxWidth(),
            text = stringResource(id = R.string.main_settings_timestamp_cert_valid_to_title),
        )
        PrimaryButton(
            modifier =
                modifier.fillMaxWidth().wrapContentHeight().padding(
                    horizontal = screenViewLargePadding,
                ),
            contentDescription =
                stringResource(
                    id = R.string.main_settings_timestamp_cert_add_certificate_button,
                ).lowercase(),
            title = R.string.main_settings_timestamp_cert_add_certificate_button,
            onClickItem = { /* TODO */ },
        )
        PrimaryButton(
            modifier =
                modifier.fillMaxWidth().wrapContentHeight().padding(
                    horizontal = screenViewLargePadding,
                ),
            contentDescription =
                stringResource(
                    id = R.string.main_settings_timestamp_cert_show_certificate_button,
                ).lowercase(),
            title = R.string.main_settings_timestamp_cert_show_certificate_button,
            onClickItem = { /* TODO */ },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsSivaCategoryDialogPreview() {
    RIADigiDocTheme {
        SettingsSivaCategoryDialog()
    }
}
