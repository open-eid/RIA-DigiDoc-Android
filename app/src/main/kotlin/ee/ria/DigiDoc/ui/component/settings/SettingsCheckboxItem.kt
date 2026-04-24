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

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsCheckboxItem(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    title: String,
    contentDescription: String,
    testTag: String = "",
) {
    Row(
        modifier =
            modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(vertical = MSPadding)
                .toggleable(
                    value = checked,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                ).semantics(mergeDescendants = true) {
                    testTagsAsResourceId = true
                    this.contentDescription = contentDescription
                }.testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier =
                Modifier
                    .weight(1f)
                    .notAccessible(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge,
        )
        Checkbox(
            modifier = Modifier.padding(horizontal = XSPadding),
            checked = checked,
            onCheckedChange = null,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsCheckboxItemPreview() {
    RIADigiDocTheme {
        Column {
            SettingsCheckboxItem(
                checked = false,
                title = stringResource(id = R.string.main_settings_open_all_filetypes_title),
                contentDescription = stringResource(id = R.string.main_settings_open_all_filetypes_title).lowercase(),
            )
            SettingsCheckboxItem(
                checked = true,
                title = stringResource(id = R.string.main_settings_allow_screenshots_title),
                contentDescription = stringResource(id = R.string.main_settings_allow_screenshots_title).lowercase(),
            )
        }
    }
}
