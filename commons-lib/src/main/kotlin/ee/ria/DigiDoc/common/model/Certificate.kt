@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.model

import ee.ria.DigiDoc.common.certificate.CertificateService
import org.bouncycastle.cert.X509CertificateHolder
import java.io.IOException

data class Certificate(
    val friendlyName: String?,
) {
    companion object {
        @Throws(IOException::class)
        fun create(
            data: ByteArray,
            certificateService: CertificateService,
        ): Certificate {
            val certificate = certificateService.parseCertificate(data)
            val friendlyName = certificateService.extractFriendlyName(certificate)

            return Certificate(
                friendlyName,
            )
        }
    }
}
