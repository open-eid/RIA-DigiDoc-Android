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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun HrefMessageDialog(
    modifier: Modifier = Modifier,
    @StringRes text1: Int,
    @StringRes text1Arg: Int? = null,
    @StringRes text2: Int?,
    @StringRes linkText: Int,
    @StringRes linkUrl: Int,
    newLineBeforeLink: Boolean = false,
    newLineBeforeText2: Boolean = false,
) {
    Column(
        modifier = modifier.padding(SPadding),
    ) {
        HrefDynamicText(
            modifier = modifier.fillMaxWidth(),
            text1 = stringResource(text1, text1Arg?.let { stringResource(it) } ?: ""),
            text2 = text2?.let { stringResource(it) } ?: "",
            linkText = stringResource(linkText),
            linkUrl = stringResource(linkUrl),
            newLineBeforeText2 = newLineBeforeText2,
            newLineBeforeLink = newLineBeforeLink,
            textStyle =
                TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    textAlign = TextAlign.Start,
                ),
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HrefMessageDialogPreview() {
    RIADigiDocTheme {
        HrefMessageDialog(
            text1 = R.string.main_diagnostics_restart_message,
            text2 = R.string.main_diagnostics_restart_message_restart_now,
            linkText = R.string.read_more_here,
            linkUrl = R.string.main_diagnostics_restart_message_href,
        )
    }
}
