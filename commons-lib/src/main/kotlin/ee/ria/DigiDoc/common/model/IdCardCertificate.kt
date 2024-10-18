@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.model

import org.bouncycastle.asn1.x509.CertificatePolicies
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.cert.X509CertificateHolder
import java.io.IOException

data class IdCardCertificate(
    val type: EIDType,
    val data: ByteArray,
    val ellipticCurve: Boolean,
) {
    companion object {
        @Throws(IOException::class)
        fun create(data: ByteArray): IdCardCertificate {
            val certificate = X509CertificateHolder(data)

            val extensions = certificate.extensions

            val certificatePolicies = CertificatePolicies.fromExtensions(extensions)
            val type = EIDType.parse(certificatePolicies)

            val ellipticCurve =
                certificate.subjectPublicKeyInfo.algorithm.algorithm
                    .equals(X9ObjectIdentifiers.id_ecPublicKey)

            return IdCardCertificate(
                type,
                data,
                ellipticCurve,
            )
        }
    }
}
