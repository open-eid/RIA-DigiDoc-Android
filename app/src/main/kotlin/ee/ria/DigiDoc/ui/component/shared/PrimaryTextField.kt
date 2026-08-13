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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.addInvisibleElement
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.getAccessibilityEventType
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.utils.extensions.notAccessible
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PrimaryTextField(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    placeholder: String = "",
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readDigitByDigit: Boolean = false,
    description: String = "",
    isError: Boolean = false,
    errorText: String = "",
    isPasswordText: Boolean = false,
    keyboardOptions: KeyboardOptions =
        KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
        ),
    trailingIcon: (@Composable () -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    testTag: String = "",
    removeIconTestTag: String = "",
    descriptionTestTag: String = "",
    errorTestTag: String = "",
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var editingStarted by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(R.string.button_name)

    LaunchedEffect(errorText) {
        if (errorText.isNotEmpty()) {
            sendAccessibilityEvent(context, getAccessibilityEventType(), errorText)
        }
    }

    Column(
        modifier =
            modifier.semantics {
                isTraversalGroup = true
            },
    ) {
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
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                editingStarted = false
                            }
                        }.semantics {
                            contentDescription =
                                if (readDigitByDigit && value.text.isNotEmpty() && value.text.all { it.isDigit() }) {
                                    value.text.split("").joinToString(" ")
                                } else if (isPasswordText) {
                                    ""
                                } else {
                                    if (description.isNotEmpty()) {
                                        "$label, $description: ${value.text}"
                                    } else {
                                        "$label: ${value.text}"
                                    }
                                }
                            testTagsAsResourceId = true
                        }.then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
                enabled = enabled,
                value = value,
                readOnly = readOnly,
                singleLine = singleLine,
                onValueChange = { newValue ->
                    val corrected =
                        if (isTalkBackEnabled(context) && !editingStarted) {
                            editingStarted = true
                            newValue.copy(selection = TextRange(newValue.text.length))
                        } else {
                            newValue
                        }
                    onValueChange(corrected)
                },
                shape = RectangleShape,
                label = {
                    Text(
                        text = label,
                    )
                },
                placeholder = {
                    Text(
                        modifier = Modifier.notAccessible(),
                        text = placeholder,
                    )
                },
                trailingIcon = {
                    if (trailingIcon != null) {
                        trailingIcon()
                    } else if (!readOnly && !isTalkBackEnabled(context) && value.text.isNotEmpty()) {
                        IconButton(onClick = {
                            onValueChange(TextFieldValue(""))
                        }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                contentDescription = "$clearButtonText $buttonName",
                            )
                        }
                    }
                },
                visualTransformation =
                    if (!isPasswordText) VisualTransformation.None else PasswordVisualTransformation(),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                keyboardOptions = keyboardOptions,
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            onDone?.invoke()
                        },
                    ),
                isError = isError,
            )

            if (trailingIcon == null && !readOnly && isTalkBackEnabled(context) && value.text.isNotEmpty()) {
                IconButton(onClick = {
                    onValueChange(TextFieldValue(""))
                    scope.launch(Main) {
                        focusRequester.requestFocus()
                        focusManager.clearFocus()
                        delay(200.milliseconds)
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

        if (description.isNotEmpty()) {
            Text(
                text = description,
                modifier =
                    Modifier
                        .padding(vertical = XSPadding)
                        .testTag(descriptionTestTag)
                        .then(
                            // Accessibility - read description before focusing on TextField
                            if (enabled) {
                                Modifier.semantics { traversalIndex = -1f }
                            } else {
                                Modifier.notAccessible()
                            },
                        ),
                color = MaterialTheme.colorScheme.onSecondary,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.labelMedium,
            )
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

fun talkBackTextFieldValue(text: String): TextFieldValue {
    val withInvisibleElements = addInvisibleElement(text)
    return TextFieldValue(
        text = withInvisibleElements,
        selection = TextRange(withInvisibleElements.length),
    )
}
