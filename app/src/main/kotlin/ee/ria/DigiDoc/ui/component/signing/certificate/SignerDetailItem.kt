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

package ee.ria.DigiDoc.ui.component.signing.certificate

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.utilsLib.container.NameUtil
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.extensions.hexString
import ee.ria.DigiDoc.utilsLib.extensions.x509Certificate
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import java.security.cert.X509Certificate

data class SignerDetailItem(
    @param:DrawableRes val icon: Int = R.drawable.ic_m3_expand_content_48dp_wght400,
    @param:StringRes val label: Int = 0,
    val value: String? = null,
    val certificate: X509Certificate? = null,
    val isTimestamp: Boolean = false,
    val isLink: Boolean = false,
    val contentDescription: String = "",
    val formatForAccessibility: Boolean = false,
    val testTag: String = "",
) {
    @Composable
    fun signersDetailItems(
        sharedContainerViewModel: SharedContainerViewModel,
        signature: SignatureInterface,
        isTimestamp: Boolean,
        signerIssuerName: String?,
        tsIssuerName: String?,
        ocspIssuerName: String?,
        tsSubjectName: String?,
        ocspSubjectName: String?,
    ): List<SignerDetailItem> {
        val signersCertificate =
            if (signature.isDigitalSeal) {
                signature.signedBy
            } else if (isTimestamp) {
                NameUtil.formatName(signature.signedBy).uppercase()
            } else {
                NameUtil.formatName(signature.signedBy)
            }
        val signatureMethod = signature.signatureMethod
        val containerFormat =
            (sharedContainerViewModel.currentContainer() as? SignedContainer)?.containerMimetype() ?: ""
        val signatureFormat = signature.profile
        val signedFileCount =
            sharedContainerViewModel.signedContainer.value
                ?.rawContainer()
                ?.dataFiles()
                ?.size
                ?.toString() ?: ""
        val timestampTime = DateUtil.getFormattedDateTime(signature.timeStampTime, false)
        val timestampTimeUtc = DateUtil.getFormattedDateTime(signature.timeStampTime, true)
        val hashValue = signature.messageImprint.hexString()
        val ocspProducedAt = DateUtil.getFormattedDateTime(signature.ocspProducedAt, false)
        val ocspProducedAtUtc = DateUtil.getFormattedDateTime(signature.ocspProducedAt, true)
        val signersMobileTime = DateUtil.getFormattedDateTime(signature.claimedSigningTime, true)

        return listOf(
            SignerDetailItem(
                icon = 0,
                label = R.string.signer_certificate_issuer_label,
                value = signerIssuerName,
                contentDescription =
                    if (signerIssuerName != null) {
                        "${stringResource(
                            id = R.string.signer_certificate_issuer_label,
                        )} $signerIssuerName"
                    } else {
                        ""
                    },
                testTag = "signersCertificateIssuer",
            ),
            SignerDetailItem(
                label = R.string.signers_certificate_label,
                value = signersCertificate,
                certificate = signature.signingCertificateDer.x509Certificate(),
                contentDescription =
                    "${stringResource(
                        id = R.string.signers_certificate_label,
                    )}, $signersCertificate",
                formatForAccessibility = true,
                testTag = "signersCertificate",
            ),
            SignerDetailItem(
                icon = R.drawable.ic_m3_open_in_new_48dp_wght400,
                isLink = true,
                label = R.string.signature_method_label,
                value = signatureMethod,
                contentDescription =
                    "${stringResource(
                        id = R.string.signature_method_label,
                    )}, link $signatureMethod",
                testTag = "signatureDetailMethod",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.container_format_label,
                value = containerFormat,
                contentDescription =
                    "${stringResource(
                        id = R.string.container_format_label,
                    )}, $containerFormat",
                testTag = "containerDetailFormat",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.signature_format_label,
                value = signatureFormat,
                contentDescription =
                    "${stringResource(
                        id = R.string.signature_format_label,
                    )}, $signatureFormat",
                testTag = "signatureDetailFormat",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.signed_file_count_label,
                value = signedFileCount,
                contentDescription =
                    "${stringResource(
                        id = R.string.signed_file_count_label,
                    )}, $signedFileCount",
                testTag = "containerDetailSignedFileCount",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.signature_timestamp_label,
                value = timestampTime,
                contentDescription =
                    "${stringResource(
                        id = R.string.signature_timestamp_label,
                    )}, $timestampTime",
                testTag = "signatureDetailTimestamp",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.signature_timestamp_utc_label,
                value = timestampTimeUtc,
                contentDescription =
                    "${stringResource(
                        id = R.string.signature_timestamp_utc_label,
                    )}, $timestampTimeUtc",
                testTag = "signatureDetailTimestampUTC",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.hash_value_of_signature_label,
                value = hashValue,
                contentDescription =
                    "${stringResource(
                        id = R.string.hash_value_of_signature_label,
                    )}, $hashValue",
                testTag = "signatureDetailHashValue",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.ts_certificate_issuer_label,
                value = tsIssuerName,
                contentDescription =
                    if (tsIssuerName != null) {
                        "${stringResource(
                            id = R.string.ts_certificate_issuer_label,
                        )}, $tsIssuerName"
                    } else {
                        ""
                    },
                testTag = "signatureDetailTimestampCertificateIssuer",
            ),
            SignerDetailItem(
                label = R.string.ts_certificate_label,
                value = tsSubjectName,
                certificate = signature.timeStampCertificateDer.x509Certificate(),
                contentDescription =
                    if (tsSubjectName != null) {
                        "${stringResource(
                            id = R.string.ts_certificate_label,
                        )}, $tsSubjectName"
                    } else {
                        ""
                    },
                testTag = "signatureDetailTimestampCertificate",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.ocsp_certificate_issuer_label,
                value = ocspIssuerName,
                contentDescription =
                    if (ocspIssuerName != null) {
                        "${stringResource(
                            id = R.string.ocsp_certificate_issuer_label,
                        )}, $ocspIssuerName"
                    } else {
                        ""
                    },
                testTag = "signatureDetailOCSPCertificateIssuer",
            ),
            SignerDetailItem(
                label = R.string.ocsp_certificate_label,
                value = ocspSubjectName,
                certificate = signature.ocspCertificateDer.x509Certificate(),
                contentDescription =
                    if (ocspSubjectName != null) {
                        "${stringResource(
                            id = R.string.ocsp_certificate_label,
                        )}, $ocspSubjectName"
                    } else {
                        ""
                    },
                testTag = "signatureDetailOCSPCertificate",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.ocsp_time_label,
                value = ocspProducedAt,
                contentDescription =
                    "${stringResource(
                        id = R.string.ocsp_time_label,
                    )}, $ocspProducedAt",
                testTag = "signatureDetailOCSPTime",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.ocsp_time_utc_label,
                value = ocspProducedAtUtc,
                contentDescription =
                    "${stringResource(
                        id = R.string.ocsp_time_utc_label,
                    )}, $ocspProducedAtUtc",
                testTag = "signatureDetailOCSPTimeUTC",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.signers_mobile_time_label,
                value = signersMobileTime,
                contentDescription =
                    "${stringResource(
                        id = R.string.signers_mobile_time_label,
                    )}, $signersMobileTime",
                testTag = "signatureDetailSignersMobileTimeUTC",
            ),
        )
    }
}
