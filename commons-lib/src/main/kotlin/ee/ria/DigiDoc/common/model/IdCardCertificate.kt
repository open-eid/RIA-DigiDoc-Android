@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.model

import ee.ria.DigiDoc.common.certificate.CertificateService
import java.io.IOException

data class IdCardCertificate(
    val type: EIDType,
    val data: ByteArray,
    val ellipticCurve: Boolean,
) {
    companion object {
        @Throws(IOException::class)
        fun create(
            data: ByteArray,
            certificateService: CertificateService,
        ): IdCardCertificate {
            val certificate = certificateService.parseCertificate(data)
            val type = certificateService.extractEIDType(certificate)
            val ellipticCurve = certificateService.isEllipticCurve(certificate)

            return IdCardCertificate(
                type,
                data,
                ellipticCurve,
            )
        }
    }
}
