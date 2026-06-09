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

package ee.ria.DigiDoc.ui.component.settings.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSBorder
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.utils.extensions.notAccessible

/**
 * A styled Card containing a radio button row used in settings screens.
 *
 * When [content] is null the card renders a simple full-width row (label + radio button).
 * When [content] is provided the card renders a Column with the label row on top followed by
 * the supplied composable. The content lambda is always invoked when non-null; callers are
 * responsible for any conditional rendering inside it (e.g. `if (selected) { ... }`).
 */
@Composable
fun SettingsRadioCard(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = XSPadding, bottom = SPadding),
        shape = buttonRoundedCornerShape,
        border =
            BorderStroke(
                width = XSBorder,
                color = MaterialTheme.colorScheme.onSurface,
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        if (content != null) {
            Column(
                modifier =
                    Modifier
                        .padding(SPadding)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                RadioButtonRow(label = label, selected = selected, onClick = onClick, withPadding = false)
                content()
            }
        } else {
            RadioButtonRow(label = label, selected = selected, onClick = onClick, withPadding = true)
        }
    }
}

@Composable
private fun RadioButtonRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    withPadding: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (withPadding) Modifier.padding(SPadding) else Modifier)
                .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier =
                Modifier
                    .weight(1f)
                    .notAccessible(),
        )
        RadioButton(
            modifier =
                Modifier
                    .semantics {
                        contentDescription = label
                    },
            selected = selected,
            onClick = onClick,
        )
    }
}
