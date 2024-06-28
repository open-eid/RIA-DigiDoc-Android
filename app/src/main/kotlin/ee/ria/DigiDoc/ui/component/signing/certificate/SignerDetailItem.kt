@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.certificate

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
import java.security.cert.X509Certificate

data class SignerDetailItem(
    @StringRes val label: Int = 0,
    val value: String? = null,
    val certificate: X509Certificate? = null,
    val contentDescription: String = "",
) {
    @Composable
    fun signersDetailItems(
        signature: SignatureInterface,
        signerIssuerName: String?,
        tsIssuerName: String?,
        ocspIssuerName: String?,
        tsSubjectName: String?,
        ocspSubjectName: String?,
    ): List<SignerDetailItem> =
        listOf(
            SignerDetailItem(
                label = R.string.signer_certificate_issuer_label,
                value = signerIssuerName,
                contentDescription = stringResource(id = R.string.signer_certificate_issuer_label),
            ),
            SignerDetailItem(
                label = R.string.signers_certificate_label,
                value = NameUtil.formatName(signature.signedBy),
                certificate = signature.signingCertificateDer.x509Certificate(),
                contentDescription = stringResource(id = R.string.signers_certificate_label),
            ),
            SignerDetailItem(
                label = R.string.signature_method_label,
                value = signature.signatureMethod,
                contentDescription = stringResource(id = R.string.signature_method_label),
            ),
            SignerDetailItem(
                label = R.string.container_format_label,
                value = SignedContainer.containerMimetype() ?: "",
                contentDescription = stringResource(id = R.string.container_format_label),
            ),
            SignerDetailItem(
                label = R.string.signature_format_label,
                value = signature.profile,
                contentDescription = stringResource(id = R.string.signature_format_label),
            ),
            SignerDetailItem(
                label = R.string.signed_file_count_label,
                value = SignedContainer.container().getDataFiles().size.toString(),
                contentDescription = stringResource(id = R.string.signed_file_count_label),
            ),
            SignerDetailItem(
                label = R.string.signature_timestamp_label,
                value = DateUtil.getFormattedDateTime(signature.timeStampTime, false),
                contentDescription = stringResource(id = R.string.signature_timestamp_label),
            ),
            SignerDetailItem(
                label = R.string.signature_timestamp_utc_label,
                value = DateUtil.getFormattedDateTime(signature.timeStampTime, true),
                contentDescription = stringResource(id = R.string.signature_timestamp_utc_label),
            ),
            SignerDetailItem(
                label = R.string.hash_value_of_signature_label,
                value = signature.messageImprint.hexString(),
                contentDescription = stringResource(id = R.string.hash_value_of_signature_label),
            ),
            SignerDetailItem(
                label = R.string.ts_certificate_issuer_label,
                value = tsIssuerName,
                contentDescription = stringResource(id = R.string.ts_certificate_issuer_label),
            ),
            SignerDetailItem(
                label = R.string.ts_certificate_label,
                value = tsSubjectName,
                certificate = signature.timeStampCertificateDer.x509Certificate(),
                contentDescription = stringResource(id = R.string.ts_certificate_label),
            ),
            SignerDetailItem(
                label = R.string.ocsp_certificate_issuer_label,
                value = ocspIssuerName,
                contentDescription = stringResource(id = R.string.ocsp_certificate_issuer_label),
            ),
            SignerDetailItem(
                label = R.string.ocsp_certificate_label,
                value = ocspSubjectName,
                certificate = signature.ocspCertificateDer.x509Certificate(),
                contentDescription = stringResource(id = R.string.ocsp_certificate_label),
            ),
            SignerDetailItem(
                label = R.string.ocsp_time_label,
                value = DateUtil.getFormattedDateTime(signature.ocspProducedAt, false),
                contentDescription = stringResource(id = R.string.ocsp_time_label),
            ),
            SignerDetailItem(
                label = R.string.ocsp_time_utc_label,
                value = DateUtil.getFormattedDateTime(signature.ocspProducedAt, true),
                contentDescription = stringResource(id = R.string.ocsp_time_utc_label),
            ),
            SignerDetailItem(
                label = R.string.signers_mobile_time_label,
                value = DateUtil.getFormattedDateTime(signature.claimedSigningTime, true),
                contentDescription = stringResource(id = R.string.signers_mobile_time_label),
            ),
        )
}
