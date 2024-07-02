@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.certificate

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R

sealed class CertificateListItem {
    data class Certificate(
        @StringRes val detailKey: Int = 0,
        val detailValue: String? = null,
        val contentDescription: String = "",
        val formatForAccessibility: Boolean = false,
    ) : CertificateListItem()

    data class TextItem(
        val text: String,
    ) : CertificateListItem()
}

class CertificateDetailItem {
    @Composable
    fun certificateDetailItems(
        @StringRes subjectNameHeader: Int,
        @StringRes issuerNameHeader: Int,
        @StringRes publicKeyInfoHeader: Int,
        @StringRes extensionsHeader: Int,
        @StringRes fingerprintsHeader: Int,
        subjectCountryOrRegion: String?,
        subjectOrganization: String?,
        subjectOrganizationalUnit: String?,
        subjectCommonName: String?,
        subjectSurname: String?,
        subjectGivenName: String?,
        subjectSerialNumber: String?,
        issuerCountryOrRegion: String?,
        issuerOrganization: String?,
        issuerCommonName: String?,
        issuerEmailAddress: String?,
        issuerOtherName: String?,
        issuerSerialNumber: String?,
        issuerVersion: String?,
        issuerSignatureAlgorithm: String?,
        issuerParameters: String?,
        issuerNotValidBefore: String?,
        issuerNotValidAfter: String?,
        publicKeyAlgorithm: String?,
        publicKeyParameters: String?,
        publicKeyKey: String?,
        publicKeyKeyUsage: String?,
        publicKeySignature: String?,
        extensions: String?,
        fingerprintSha256: String?,
        fingerprintSha1: String?,
    ): List<CertificateListItem> =
        listOf(
            CertificateListItem.TextItem(stringResource(id = subjectNameHeader)),
            CertificateListItem.Certificate(
                detailKey = R.string.country_or_region,
                detailValue = subjectCountryOrRegion,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.organization,
                detailValue = subjectOrganization,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.organizational_unit,
                detailValue = subjectOrganizationalUnit,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.common_name,
                detailValue = subjectCommonName,
                formatForAccessibility = true,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.surname,
                detailValue = subjectSurname,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.given_name,
                detailValue = subjectGivenName,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.serial_number,
                detailValue = subjectSerialNumber,
                formatForAccessibility = true,
            ),
            CertificateListItem.TextItem(stringResource(id = issuerNameHeader)),
            CertificateListItem.Certificate(
                detailKey = R.string.country_or_region,
                detailValue = issuerCountryOrRegion,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.organization,
                detailValue = issuerOrganization,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.common_name,
                detailValue = issuerCommonName,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.email_address,
                detailValue = issuerEmailAddress,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.other_name,
                detailValue = issuerOtherName,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.serial_number,
                detailValue = issuerSerialNumber,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.version,
                detailValue = issuerVersion,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.signature_algorithm,
                detailValue = issuerSignatureAlgorithm,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.parameters,
                detailValue = issuerParameters,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.not_valid_before,
                detailValue = issuerNotValidBefore,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.not_valid_after,
                detailValue = issuerNotValidAfter,
            ),
            CertificateListItem.TextItem(stringResource(id = publicKeyInfoHeader)),
            CertificateListItem.Certificate(
                detailKey = R.string.algorithm,
                detailValue = publicKeyAlgorithm,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.parameters,
                detailValue = publicKeyParameters,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.public_key,
                detailValue = publicKeyKey,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.key_usage,
                detailValue = publicKeyKeyUsage,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.signature,
                detailValue = publicKeySignature,
            ),
            CertificateListItem.TextItem(stringResource(id = extensionsHeader)),
            CertificateListItem.Certificate(
                detailKey = 0,
                detailValue = extensions,
            ),
            CertificateListItem.TextItem(stringResource(id = fingerprintsHeader)),
            CertificateListItem.Certificate(
                detailKey = R.string.sha_256,
                detailValue = fingerprintSha256,
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.sha_1,
                detailValue = fingerprintSha1,
            ),
        )
}
