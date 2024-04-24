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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.TextCheckBox
import ee.ria.DigiDoc.ui.theme.Dimensions.alertDialogInnerPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.settingsItemEndPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.settingsItemStartPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.textFieldHeight
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SettingsEditValueDialog(
    modifier: Modifier = Modifier,
    cancelButtonClick: () -> Unit = {},
    okButtonClick: () -> Unit = {},
    placeHolderText: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit = {},
    title: String,
    useDefaultChecked: Boolean = true,
    useDefaultCheckedChange: (Boolean) -> Unit = {},
) {
    Column(
        modifier = modifier.padding(alertDialogInnerPadding),
    ) {
        Text(
            modifier =
                modifier
                    .padding(horizontal = settingsItemStartPadding, vertical = settingsItemEndPadding)
                    .fillMaxWidth(),
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )
        TextField(
            modifier =
                modifier
                    .padding(vertical = settingsItemEndPadding)
                    .fillMaxWidth()
                    .height(textFieldHeight),
            value = value,
            enabled = !useDefaultChecked,
            shape = RectangleShape,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeHolderText)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        TextCheckBox(
            checked = useDefaultChecked,
            onCheckedChange = useDefaultCheckedChange,
            title = stringResource(id = R.string.main_settings_tsa_url_use_default),
            contentDescription = stringResource(id = R.string.signature_update_mobile_id_remember_me).lowercase(),
        )
        CancelAndOkButtonRow(
            cancelButtonTitle = stringResource(id = R.string.cancel_button),
            okButtonTitle = stringResource(id = R.string.ok_button),
            cancelButtonContentDescription = "",
            okButtonContentDescription = "",
            cancelButtonClick = cancelButtonClick,
            okButtonClick = okButtonClick,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsAccessToTimeStampingServiceDialogPreview() {
    RIADigiDocTheme {
        SettingsEditValueDialog(
            title = "Option setting edit ".repeat(2),
            placeHolderText = "some value placeholder",
            value = TextFieldValue("some value"),
        )
    }
}
