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
                        roleData.city, roleData.state,
                        roleData.zip, roleData.country,
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
