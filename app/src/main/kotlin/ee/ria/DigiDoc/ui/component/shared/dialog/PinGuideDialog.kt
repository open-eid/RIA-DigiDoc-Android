@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared.dialog

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.pin.PinChangeVariant
import ee.ria.DigiDoc.ui.component.shared.MessageDialog

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PinGuideDialog(
    modifier: Modifier = Modifier,
    showDialog: MutableState<Boolean>,
    pinChangeVariant: PinChangeVariant,
    @StringRes title: Int,
    titleExtra: String = "",
    guidelines: String,
    @StringRes confirmButton: Int,
    confirmButtonExtra: String = "",
    onResult: (Boolean, PinChangeVariant) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    val titleText = stringResource(title, titleExtra)
    val dismissButtonText = stringResource(R.string.close_button)
    val confirmButtonText = stringResource(confirmButton, confirmButtonExtra)

    LaunchedEffect(Unit, showDialog) {
        if (showDialog.value) {
            focusRequester.requestFocus()
        }
    }

    if (showDialog.value) {
        MessageDialog(
            modifier = modifier,
            title = titleText,
            message = guidelines,
            showIcons = false,
            dismissButtonText = dismissButtonText,
            confirmButtonText = confirmButtonText,
            dismissButtonContentDescription = dismissButtonText,
            confirmButtonContentDescription = confirmButtonText,
            onDismissRequest = {
                showDialog.value = false
            },
            onDismissButton = {
                showDialog.value = false
                onResult(false, pinChangeVariant)
            },
            onConfirmButton = {
                showDialog.value = true
                onResult(true, pinChangeVariant)
            },
        )
    }
}
