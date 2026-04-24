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

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R

sealed class CertificateListItem {
    data class Certificate(
        @param:StringRes val detailKey: Int = 0,
        val detailValue: String? = null,
        val testTag: String = "",
        val contentDescription: String = "",
        val formatForAccessibility: Boolean = false,
    ) : CertificateListItem()

    data class TextItem(
        val text: String,
        val testTag: String = "",
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
            CertificateListItem.TextItem(
                text = stringResource(id = subjectNameHeader),
                testTag = "certificateDetailSubjectDataTitle",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.country_or_region,
                detailValue = subjectCountryOrRegion,
                testTag = "certificateDetailCountry",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.organization,
                detailValue = subjectOrganization,
                testTag = "certificateDetailOrganization",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.organizational_unit,
                detailValue = subjectOrganizationalUnit,
                testTag = "certificateDetailOrganizationalUnit",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.common_name,
                detailValue = subjectCommonName,
                formatForAccessibility = true,
                testTag = "certificateDetailCommonName",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.surname,
                detailValue = subjectSurname,
                testTag = "certificateDetailSurname",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.given_name,
                detailValue = subjectGivenName,
                testTag = "certificateDetailGivenName",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.serial_number,
                detailValue = subjectSerialNumber,
                testTag = "certificateDetailSerialCode",
                formatForAccessibility = true,
            ),
            CertificateListItem.TextItem(
                text = stringResource(id = issuerNameHeader),
                testTag = "certificateDetailIssuerDataTitle",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.country_or_region,
                detailValue = issuerCountryOrRegion,
                testTag = "certificateDetailIssuerCountry",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.organization,
                detailValue = issuerOrganization,
                testTag = "certificateDetailIssuerOrganization",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.common_name,
                detailValue = issuerCommonName,
                testTag = "certificateDetailIssuerCommonName",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.email_address,
                detailValue = issuerEmailAddress,
                testTag = "certificateDetailIssuerEmail",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.other_name,
                detailValue = issuerOtherName,
                testTag = "certificateDetailIssuerOrganizationIdentifier",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.serial_number,
                detailValue = issuerSerialNumber,
                testTag = "certificateDetailSerialNumber",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.version,
                detailValue = issuerVersion,
                testTag = "certificateDetailVersion",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.signature_algorithm,
                detailValue = issuerSignatureAlgorithm,
                testTag = "certificateDetailSignatureAlgorithm",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.parameters,
                detailValue = issuerParameters,
                testTag = "certificateDetailSignatureParameters",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.not_valid_before,
                detailValue = issuerNotValidBefore,
                testTag = "certificateDetailNotValidBefore",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.not_valid_after,
                detailValue = issuerNotValidAfter,
                testTag = "certificateDetailNotValidAfter",
            ),
            CertificateListItem.TextItem(
                text = stringResource(id = publicKeyInfoHeader),
                testTag = "certificateDetailPublicKeyInfoTitle",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.algorithm,
                detailValue = publicKeyAlgorithm,
                testTag = "certificateDetailPublicKeyAlgorithm",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.parameters,
                detailValue = publicKeyParameters,
                testTag = "certificateDetailPublicKeyParameters",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.public_key,
                detailValue = publicKeyKey,
                testTag = "certificateDetailPublicKeyPK",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.key_usage,
                detailValue = publicKeyKeyUsage,
                testTag = "certificateDetailPublicKeyKeyUsage",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.signature,
                detailValue = publicKeySignature,
                testTag = "certificateDetailSignature",
            ),
            CertificateListItem.TextItem(
                text = stringResource(id = extensionsHeader),
                testTag = "certificateDetailExtensionsTitle",
            ),
            CertificateListItem.Certificate(
                detailKey = 0,
                detailValue = extensions,
                testTag = "certificateDetailExtensions",
            ),
            CertificateListItem.TextItem(
                text = stringResource(id = fingerprintsHeader),
                testTag = "certificateDetailFingerprintsTitle",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.sha_256,
                detailValue = fingerprintSha256,
                testTag = "certificateDetailFingerprintsSHA256",
            ),
            CertificateListItem.Certificate(
                detailKey = R.string.sha_1,
                detailValue = fingerprintSha1,
                testTag = "certificateDetailFingerprintsSHA1",
            ),
        )
}
