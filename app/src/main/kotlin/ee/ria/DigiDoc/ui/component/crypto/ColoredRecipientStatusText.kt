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

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ee.ria.DigiDoc.ui.component.shared.TagBadge
import ee.ria.DigiDoc.ui.theme.extendedColorScheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColoredRecipientStatusText(
    text: String,
    status: RecipientDecryptionStatus,
    modifier: Modifier = Modifier,
) {
    val tagBackgroundColor =
        when (status) {
            RecipientDecryptionStatus.NOT_ENCRYPTED -> MaterialTheme.colorScheme.surfaceVariant
            RecipientDecryptionStatus.NOT_ENCRYPTED_EXPIRED,
            RecipientDecryptionStatus.EXPIRED,
            -> MaterialTheme.colorScheme.errorContainer
            RecipientDecryptionStatus.VALID -> MaterialTheme.extendedColorScheme.successContainer
        }

    val tagContentColor =
        when (status) {
            RecipientDecryptionStatus.NOT_ENCRYPTED -> MaterialTheme.colorScheme.onSurface
            RecipientDecryptionStatus.NOT_ENCRYPTED_EXPIRED,
            RecipientDecryptionStatus.EXPIRED,
            -> MaterialTheme.colorScheme.onErrorContainer
            RecipientDecryptionStatus.VALID -> MaterialTheme.extendedColorScheme.onSuccessContainer
        }

    FlowRow(
        modifier = modifier,
    ) {
        TagBadge(
            text = text,
            backgroundColor = tagBackgroundColor,
            contentColor = tagContentColor,
            modifier =
                modifier
                    .alignByBaseline()
                    .focusable()
                    .testTag("recipientListDecryptionStatus"),
        )
    }
}
