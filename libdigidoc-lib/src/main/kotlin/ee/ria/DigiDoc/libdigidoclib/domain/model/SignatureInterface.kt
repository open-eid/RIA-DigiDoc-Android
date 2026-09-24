// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import java.io.Serializable
import java.util.Date

interface SignatureInterface : Serializable {
    val id: String
    val name: String
    val claimedSigningTime: String
    val trustedSigningTime: String
    val signatureMethod: String
    val dataToSign: ByteArray?
    val policy: String
    val spUri: String
    val profile: String
    val city: String
    val stateOrProvince: String
    val postalCode: String
    val countryName: String
    val signerRoles: List<String>
    val ocspProducedAt: String
    val timeStampTime: String
    val archiveTimeStampTime: String
    val streetAddress: String
    val signedBy: String
    val messageImprint: ByteArray
    val signingCertificateDer: ByteArray
    val ocspCertificateDer: ByteArray
    val timeStampCertificateDer: ByteArray
    val archiveTimeStampCertificateDer: ByteArray
    val isDigitalSeal: Boolean

    val validator: ValidatorInterface
    val validUntil: Date? get() = null
    val archiveTimeStamps: List<ArchiveTimestamp> get() = emptyList()
}
