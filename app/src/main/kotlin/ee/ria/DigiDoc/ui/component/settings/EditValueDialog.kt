@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers

@OptIn(ExperimentalComposeUiApi::class)
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
        modifier =
            modifier
                .padding(screenViewLargePadding)
                .testTag("editValueDialog"),
    ) {
        Text(
            modifier =
                modifier
                    .padding(
                        vertical = screenViewLargePadding,
                    )
                    .fillMaxWidth()
                    .semantics { heading() },
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )
        TextField(
            modifier =
                modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics {
                        testTagsAsResourceId = true
                        testTag = "editValueDialogTextField"
                        contentDescription =
                            "$title " +
                            "${formatNumbers(editValue.text)} "
                    },
            shape = RectangleShape,
            value = editValue,
            onValueChange = onEditValueChange,
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleSmall,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Ascii,
                ),
        )
        CancelAndOkButtonRow(
            modifier = modifier,
            cancelButtonClick = cancelButtonClick,
            okButtonClick = okButtonClick,
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.ok_button,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button).lowercase(),
            okButtonContentDescription = stringResource(id = R.string.ok_button).lowercase(),
            cancelButtonTestTag = "editValueDialogCancelButton",
            okButtonTestTag = "editValueDialogOkButton",
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
