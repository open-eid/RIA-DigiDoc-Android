@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import okio.ByteString
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.CertificatePolicies
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.cert.X509CertificateHolder
import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.Instant

data class Certificate(
    val type: EIDType?,
    val commonName: String?,
    val friendlyName: String?,
    val notAfter: Instant?,
    val ellipticCurve: Boolean,
    val keyUsage: KeyUsage?,
    val extendedKeyUsage: ExtendedKeyUsage?,
    val data: ByteString?,
) {
    fun expired(): Boolean {
        return notAfter?.isBefore(Instant.now()) ?: false
    }

    @Throws(CertificateException::class)
    fun x509Certificate(): X509Certificate {
        return CertificateFactory.getInstance("X.509")
            .generateCertificate(
                ByteArrayInputStream(
                    data?.toByteArray() ?: byteArrayOf(),
                ),
            ) as X509Certificate
    }

    companion object {
        @Throws(IOException::class)
        fun create(data: ByteString): Certificate {
            val certificate = X509CertificateHolder(data.toByteArray())
            val extensions = certificate.extensions

            val certificatePolicies = CertificatePolicies.fromExtensions(extensions)
            val type: EIDType = EIDType.parse(certificatePolicies)

            val rdNs = certificate.subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.CN))
            val commonName = rdNs[0].first.value.toString().trim { it <= ' ' }

            val rdSNNs =
                certificate.subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.SURNAME))
            val rdGNNs =
                certificate.subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.GIVENNAME))
            val rdSERIALNs =
                certificate.subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.SERIALNUMBER))

            // http://www.etsi.org/deliver/etsi_en/319400_319499/31941201/01.01.01_60/en_31941201v010101p.pdf
            val types: List<String> = mutableListOf("PAS", "IDC", "PNO", "TAX", "TIN")
            var serialNR =
                if (rdSERIALNs.isEmpty()) {
                    ""
                } else {
                    rdSERIALNs[0].first.value.toString()
                        .trim { it <= ' ' }
                }
            if (serialNR.length > 6 && (
                    types.contains(
                        serialNR.substring(
                            0,
                            3,
                        ),
                    ) || serialNR[2] == ':'
                ) && serialNR[5] == '-'
            ) {
                serialNR = serialNR.substring(6)
            }

            val friendlyName =
                if (rdSNNs.isEmpty() || rdGNNs.isEmpty()) {
                    commonName
                } else {
                    rdSNNs[0].first.value.toString()
                        .trim { it <= ' ' } + "," +
                        rdGNNs[0].first.value.toString().trim { it <= ' ' } + "," + serialNR
                }

            val notAfter = Instant.ofEpochMilli(certificate.notAfter.time)

            val ellipticCurve =
                certificate.subjectPublicKeyInfo.algorithm.algorithm
                    .equals(X9ObjectIdentifiers.id_ecPublicKey)

            val keyUsage = KeyUsage.fromExtensions(extensions)

            var extendedKeyUsage = ExtendedKeyUsage.fromExtensions(extensions)
            if (extendedKeyUsage == null) {
                extendedKeyUsage = ExtendedKeyUsage(arrayOf())
            }

            return Certificate(
                type,
                commonName,
                friendlyName,
                notAfter,
                ellipticCurve,
                keyUsage,
                extendedKeyUsage,
                data,
            )
        }
    }
}
