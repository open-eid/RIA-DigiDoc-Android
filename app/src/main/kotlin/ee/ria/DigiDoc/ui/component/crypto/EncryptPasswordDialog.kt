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

package ee.ria.DigiDoc.ui.component.crypto

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.PrimaryTextField
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.crypto.PasswordUtil.isPasswordValid

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EncryptPasswordDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onEncrypt: (keyLabel: String, password: String) -> Unit = { _, _ -> },
) {
    var keyLabel by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var password by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var repeatPassword by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    val keyLabelLabel = stringResource(R.string.crypto_password_key_label_placeholder)
    val passwordLabel = stringResource(R.string.crypto_password_enter)
    val repeatPasswordLabel = stringResource(R.string.crypto_password_repeat)

    val requirementsTitle = stringResource(R.string.crypto_password_requirements_title)
    val requirementLength = stringResource(R.string.crypto_password_requirement_length)
    val requirementNumber = stringResource(R.string.crypto_password_requirement_number)
    val requirementUppercase = stringResource(R.string.crypto_password_requirement_uppercase)
    val requirementLowercase = stringResource(R.string.crypto_password_requirement_lowercase)
    val requirementLengthTts = stringResource(R.string.crypto_password_requirement_length_tts)
    val requirementNumberTts = stringResource(R.string.crypto_password_requirement_number_tts)
    val requirementStrings =
        remember(requirementLength, requirementNumber, requirementUppercase, requirementLowercase) {
            listOf(requirementLength, requirementNumber, requirementUppercase, requirementLowercase)
        }
    val requirementsContentDescription =
        remember(
            requirementsTitle,
            requirementLengthTts,
            requirementNumberTts,
            requirementUppercase,
            requirementLowercase,
        ) {
            "$requirementsTitle: ${
                listOf(
                    requirementLengthTts,
                    requirementNumberTts,
                    requirementUppercase,
                    requirementLowercase,
                ).joinToString(", ")
            }"
        }

    val passwordValid by remember { derivedStateOf { isPasswordValid(password.text) } }
    val passwordIsError = password.text.isNotEmpty() && !passwordValid
    val passwordsMatch = password.text == repeatPassword.text
    val repeatPasswordIsError = repeatPassword.text.isNotEmpty() && !passwordsMatch

    PasswordDialogScaffold(
        modifier = modifier,
        title = stringResource(R.string.crypto_encrypt_tab_password),
        okButtonTitle = R.string.encrypt_button,
        okButtonEnabled = passwordValid && passwordsMatch,
        onDismiss = onDismiss,
        onOkButtonClick = { onEncrypt(keyLabel.text, password.text) },
        cancelButtonTestTag = "encryptPasswordDialogCancelButton",
        okButtonTestTag = "encryptPasswordDialogEncryptButton",
    ) {
        PrimaryTextField(
            modifier = modifier.fillMaxWidth(),
            value = keyLabel,
            onValueChange = { keyLabel = it },
            label = keyLabelLabel,
            placeholder = keyLabelLabel,
            description = stringResource(R.string.crypto_password_key_label_hint),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(modifier = modifier.height(MPadding))

        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
        ) {
            Row(
                modifier = modifier.padding(SPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_m3_info_48dp_wght400),
                    contentDescription = null,
                    modifier = modifier.size(iconSizeXXS),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = modifier.width(XSPadding))
                Text(
                    text = stringResource(R.string.crypto_password_secure_place_info),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = modifier.height(MPadding))

        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .semantics { isTraversalGroup = true },
        ) {
            PrimaryTextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .semantics {
                            isTraversalGroup = true
                            traversalIndex = 1f
                        },
                value = password,
                onValueChange = { password = it },
                label = passwordLabel,
                placeholder = passwordLabel,
                isPasswordText = true,
                isError = passwordIsError,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                    ),
            )

            Spacer(modifier = modifier.height(SPadding))

            val requirementColor: Color =
                if (passwordIsError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }

            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics {
                            traversalIndex = 0f
                            contentDescription = requirementsContentDescription
                        },
            ) {
                requirementStrings.forEach { req ->
                    Text(
                        text = "• $req",
                        modifier = modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelMedium,
                        color = requirementColor,
                        textAlign = TextAlign.Start,
                    )
                }
            }
        }

        Spacer(modifier = modifier.height(MPadding))

        PrimaryTextField(
            modifier = modifier.fillMaxWidth(),
            value = repeatPassword,
            onValueChange = { repeatPassword = it },
            label = repeatPasswordLabel,
            placeholder = repeatPasswordLabel,
            isPasswordText = true,
            isError = repeatPasswordIsError,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
        )

        if (repeatPasswordIsError) {
            Text(
                modifier = modifier.fillMaxWidth().padding(top = XSPadding),
                text = stringResource(R.string.crypto_password_mismatch),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EncryptPasswordDialogPreview() {
    RIADigiDocTheme {
        EncryptPasswordDialog()
    }
}
