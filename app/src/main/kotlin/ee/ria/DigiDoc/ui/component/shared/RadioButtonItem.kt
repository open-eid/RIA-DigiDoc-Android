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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RadioButtonItem(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @StringRes changedLabel: Int,
    contentDescription: String,
    testTag: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val context = LocalContext.current
    val titleText = stringResource(id = title)
    val changedText = stringResource(id = changedLabel)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .selectable(
                    selected = isSelected,
                    role = Role.RadioButton,
                    onClick = {
                        onSelect()
                        sendAccessibilityEvent(
                            context,
                            changedText,
                        )
                    },
                ).semantics {
                    testTagsAsResourceId = true
                    this.contentDescription = contentDescription
                }.testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titleText,
            modifier =
                modifier
                    .weight(1f)
                    .notAccessible(),
        )

        RadioButton(
            selected = isSelected,
            onClick = null,
        )
    }
}
