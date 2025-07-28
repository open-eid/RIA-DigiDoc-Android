@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
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
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.extensions.notAccessible
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }

    val buttonName = stringResource(id = R.string.button_name)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

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
                        vertical = SPadding,
                    ).fillMaxWidth()
                    .semantics {
                        heading()
                        testTagsAsResourceId = true
                    }.testTag("editValueTitle"),
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = modifier.height(SPadding))

        Row(
            modifier =
                modifier
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier =
                    modifier
                        .focusRequester(focusRequester)
                        .weight(1f)
                        .fillMaxWidth()
                        .clearAndSetSemantics {
                            testTagsAsResourceId = true
                            testTag = "editValueDialogTextField"
                            contentDescription = "$title ${formatNumbers(editValue.text)}"
                        }.testTag("editValueTextField"),
                value = editValue,
                onValueChange = { newValue ->
                    onEditValueChange(newValue.copy(selection = TextRange(newValue.text.length)))
                },
                label = { Text(subtitle) },
                maxLines = 1,
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Ascii,
                    ),
                trailingIcon = {
                    if (!isTalkBackEnabled(context) && editValue.text.isNotEmpty()) {
                        IconButton(onClick = {
                            onClearValueClick()
                            scope.launch(Main) {
                                focusRequester.requestFocus()
                                focusManager.clearFocus()
                                delay(200)
                                focusRequester.requestFocus()
                            }
                        }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                contentDescription = "${stringResource(R.string.clear_text)} $buttonName",
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

            if (isTalkBackEnabled(context) && editValue.text.isNotEmpty()) {
                IconButton(onClick = onClearValueClick) {
                    Icon(
                        modifier =
                            modifier
                                .semantics {
                                    testTagsAsResourceId = true
                                }.testTag("editValueRemoveIconButton"),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                        contentDescription = "${stringResource(R.string.clear_text)} $buttonName",
                    )
                }
            }
        }

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
