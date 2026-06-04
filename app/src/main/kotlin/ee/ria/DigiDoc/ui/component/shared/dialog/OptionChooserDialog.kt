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

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OptionChooserDialog(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    choices: List<String>,
    selectedChoice: Int = 0,
    cancelButtonClick: () -> Unit = {},
    okButtonClick: (Int) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    var currentChoice by remember { mutableIntStateOf(selectedChoice) }

    val optionText = stringResource(id = R.string.option)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier =
            modifier
                .padding(MPadding)
                .fillMaxWidth()
                .testTag("optionChooserDialog"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier =
                modifier
                    .padding(vertical = SPadding)
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .semantics {
                        heading()
                        testTagsAsResourceId = true
                    }.testTag("optionChooserTitle"),
            text = stringResource(title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Start,
        )

        Spacer(modifier = modifier.height(SPadding))

        Column {
            choices.forEachIndexed { index, choice ->
                Row(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(vertical = SPadding)
                            .padding(start = XSPadding)
                            .selectable(
                                selected = index == currentChoice,
                                role = Role.RadioButton,
                                onClick = {
                                    currentChoice = index
                                },
                            ).semantics {
                                testTagsAsResourceId = true
                                this.contentDescription =
                                    String.format(optionText, choices[index].lowercase())
                            }.testTag("optionChooser$index"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = choice,
                        modifier =
                            modifier
                                .weight(1f)
                                .notAccessible(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    RadioButton(
                        selected = index == currentChoice,
                        onClick = null,
                    )
                }
                HorizontalDivider()
            }
        }

        Spacer(modifier = modifier.height(SPadding))

        CancelAndOkButtonRow(
            modifier = modifier,
            cancelButtonClick = cancelButtonClick,
            okButtonClick = {
                okButtonClick(currentChoice)
            },
            cancelButtonTitle = R.string.close_button,
            okButtonTitle = R.string.choose_button,
            cancelButtonContentDescription = stringResource(id = R.string.close_button).lowercase(),
            okButtonContentDescription = stringResource(id = R.string.choose_button).lowercase(),
            cancelButtonTestTag = "optionChooserDialogCancelButton",
            okButtonTestTag = "optionChooserDialogOkButton",
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OptionChooserDialogPreview() {
    RIADigiDocTheme {
        OptionChooserDialog(
            title = R.string.choose_server_option,
            choices =
                listOf(
                    "Server 1",
                    "Server 2",
                ),
            cancelButtonClick = {},
            okButtonClick = {},
        )
    }
}
