/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SecurePinTextField(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    pin: MutableState<ByteArray>,
    label: String,
    pinCodeTextEdited: MutableState<Boolean>? = null,
    isError: Boolean = false,
    errorText: String = "",
    keyboardImeAction: ImeAction = ImeAction.Done,
    onDone: (() -> Unit)? = null,
    removeIconTestTag: String = "",
    showIconTestTag: String = "",
    errorTestTag: String = "",
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val keyboardController = LocalSoftwareKeyboardController.current

    var shown by rememberSaveable { mutableStateOf(false) }

    val displayText =
        remember(pin.value, shown) {
            if (shown) shownPinText(pin.value) else "*".repeat(pin.value.size)
        }

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(R.string.button_name)
    val showPasswordText = stringResource(R.string.show_password)
    val hidePasswordText = stringResource(R.string.hide_password)

    CompositionLocalProvider(LocalTextToolbar provides NoFloatingToolbar) {
        Column(modifier = modifier) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier =
                        Modifier
                            .focusRequester(focusRequester)
                            .weight(1f)
                            .fillMaxWidth()
                            .semantics {
                                if (!shown) {
                                    password()
                                }
                                testTagsAsResourceId = true
                            }.testTag("pinTextField"),
                    label = {
                        Text(text = label)
                    },
                    value = displayText,
                    singleLine = true,
                    onValueChange = { newValue ->
                        val digits = newValue.filter { it.isDigit() }
                        val previous = pin.value
                        val next =
                            if (shown) {
                                // Shown text is the PIN itself, so it is authoritative
                                ByteArray(digits.length) { digits[it].code.toByte() }
                            } else if (digits.isEmpty()) {
                                // Masked, so an edit leaving no digit can only be a deletion
                                if (previous.isNotEmpty()) {
                                    previous.copyOf(previous.size - 1)
                                } else {
                                    previous
                                }
                            } else {
                                previous + digits.last().code.toByte()
                            }
                        pin.value = next
                        pinCodeTextEdited?.value = true
                    },
                    trailingIcon = {
                        IconButton(onClick = { shown = !shown }) {
                            Icon(
                                modifier =
                                    Modifier
                                        .size(iconSizeXXS)
                                        .semantics { testTagsAsResourceId = true }
                                        .then(
                                            if (showIconTestTag.isNotEmpty()) {
                                                Modifier.testTag(showIconTestTag)
                                            } else {
                                                Modifier
                                            },
                                        ),
                                imageVector =
                                    ImageVector.vectorResource(
                                        if (shown) {
                                            R.drawable.ic_visibility
                                        } else {
                                            R.drawable.ic_visibility_off
                                        },
                                    ),
                                contentDescription = if (shown) hidePasswordText else showPasswordText,
                            )
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
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                onDone?.invoke()
                            },
                        ),
                    isError = isError,
                )

                if (isTalkBackEnabled(context) && pin.value.isNotEmpty()) {
                    IconButton(onClick = {
                        pin.value = byteArrayOf()
                        pinCodeTextEdited?.value = true
                        scope.launch(Main) {
                            focusRequester.requestFocus()
                            focusManager.clearFocus()
                            delay(200)
                            focusRequester.requestFocus()
                        }
                    }) {
                        Icon(
                            modifier =
                                Modifier
                                    .semantics { testTagsAsResourceId = true }
                                    .then(
                                        if (removeIconTestTag.isNotEmpty()) {
                                            Modifier.testTag(removeIconTestTag)
                                        } else {
                                            Modifier
                                        },
                                    ),
                            imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                            contentDescription = "$clearButtonText $buttonName",
                        )
                    }
                }
            }

            if (errorText.isNotEmpty()) {
                Text(
                    modifier =
                        Modifier
                            .padding(top = XSPadding)
                            .padding(bottom = MSPadding)
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = errorText
                                liveRegion = LiveRegionMode.Polite
                            }.testTag(errorTestTag),
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun shownPinText(pin: ByteArray): String = String(CharArray(pin.size) { pin[it].toInt().toChar() })

// Dont show Copy, Paste options
private val NoFloatingToolbar =
    object : TextToolbar {
        override val status: TextToolbarStatus = TextToolbarStatus.Hidden

        override fun hide() = Unit

        override fun showMenu(
            rect: Rect,
            onCopyRequested: (() -> Unit)?,
            onPasteRequested: (() -> Unit)?,
            onCutRequested: (() -> Unit)?,
            onSelectAllRequested: (() -> Unit)?,
        ) = Unit
    }
