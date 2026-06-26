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

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.ui.component.shared.TagBadge
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.extendedColorScheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColoredSignedStatusText(
    text: String,
    status: ValidatorInterface.Status,
    modifier: Modifier = Modifier,
) {
    val parts = text.split(" (", limit = 2)

    val isSignatureValidOrWarning =
        status == ValidatorInterface.Status.Valid ||
            status == ValidatorInterface.Status.Warning ||
            status == ValidatorInterface.Status.NonQSCD

    val tagBackgroundColor = if (isSignatureValidOrWarning) MaterialTheme.extendedColorScheme.successContainer else MaterialTheme.colorScheme.errorContainer
    val tagContentColor = if (isSignatureValidOrWarning) MaterialTheme.extendedColorScheme.onSuccessContainer else MaterialTheme.colorScheme.onErrorContainer
    val additionalTextColor =
        if (status == ValidatorInterface.Status.Valid) {
            MaterialTheme.extendedColorScheme.onWarningContainer
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        }

    FlowRow(
        modifier = modifier,
    ) {
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
