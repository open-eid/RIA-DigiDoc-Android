@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import ee.ria.DigiDoc.common.Constant.SignatureRequest.SIGNATURE_PROFILE_TS
import ee.ria.DigiDoc.utilsLib.certificate.CertificateUtil
import ee.ria.DigiDoc.utilsLib.text.TextUtil.removeEmptyStrings
import ee.ria.libdigidocpp.Container
import ee.ria.libdigidocpp.Signature
import ee.ria.libdigidocpp.StringVector
import org.bouncycastle.util.encoders.Base64
import java.nio.charset.StandardCharsets
import java.security.cert.CertificateException

class ContainerWrapper(containerPath: String?) {
    val container: Container = Container.open(containerPath)
    private lateinit var signature: Signature

    @Throws(CertificateException::class)
    fun prepareSignature(
        cert: String?,
        roleData: RoleData?,
    ): String {
        signature =
            if (roleData != null) {
                container.prepareWebSignature(
                    cert?.let { CertificateUtil.x509Certificate(it).encoded }, SIGNATURE_PROFILE_TS,
                    StringVector(removeEmptyStrings(roleData.roles)), roleData.city,
                    roleData.state, roleData.zip, roleData.country,
                )
            } else {
                container.prepareWebSignature(
                    cert?.let { CertificateUtil.x509Certificate(it).encoded },
                    SIGNATURE_PROFILE_TS,
                )
            }
        val dataToSignBytes: ByteArray =
            Base64.encode(
                signature.dataToSign(),
            )
        val dataToSign = String(dataToSignBytes, StandardCharsets.UTF_8)
        return removeWhitespaces(dataToSign)
    }

    fun finalizeSignature(signatureValue: String?) {
        val signatureValueBytes: ByteArray = Base64.decode(signatureValue)
        signature.setSignatureValue(signatureValueBytes)
        signature.extendSignatureProfile(SIGNATURE_PROFILE_TS)
        container.save()
    }

    private fun removeWhitespaces(text: String): String {
        return text.replace("\\s+".toRegex(), "")
    }
}
