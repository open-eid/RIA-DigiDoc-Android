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
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun EditValueDialog(
    modifier: Modifier = Modifier,
    title: String,
    editValue: TextFieldValue = TextFieldValue(""),
    onEditValueChange: (TextFieldValue) -> Unit = {},
    cancelButtonClick: () -> Unit = {},
    okButtonClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(Dimensions.alertDialogInnerPadding),
    ) {
        Text(
            modifier =
                modifier
                    .padding(
                        horizontal = Dimensions.settingsItemStartPadding,
                        vertical = Dimensions.settingsItemEndPadding,
                    )
                    .fillMaxWidth(),
            text = title,
            style = MaterialTheme.typography.titleSmall,
        )
        TextField(
            modifier =
                modifier
                    .padding(vertical = Dimensions.settingsItemEndPadding)
                    .fillMaxWidth()
                    .height(Dimensions.textFieldHeight),
            shape = RectangleShape,
            value = editValue,
            onValueChange = onEditValueChange,
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleSmall,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        )
        CancelAndOkButtonRow(
            cancelButtonClick = cancelButtonClick,
            okButtonClick = okButtonClick,
            cancelButtonTitle = stringResource(id = R.string.cancel_button),
            okButtonTitle = stringResource(id = R.string.ok_button),
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button).lowercase(),
            okButtonContentDescription = stringResource(id = R.string.ok_button).lowercase(),
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditValueDialogPreview() {
    RIADigiDocTheme {
        EditValueDialog(
            title = "Container name",
            editValue = TextFieldValue("some_File_name.pdf"),
        )
    }
}
