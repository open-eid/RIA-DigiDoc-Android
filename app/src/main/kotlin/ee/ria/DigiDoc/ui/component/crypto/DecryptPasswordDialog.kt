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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.PrimaryTextField
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DecryptPasswordDialog(
    modifier: Modifier = Modifier,
    keyLabel: String,
    onDismiss: () -> Unit = {},
    onDecrypt: (password: String) -> Unit = {},
) {
    var password by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    val containerKeyLabelTitle = stringResource(R.string.crypto_password_key_label_placeholder)
    val containerPasswordLabel = stringResource(R.string.crypto_password_enter)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier =
                modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier =
                    modifier
                        .padding(MPadding)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.decrypt_button),
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Start,
                )

                Spacer(modifier = modifier.height(MPadding))

                Text(
                    text = containerKeyLabelTitle,
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .notAccessible(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = keyLabel,
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "$containerKeyLabelTitle, $keyLabel"
                            },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                )

                Spacer(modifier = modifier.height(MPadding))

                PrimaryTextField(
                    modifier = modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { password = it },
                    label = containerPasswordLabel,
                    placeholder = containerPasswordLabel,
                    isPasswordText = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                )

                Spacer(modifier = modifier.height(MPadding))

                CancelAndOkButtonRow(
                    modifier = modifier,
                    cancelButtonClick = onDismiss,
                    okButtonClick = { onDecrypt(password.text) },
                    okButtonEnabled = password.text.isNotEmpty(),
                    cancelButtonTitle = R.string.cancel_button,
                    okButtonTitle = R.string.decrypt_button,
                    cancelButtonContentDescription =
                        stringResource(R.string.cancel_button).lowercase(),
                    okButtonContentDescription =
                        stringResource(R.string.decrypt_button).lowercase(),
                    cancelButtonTestTag = "decryptPasswordDialogCancelButton",
                    okButtonTestTag = "decryptPasswordDialogDecryptButton",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DecryptPasswordDialogPreview() {
    RIADigiDocTheme {
        DecryptPasswordDialog(keyLabel = "Allkirjastamata lepingud 2026")
    }
}
