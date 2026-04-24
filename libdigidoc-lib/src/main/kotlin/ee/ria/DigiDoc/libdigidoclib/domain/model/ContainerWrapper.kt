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

import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.utilsLib.text.TextUtil.removeEmptyStrings
import ee.ria.libdigidocpp.ExternalSigner
import ee.ria.libdigidocpp.Signature
import ee.ria.libdigidocpp.StringVector
import java.security.cert.CertificateException

interface ContainerWrapper {
    @Throws(CertificateException::class)
    fun prepareSignature(
        signer: ExternalSigner,
        signedContainer: SignedContainer?,
        cert: ByteArray?,
        roleData: RoleData?,
    ): ByteArray

    fun finalizeSignature(
        signer: ExternalSigner,
        signedContainer: SignedContainer?,
        signatureArray: ByteArray,
    )
}

class ContainerWrapperImpl : ContainerWrapper {
    private lateinit var signature: Signature

    @Throws(CertificateException::class)
    override fun prepareSignature(
        signer: ExternalSigner,
        signedContainer: SignedContainer?,
        cert: ByteArray?,
        roleData: RoleData?,
    ): ByteArray {
        signature =
            when {
                roleData != null && signedContainer != null -> {
                    signer.setSignerRoles(StringVector(removeEmptyStrings(roleData.roles)))
                    signer.setSignatureProductionPlace(
                        roleData.city,
                        roleData.state,
                        roleData.zip,
                        roleData.country,
                    )
                    signedContainer.rawContainer()?.prepareSignature(
                        signer,
                    ) ?: throw IllegalStateException("Failed to prepare signature with role data")
                }
                signedContainer?.rawContainer() != null -> {
                    signedContainer.rawContainer()?.prepareSignature(
                        signer,
                    ) ?: throw IllegalStateException("Failed to prepare signature without role data")
                }
                else -> throw IllegalStateException("Unable to get container")
            }
        return signature.dataToSign()
    }

    override fun finalizeSignature(
        signer: ExternalSigner,
        signedContainer: SignedContainer?,
        signatureArray: ByteArray,
    ) {
        signature.setSignatureValue(signatureArray)
        signature.extendSignatureProfile(signer)
        signedContainer?.rawContainer()?.save()
    }
}
