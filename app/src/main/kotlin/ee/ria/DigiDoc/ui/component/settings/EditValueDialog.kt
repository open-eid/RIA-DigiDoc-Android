@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditValueDialog(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    editValue: TextFieldValue = TextFieldValue(""),
    onEditValueChange: (TextFieldValue) -> Unit = {},
    onClearValueClick: () -> Unit = {},
    cancelButtonClick: () -> Unit = {},
    okButtonClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .padding(MPadding)
                .fillMaxWidth()
                .testTag("editValueDialog"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            modifier =
                modifier
                    .size(MPadding)
                    .notAccessible(),
            tint = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = modifier.height(XSPadding))

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
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = modifier.height(SPadding))

        OutlinedTextField(
            modifier =
                modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics {
                        testTagsAsResourceId = true
                        testTag = "editValueDialogTextField"
                        contentDescription =
                            "$title ${formatNumbers(editValue.text)}"
                    },
            value = editValue,
            onValueChange = onEditValueChange,
            label = { Text(subtitle) },
            maxLines = 1,
            singleLine = true,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Ascii,
                ),
            trailingIcon = {
                if (editValue.text.isNotEmpty()) {
                    IconButton(onClick = onClearValueClick) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                            contentDescription = stringResource(R.string.clear_text),
                        )
                    }
                }
            },
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
        )

        Spacer(modifier = modifier.height(SPadding))

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
            title = "Change container name",
            editValue = TextFieldValue("some_File_name.pdf"),
            subtitle = "Container name",
        )
    }
}
