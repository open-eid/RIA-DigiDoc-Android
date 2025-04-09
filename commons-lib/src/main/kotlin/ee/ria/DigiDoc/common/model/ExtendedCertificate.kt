@file:Suppress("PackageName")

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExtendedCertificate

        if (ellipticCurve != other.ellipticCurve) return false
        if (type != other.type) return false
        if (!data.contentEquals(other.data)) return false
        if (keyUsage != other.keyUsage) return false
        if (extendedKeyUsage != other.extendedKeyUsage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ellipticCurve.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + keyUsage.hashCode()
        result = 31 * result + extendedKeyUsage.hashCode()
        return result
    }
}
