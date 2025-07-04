@file:Suppress("PackageName", "ArrayInDataClass")

package ee.ria.DigiDoc.common.model

import ee.ria.DigiDoc.common.certificate.CertificateService
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyUsage
import java.io.IOException

data class ExtendedCertificate(
    val type: EIDType,
    val data: ByteArray,
    val keyUsage: KeyUsage,
    val extendedKeyUsage: ExtendedKeyUsage,
    val ellipticCurve: Boolean,
) {
    companion object {
        @Throws(IOException::class)
        fun create(
            data: ByteArray,
            certificateService: CertificateService,
        ): ExtendedCertificate {
            val certificate = certificateService.parseCertificate(data)
            val type = certificateService.extractEIDType(certificate)
            val ellipticCurve = certificateService.isEllipticCurve(certificate)

            val keyUsage = certificateService.extractKeyUsage(certificate)
            val extendedKeyUsage = certificateService.extractExtendedKeyUsage(certificate)

            return ExtendedCertificate(
                type,
                data,
                keyUsage,
                extendedKeyUsage,
                ellipticCurve,
            )
        }
    }
}
