// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.ui.component.shared.TagBadge
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.extendedColorScheme
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColoredSignedStatusText(
    text: String,
    status: ValidatorInterface.Status,
    modifier: Modifier = Modifier,
    validUntil: Date? = null,
    isSignatureExtended: Boolean = false,
) {
    val parts = text.split(" (", limit = 2)

    val isSignatureValidOrWarning =
        status == ValidatorInterface.Status.Valid ||
            status == ValidatorInterface.Status.Warning ||
            status == ValidatorInterface.Status.NonQSCD

    val tagBackgroundColor =
        if (isSignatureValidOrWarning) {
            MaterialTheme.extendedColorScheme.successContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        }

    val tagContentColor =
        if (isSignatureValidOrWarning) {
            MaterialTheme.extendedColorScheme.onSuccessContainer
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        }

    val additionalTextColor =
        if (status == ValidatorInterface.Status.Valid) {
            MaterialTheme.extendedColorScheme.onWarningContainer
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        }

    FlowRow(
        modifier = modifier,
    ) {
        if (validUntil != null) {
            val isExpired = validUntil.before(Date()) || !isSignatureValidOrWarning
            val validUntilBackgroundColor =
                if (isExpired) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.extendedColorScheme.successContainer
                }
            val validUntilContentColor =
                if (isExpired) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.extendedColorScheme.onSuccessContainer
                }
            val formattedDate = DateUtil.dateFormat.format(validUntil)
            TagBadge(
                text = stringResource(R.string.signature_valid_until, formattedDate),
                backgroundColor = validUntilBackgroundColor,
                contentColor = validUntilContentColor,
                modifier =
                    modifier
                        .alignByBaseline()
                        .focusable()
                        .testTag("signatureUpdateListValidUntil"),
            )
        }

        if (!isSignatureExtended || validUntil == null || !isSignatureValidOrWarning) {
            TagBadge(
                text = parts[0],
                backgroundColor = tagBackgroundColor,
                contentColor = tagContentColor,
                modifier =
                    modifier
                        .alignByBaseline()
                        .focusable()
                        .testTag("signatureUpdateListSignatureStatus"),
            )

            if (parts.size > 1) {
                Text(
                    text = " (${parts[1]}",
                    color = additionalTextColor,
                    modifier =
                        modifier
                            .alignByBaseline()
                            .focusable()
                            .testTag("signatureUpdateListSignatureStatusCaution"),
                    style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ColoredSignedStatusTextPreview() {
    RIADigiDocTheme {
        ColoredSignedStatusText(
            text = "Allkiri on kehtiv",
            status = ValidatorInterface.Status.Valid,
            modifier = Modifier,
        )
    }
}
