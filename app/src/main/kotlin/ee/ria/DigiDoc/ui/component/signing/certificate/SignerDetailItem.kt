@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.certificate

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.utilsLib.container.NameUtil
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.extensions.hexString
import ee.ria.DigiDoc.utilsLib.extensions.x509Certificate
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import java.security.cert.X509Certificate

data class SignerDetailItem(
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
    fun signersDetailItems(
        sharedContainerViewModel: SharedContainerViewModel,
        signature: SignatureInterface,
        signerIssuerName: String?,
        tsIssuerName: String?,
        ocspIssuerName: String?,
        tsSubjectName: String?,
        ocspSubjectName: String?,
    ): List<SignerDetailItem> =
        listOf(
            SignerDetailItem(
                icon = 0,
                label = R.string.signer_certificate_issuer_label,
                value = signerIssuerName,
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.signer_certificate_issuer_label,
                        )} $value"
                    } else {
                        ""
                    },
                testTag = "signersCertificateIssuer",
            ),
            SignerDetailItem(
                label = R.string.signers_certificate_label,
                value = NameUtil.formatName(signature.signedBy),
                certificate = signature.signingCertificateDer.x509Certificate(),
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.signers_certificate_label,
                        )}, $value"
                    } else {
                        ""
                    },
                formatForAccessibility = true,
                testTag = "signersCertificate",
            ),
            SignerDetailItem(
                icon = R.drawable.ic_m3_open_in_new_48dp_wght400,
                isLink = true,
                label = R.string.signature_method_label,
                value = signature.signatureMethod,
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.signature_method_label,
                        )}, $value"
                    } else {
                        ""
                    },
                testTag = "signatureDetailMethod",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.container_format_label,
                value = sharedContainerViewModel.currentSignedContainer()?.containerMimetype() ?: "",
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.container_format_label,
                        )}, $value"
                    } else {
                        ""
                    },
                testTag = "containerDetailFormat",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.signature_format_label,
                value = signature.profile,
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.signature_format_label,
                        )}, $value"
                    } else {
                        ""
                    },
                testTag = "signatureDetailFormat",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.signed_file_count_label,
                value =
                    sharedContainerViewModel.signedContainer.value
                        ?.rawContainer()?.dataFiles()
                        ?.size
                        .toString(),
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.signed_file_count_label,
                        )}, $value"
                    } else {
                        ""
                    },
                testTag = "containerDetailSignedFileCount",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.signature_timestamp_label,
                value = DateUtil.getFormattedDateTime(signature.timeStampTime, false),
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.signature_timestamp_label,
                        )}, $value"
                    } else {
                        ""
                    },
                testTag = "signatureDetailTimestamp",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.signature_timestamp_utc_label,
                value = DateUtil.getFormattedDateTime(signature.timeStampTime, true),
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.signature_timestamp_utc_label,
                        )}, $value"
                    } else {
                        ""
                    },
                testTag = "signatureDetailTimestampUTC",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.hash_value_of_signature_label,
                value = signature.messageImprint.hexString(),
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.hash_value_of_signature_label,
                        )}, $value"
                    } else {
                        ""
                    },
                testTag = "signatureDetailHashValue",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.ts_certificate_issuer_label,
                value = tsIssuerName,
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.ts_certificate_issuer_label,
                        )}, $value"
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
                    if (value != null) {
                        "${stringResource(
                            id = R.string.ts_certificate_label,
                        )}, $value"
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
                    if (value != null) {
                        "${stringResource(
                            id = R.string.ocsp_certificate_issuer_label,
                        )}, $value"
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
                    if (value != null) {
                        "${stringResource(
                            id = R.string.ocsp_certificate_label,
                        )}, $value"
                    } else {
                        ""
                    },
                testTag = "signatureDetailOCSPCertificate",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.ocsp_time_label,
                value = DateUtil.getFormattedDateTime(signature.ocspProducedAt, false),
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.ocsp_time_label,
                        )}, $value"
                    } else {
                        ""
                    },
                testTag = "signatureDetailOCSPTime",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.ocsp_time_utc_label,
                value = DateUtil.getFormattedDateTime(signature.ocspProducedAt, true),
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.ocsp_time_utc_label,
                        )}, $value"
                    } else {
                        ""
                    },
                testTag = "signatureDetailOCSPTimeUTC",
            ),
            SignerDetailItem(
                icon = 0,
                label = R.string.signers_mobile_time_label,
                value = DateUtil.getFormattedDateTime(signature.claimedSigningTime, true),
                contentDescription =
                    if (value != null) {
                        "${stringResource(
                            id = R.string.signers_mobile_time_label,
                        )}, $value"
                    } else {
                        ""
                    },
                testTag = "signatureDetailSignersMobileTimeUTC",
            ),
        )
}
