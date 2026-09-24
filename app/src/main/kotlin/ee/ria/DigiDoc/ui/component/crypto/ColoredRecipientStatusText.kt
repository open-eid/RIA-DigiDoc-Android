// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
