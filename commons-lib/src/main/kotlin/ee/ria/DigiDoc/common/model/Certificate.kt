@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.model

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.X509CertificateHolder
import java.io.IOException

data class Certificate(
    val friendlyName: String?,
) {
    companion object {
        @Throws(IOException::class)
        fun create(data: ByteArray): Certificate {
            val certificate = X509CertificateHolder(data)

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

            return Certificate(
                friendlyName,
            )
        }
    }
}
