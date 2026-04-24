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

package ee.ria.DigiDoc.ui.component.signing

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import ee.ria.DigiDoc.utilsLib.container.NameUtil

@Composable
fun StyledNameText(
    modifier: Modifier = Modifier,
    name: String,
    allCaps: Boolean = false,
    formatName: Boolean = true,
) {
    var formattedName = if (formatName) NameUtil.formatName(name) else name

    if (allCaps) {
        formattedName = formattedName.uppercase()
    }

    val nameParts = formattedName.split(", ").map { it.trim() }

    val styledText =
        buildAnnotatedString {
            if (nameParts.size == 2) {
                val (fullName, code) = nameParts

                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(fullName)
                pop()

                append(", $code")
            } else {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(formattedName)
            }
        }

    Text(
        modifier = modifier,
        text = styledText,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyLarge,
    )
}
