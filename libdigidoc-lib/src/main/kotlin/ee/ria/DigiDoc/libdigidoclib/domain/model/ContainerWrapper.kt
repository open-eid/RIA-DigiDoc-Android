@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import ee.ria.DigiDoc.common.Constant.SignatureRequest.SIGNATURE_PROFILE_TS
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.utilsLib.extensions.removeWhitespaces
import ee.ria.DigiDoc.utilsLib.signing.CertificateUtil
import ee.ria.DigiDoc.utilsLib.text.TextUtil.removeEmptyStrings
import ee.ria.libdigidocpp.Container
import ee.ria.libdigidocpp.Signature
import ee.ria.libdigidocpp.StringVector
import org.bouncycastle.util.encoders.Base64
import java.nio.charset.StandardCharsets
import java.security.cert.CertificateException

interface ContainerWrapper {
    val container: Container?

    @Throws(CertificateException::class)
    fun prepareSignature(
        cert: String?,
        roleData: RoleData?,
    ): String

    fun finalizeSignature(signatureValue: String?)
}

class ContainerWrapperImpl : ContainerWrapper {
    override val container = SignedContainer.rawContainer()
    private lateinit var signature: Signature

    @Throws(CertificateException::class)
    override fun prepareSignature(
        cert: String?,
        roleData: RoleData?,
    ): String {
        signature =
            if (roleData != null && container != null) {
                container.prepareWebSignature(
                    cert?.let { CertificateUtil.x509Certificate(it).encoded }, SIGNATURE_PROFILE_TS,
                    StringVector(removeEmptyStrings(roleData.roles)), roleData.city,
                    roleData.state, roleData.zip, roleData.country,
                )
            } else if (container != null) {
                container.prepareWebSignature(
                    cert?.let { CertificateUtil.x509Certificate(it).encoded },
                    SIGNATURE_PROFILE_TS,
                )
            } else {
                throw IllegalStateException("Unable to get container")
            }
        val dataToSignBytes: ByteArray =
            Base64.encode(
                signature.dataToSign(),
            )
        val dataToSign = String(dataToSignBytes, StandardCharsets.UTF_8)
        return dataToSign.removeWhitespaces()
    }

    override fun finalizeSignature(signatureValue: String?) {
        val signatureValueBytes: ByteArray = Base64.decode(signatureValue)
        signature.setSignatureValue(signatureValueBytes)
        signature.extendSignatureProfile(SIGNATURE_PROFILE_TS)
        container?.save()
    }
}
