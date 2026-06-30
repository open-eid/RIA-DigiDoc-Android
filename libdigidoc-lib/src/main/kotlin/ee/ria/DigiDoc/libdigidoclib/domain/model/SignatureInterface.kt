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

@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import java.io.Serializable

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
}
