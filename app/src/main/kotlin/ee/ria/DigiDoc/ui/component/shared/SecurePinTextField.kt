@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.zIndex
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SecurePinTextField(
    modifier: Modifier,
    pin: MutableState<ByteArray>,
    pinCodeLabel: String,
    pinNumberFocusRequester: FocusRequester,
    previousFocusRequester: FocusRequester,
    pinCodeTextEdited: MutableState<Boolean>?,
    trailingIconContentDescription: String,
    isError: Boolean,
    keyboardImeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val context = LocalContext.current

    OutlinedTextField(
        label = {
            Text(text = pinCodeLabel)
        },
        value = "*".repeat(pin.value.size),
        singleLine = true,
        modifier =
            modifier
                .focusRequester(pinNumberFocusRequester)
                .focusProperties {
                    previous = previousFocusRequester
                }
                .zIndex(1f)
                .focusable()
                .semantics {
                    traversalIndex = 1f
                    testTagsAsResourceId = true
                }
                .testTag("pinTextField"),
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter { it.isDigit() }
            if (digitsOnly.isEmpty()) {
                if (pin.value.isNotEmpty()) {
                    pin.value = pin.value.dropLast(1).toByteArray()
                }
            } else {
                pin.value = pin.value + digitsOnly.last().code.toByte()
            }
            pinCodeTextEdited?.value = true
        },
        trailingIcon = {
            if (!isTalkBackEnabled(context) && pin.value.isNotEmpty()) {
                IconButton(
                    modifier =
                        modifier
                            .zIndex(2f)
                            .semantics { traversalIndex = 2f }
                            .testTag("pinRemoveButton"),
                    onClick = { pin.value = byteArrayOf() },
                ) {
                    Icon(
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("pinRemoveButtonIcon"),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                        contentDescription = trailingIconContentDescription,
                    )
                }
            }
        },
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
            ),
        keyboardOptions =
            KeyboardOptions.Default.copy(
                imeAction = keyboardImeAction,
                keyboardType = KeyboardType.NumberPassword,
            ),
        keyboardActions = keyboardActions,
        isError = isError,
    )
}
