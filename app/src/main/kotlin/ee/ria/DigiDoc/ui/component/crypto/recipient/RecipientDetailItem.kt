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

package ee.ria.DigiDoc.ui.component.crypto.recipient

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.utilsLib.date.DateUtil.dateFormat
import ee.ria.DigiDoc.utilsLib.extensions.x509Certificate
import java.security.cert.X509Certificate

data class RecipientDetailItem(
    @param:DrawableRes val icon: Int = R.drawable.ic_m3_expand_content_48dp_wght400,
    @param:StringRes val label: Int = 0,
    val value: String? = null,
    val certificate: X509Certificate? = null,
    val isLink: Boolean = false,
    val contentDescription: String = "",
    val formatForAccessibility: Boolean = false,
    val testTag: String = "",
) {
    @Composable
    fun recipientDetailItems(
        recipient: Addressee,
        recipientFormattedName: String?,
        recipientIssuerName: String?,
        recipientConcatKDFAlgorithmURI: String?,
    ): List<RecipientDetailItem> {
        val recipientCertificate = recipient.data.x509Certificate()
        return listOf(
            RecipientDetailItem(
                icon =
                    if (recipientCertificate != null) {
                        R.drawable.ic_m3_expand_content_48dp_wght400
                    } else {
                        0
                    },
                label = R.string.recipient_details_name_label,
                value = recipientFormattedName,
                certificate = recipientCertificate,
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.recipient_details_name_label,
                        )} $value"
                    } else {
                        ""
                    },
                testTag = "recipientFormattedName",
            ),
            RecipientDetailItem(
                icon = 0,
                label = R.string.recipient_details_certificate_issuer_label,
                value = recipientIssuerName,
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.recipient_details_certificate_issuer_label,
                        )} $value"
                    } else {
                        ""
                    },
                testTag = "recipientCertificateIssuer",
            ),
            RecipientDetailItem(
                icon = 0,
                label = R.string.recipient_details_concat_kdf_algorithm_url,
                value = recipientConcatKDFAlgorithmURI,
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.recipient_details_concat_kdf_algorithm_url,
                        )} $value"
                    } else {
                        ""
                    },
                testTag = "recipientConcatKDFAlgorithmURI",
            ),
            RecipientDetailItem(
                icon = 0,
                label = R.string.recipient_details_certificate_valid_to_label,
                value =
                    recipient.validTo?.let {
                        dateFormat.format(
                            it,
                        )
                    },
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.recipient_details_certificate_valid_to_label,
                        )} $value"
                    } else {
                        ""
                    },
                testTag = "recipientCertificateValidTo",
            ),
            RecipientDetailItem(
                icon = 0,
                label = R.string.recipient_details_key_label,
                value = recipient.keyLabel,
                testTag = "recipientKeyLabel",
            ),
            RecipientDetailItem(
                icon = 0,
                label = R.string.recipient_details_keyserver_label,
                value = recipient.serverId,
                testTag = "recipientServerId",
            ),
            RecipientDetailItem(
                icon = 0,
                label = R.string.recipient_details_transaction_label,
                value = recipient.transactionId,
                testTag = "recipientTransactionId",
            ),
        )
    }
}
