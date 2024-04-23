@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.SelectionSpinner
import ee.ria.DigiDoc.ui.component.shared.TextCheckBox
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.Dimensions.textVerticalPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SmartIdView(
    modifier: Modifier = Modifier,
    cancelButtonClick: () -> Unit = {},
) {
    Column {
        Text(
            text = stringResource(id = R.string.signature_update_smart_id_message),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = textVerticalPadding),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(id = R.string.signature_update_smart_id_country),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = textVerticalPadding),
        )
        val countriesList = stringArrayResource(id = R.array.smart_id_country)
        SelectionSpinner(
            list = countriesList,
            preselected = countriesList.first(),
            onSelectionChanged = {},
            modifier = modifier.fillMaxWidth().height(Dimensions.textFieldHeight),
        )
        Text(
            text = stringResource(id = R.string.signature_update_mobile_id_personal_code),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = textVerticalPadding),
        )
        var personalCodeText by remember { mutableStateOf(TextFieldValue(text = "")) }
        TextField(
            modifier = modifier.fillMaxWidth().height(Dimensions.textFieldHeight),
            value = personalCodeText,
            shape = RectangleShape,
            onValueChange = {
                personalCodeText = it
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        val rememberMeCheckedState = remember { mutableStateOf(true) }
        TextCheckBox(
            checked = rememberMeCheckedState.value,
            onCheckedChange = { rememberMeCheckedState.value = it },
            title = stringResource(id = R.string.signature_update_mobile_id_remember_me),
            contentDescription = stringResource(id = R.string.signature_update_mobile_id_remember_me).lowercase(),
        )
        CancelAndOkButtonRow(
            cancelButtonTitle = stringResource(id = R.string.cancel_button),
            okButtonTitle = stringResource(id = R.string.sign_button),
            cancelButtonContentDescription = "",
            okButtonContentDescription = "",
            cancelButtonClick = cancelButtonClick,
            okButtonClick = {
                // TODO:
            },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SmartIdViewPreview() {
    RIADigiDocTheme {
        SmartIdView()
    }
}
