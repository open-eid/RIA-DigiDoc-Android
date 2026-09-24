// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SingleButtonDialog(
    title: String,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonName = stringResource(id = R.string.button_name)

    AlertDialog(
        onDismissRequest = onButtonClick,
        title = {
            Text(
                text = title,
                modifier =
                    modifier
                        .fillMaxWidth()
                        .semantics {
                            heading()
                            this.contentDescription = title.lowercase()
                            testTagsAsResourceId = true
                        }.testTag("singleButtonDialogTitleText"),
            )
        },
        text = {
            Text(
                text = message,
                modifier =
                    modifier
                        .fillMaxWidth()
                        .semantics {
                            this.contentDescription = message.lowercase()
                            testTagsAsResourceId = true
                        }.verticalScroll(rememberScrollState())
                        .testTag("singleButtonDialogMessageText"),
            )
        },
        confirmButton = {
            TextButton(onClick = onButtonClick) {
                Text(
                    text = buttonText,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        modifier
                            .semantics {
                                this.contentDescription = "$buttonText $buttonName"
                                testTagsAsResourceId = true
                            }.testTag("singleButtonDialogButtonText"),
                )
            }
        },
    )
}
