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

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CancelAndOkButtonRow(
    modifier: Modifier = Modifier,
    showCancelButton: Boolean = true,
    cancelButtonEnabled: Boolean = true,
    okButtonEnabled: Boolean = true,
    cancelButtonClick: () -> Unit = {},
    okButtonClick: () -> Unit = {},
    @StringRes cancelButtonTitle: Int,
    @StringRes okButtonTitle: Int,
    cancelButtonContentDescription: String,
    okButtonContentDescription: String,
    okButtonTestTag: String = "",
    cancelButtonTestTag: String = "",
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        if (showCancelButton) {
            TextButton(
                onClick = cancelButtonClick,
                enabled = cancelButtonEnabled,
                modifier =
                    modifier
                        .semantics {
                            testTagsAsResourceId = true
                        }.testTag(okButtonTestTag),
            ) {
                Text(
                    modifier =
                        modifier
                            .semantics { contentDescription = cancelButtonContentDescription },
                    text = stringResource(cancelButtonTitle),
                )
            }
        }
        Spacer(modifier = modifier.width(XSPadding))
        TextButton(
            onClick = okButtonClick,
            enabled = okButtonEnabled,
            modifier =
                modifier
                    .semantics {
                        testTagsAsResourceId = true
                    }.testTag(cancelButtonTestTag),
        ) {
            Text(
                modifier = modifier.semantics { contentDescription = okButtonContentDescription },
                text = stringResource(okButtonTitle),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CancelAndOkButtonRowPreview() {
    RIADigiDocTheme {
        CancelAndOkButtonRow(
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.sign_button,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button),
            okButtonContentDescription = stringResource(id = R.string.sign_button),
            showCancelButton = true,
        )
    }
}
