@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import ee.ria.DigiDoc.common.Constant.SignatureRequest.SIGNATURE_PROFILE_TS
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.utilsLib.extensions.removeWhitespaces
import ee.ria.DigiDoc.utilsLib.signing.CertificateUtil
import ee.ria.DigiDoc.utilsLib.text.TextUtil.removeEmptyStrings
import ee.ria.libdigidocpp.Signature
import ee.ria.libdigidocpp.StringVector
import org.bouncycastle.util.encoders.Base64
import java.nio.charset.StandardCharsets
import java.security.cert.CertificateException

interface ContainerWrapper {
    @Throws(CertificateException::class)
    fun prepareSignature(
        signedContainer: SignedContainer?,
        cert: ByteArray?,
        roleData: RoleData?,
    ): ByteArray

    @Throws(CertificateException::class)
    fun prepareSignature(
        signedContainer: SignedContainer?,
        cert: String?,
        roleData: RoleData?,
    ): String

    fun finalizeSignature(
        signedContainer: SignedContainer?,
        signatureArray: ByteArray,
    )

    fun finalizeSignature(
        signedContainer: SignedContainer?,
        signatureValue: String?,
    )
}

class ContainerWrapperImpl : ContainerWrapper {
    private lateinit var signature: Signature

    @Throws(CertificateException::class)
    override fun prepareSignature(
        signedContainer: SignedContainer?,
        cert: ByteArray?,
        roleData: RoleData?,
    ): ByteArray {
        signature =
            when {
                roleData != null && signedContainer != null -> {
                    signedContainer.rawContainer()?.prepareWebSignature(
                        cert,
                        SIGNATURE_PROFILE_TS,
                        StringVector(removeEmptyStrings(roleData.roles)),
                        roleData.city,
                        roleData.state,
                        roleData.zip,
                        roleData.country,
                    ) ?: throw IllegalStateException("Failed to prepare signature with role data")
                }
                signedContainer?.rawContainer() != null -> {
                    signedContainer.rawContainer()?.prepareWebSignature(
                        cert,
                        SIGNATURE_PROFILE_TS,
                    ) ?: throw IllegalStateException("Failed to prepare signature without role data")
                }
                else -> throw IllegalStateException("Unable to get container")
            }
        return signature.dataToSign()
    }

    @Throws(CertificateException::class)
    override fun prepareSignature(
        signedContainer: SignedContainer?,
        cert: String?,
        roleData: RoleData?,
    ): String {
        val dataToSignBytes =
            Base64.encode(
                prepareSignature(
                    signedContainer,
                    cert?.let {
                        CertificateUtil.x509Certificate(it).encoded
                    },
                    roleData,
                ),
            )
        val dataToSign = String(dataToSignBytes, StandardCharsets.UTF_8)
        dataToSign.removeWhitespaces()
        return dataToSign
    }

    override fun finalizeSignature(
        signedContainer: SignedContainer?,
        signatureValue: String?,
    ) {
        val signatureValueBytes: ByteArray = Base64.decode(signatureValue)
        finalizeSignature(signedContainer, signatureValueBytes)
    }

    override fun finalizeSignature(
        signedContainer: SignedContainer?,
        signatureArray: ByteArray,
    ) {
        signature.setSignatureValue(signatureArray)
        signature.extendSignatureProfile(SIGNATURE_PROFILE_TS)
        signedContainer?.rawContainer()?.save()
    }
}
