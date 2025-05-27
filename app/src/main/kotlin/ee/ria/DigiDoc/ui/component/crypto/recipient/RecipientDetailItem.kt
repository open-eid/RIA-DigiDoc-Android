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
    @DrawableRes val icon: Int = R.drawable.ic_m3_expand_content_48dp_wght400,
    @StringRes val label: Int = 0,
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
    ): List<RecipientDetailItem> =
        listOf(
            RecipientDetailItem(
                icon = 0,
                label = R.string.recipient_details_name_label,
                value = recipientFormattedName,
                certificate = recipient.data.x509Certificate(),
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
        )
}
