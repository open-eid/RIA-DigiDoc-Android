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
    newLineBeforeLink: Boolean = true,
    newLineBeforeText2: Boolean = true,
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
                        newLineBeforeLink = newLineBeforeLink,
                        newLineBeforeText2 = newLineBeforeText2,
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
