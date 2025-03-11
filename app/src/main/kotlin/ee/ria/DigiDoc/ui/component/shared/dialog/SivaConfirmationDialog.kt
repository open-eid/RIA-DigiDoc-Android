@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SivaConfirmationDialog(
    showDialog: MutableState<Boolean>,
    onResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit, showDialog) {
        if (showDialog.value) {
            focusRequester.requestFocus()
        }
    }

    if (showDialog.value) {
        ConfirmationDialog(
            modifier =
                modifier
                    .semantics {
                        testTagsAsResourceId = true
                    }
                    .testTag("sivaConfirmationDialog"),
            showDialog = showDialog.value,
            text1 = R.string.siva_send_message_dialog,
            text2 = R.string.siva_continue_question,
            linkText = R.string.siva_read_here,
            linkUrl = R.string.siva_info_url,
            onConfirm = {
                showDialog.value = false
                onResult(true)
            },
            onDismiss = {
                showDialog.value = false
                onResult(false)
            },
        )
    }
}
