@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.certificate

import ee.ria.DigiDoc.common.model.EIDType
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.X509CertificateHolder
import java.io.IOException

interface CertificateService {
    @Throws(IOException::class)
    fun parseCertificate(data: ByteArray): X509CertificateHolder

    fun extractEIDType(certificate: X509CertificateHolder): EIDType

    fun extractFriendlyName(certificate: X509CertificateHolder): String

    fun extractKeyUsage(certificate: X509CertificateHolder): KeyUsage

    fun extractExtendedKeyUsage(certificate: X509CertificateHolder): ExtendedKeyUsage

    fun isEllipticCurve(certificate: X509CertificateHolder): Boolean
}
