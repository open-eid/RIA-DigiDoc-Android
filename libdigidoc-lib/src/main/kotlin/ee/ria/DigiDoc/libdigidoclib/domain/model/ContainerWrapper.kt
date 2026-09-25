// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.init.libdigidocppDispatcher
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.text.TextUtil.removeEmptyStrings
import ee.ria.libdigidocpp.ExternalSigner
import ee.ria.libdigidocpp.Signature
import ee.ria.libdigidocpp.StringVector
import kotlinx.coroutines.withContext
import java.security.cert.CertificateException

interface ContainerWrapper {
    @Throws(CertificateException::class)
    suspend fun prepareSignature(
        signer: ExternalSigner,
        signedContainer: SignedContainer?,
        cert: ByteArray?,
        roleData: RoleData?,
    ): ByteArray

    suspend fun finalizeSignature(
        signer: ExternalSigner,
        signedContainer: SignedContainer?,
        signatureArray: ByteArray,
    )
}

class ContainerWrapperImpl : ContainerWrapper {
    private lateinit var signature: Signature
    private val logTag = "Libdigidoc-ContainerWrapper"

    @Throws(CertificateException::class)
    override suspend fun prepareSignature(
        signer: ExternalSigner,
        signedContainer: SignedContainer?,
        cert: ByteArray?,
        roleData: RoleData?,
    ): ByteArray =
        withContext(libdigidocppDispatcher) {
            debugLog(logTag, "Preparing signature (with role data: ${roleData != null})")
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
            val dataToSign = signature.dataToSign()
            debugLog(logTag, "Signature prepared (${dataToSign.size} bytes to sign)")
            dataToSign
        }

    override suspend fun finalizeSignature(
        signer: ExternalSigner,
        signedContainer: SignedContainer?,
        signatureArray: ByteArray,
    ) = withContext(libdigidocppDispatcher) {
        signature.setSignatureValue(signatureArray)
        debugLog(logTag, "Extending signature profile (fetches OCSP confirmation and timestamp)")
        try {
            signature.extendSignatureProfile(signer)
        } catch (e: Exception) {
            errorLog(logTag, "Unable to extend signature profile: ${e.message}", e)
            throw e
        }
        signedContainer?.rawContainer()?.save()
        debugLog(logTag, "Signature finalized and container saved")
    }
}
