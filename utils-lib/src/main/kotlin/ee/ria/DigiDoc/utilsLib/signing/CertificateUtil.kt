@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.signing

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

object CertificateUtil {
    @Throws(CertificateException::class)
    fun x509Certificate(certPem: String): X509Certificate =
        CertificateFactory
            .getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(certPem.toByteArray(StandardCharsets.UTF_8))) as X509Certificate

    @Throws(CertificateException::class)
    fun x509Certificate(certDer: ByteArray?): X509Certificate =
        CertificateFactory
            .getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(certDer)) as X509Certificate
}
