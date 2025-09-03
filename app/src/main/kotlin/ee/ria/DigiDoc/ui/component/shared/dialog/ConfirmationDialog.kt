@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    @StringRes text1: Int,
    @StringRes text2: Int,
    @StringRes linkText: Int,
    @StringRes linkUrl: Int,
    showLinkOnOneLine: Boolean = true,
    modifier: Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val cancelButton = stringResource(R.string.cancel_button)
    val confirmButton = stringResource(R.string.ok_button)
    val buttonName = stringResource(id = R.string.button_name)

    if (showDialog) {
        AlertDialog(
            modifier =
                modifier
                    .semantics {
                        testTagsAsResourceId = true
                    }.testTag("sivaConfirmationDialog"),
            onDismissRequest = onDismiss,
            title = {},
            text = {
                Column(
                    modifier =
                        modifier
                            .verticalScroll(rememberScrollState()),
                ) {
                    HrefMessageDialog(
                        modifier =
                            modifier
                                .semantics {
                                    testTagsAsResourceId = true
                                }.testTag("sivaConfirmationMessageDialog"),
                        text1 = text1,
                        text2 = text2,
                        linkText = linkText,
                        linkUrl = linkUrl,
                        showLinkOnOneLine = showLinkOnOneLine,
                    )
                }
            },
            dismissButton = {
                TextButton(onDismiss) {
                    Text(
                        modifier =
                            modifier
                                .semantics {
                                    this.contentDescription = "$cancelButton $buttonName"
                                    testTagsAsResourceId = true
                                }.testTag("confirmationDialogCancelButton"),
                        text = cancelButton,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(
                        modifier =
                            modifier
                                .semantics {
                                    this.contentDescription = "$confirmButton $buttonName"
                                    testTagsAsResourceId = true
                                }.testTag("confirmationDialogConfirmButton"),
                        text = confirmButton,
                    )
                }
            },
        )
        InvisibleElement(modifier = modifier)
    }
}
