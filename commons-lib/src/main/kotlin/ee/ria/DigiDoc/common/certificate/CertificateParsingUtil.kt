// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.certificate

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.X509CertificateHolder
import java.security.cert.X509Certificate

object CertificateParsingUtil {
    fun extractFriendlyName(certificate: X509CertificateHolder): String = extractFriendlyName(certificate.subject)

    fun personalData(cert: X509Certificate): PersonalData =
        personalData(X500Name.getInstance(cert.subjectX500Principal.encoded))

    private fun extractFriendlyName(subject: X500Name): String {
        val rdNs = subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.CN))
        val commonName =
            rdNs[0]
                .first.value
                .toString()
                .trim { it <= ' ' }

        val rdSNNs = subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.SURNAME))
        val rdGNNs = subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.GIVENNAME))
        val rdSERIALNs = subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.SERIALNUMBER))

        val types: List<String> = mutableListOf("PAS", "IDC", "PNO", "TAX", "TIN")
        var serialNR =
            if (rdSERIALNs.isEmpty()) {
                ""
            } else {
                rdSERIALNs[0]
                    .first.value
                    .toString()
                    .trim { it <= ' ' }
            }

        if (serialNR.length > 6 &&
            (
                types.contains(serialNR.substring(0, 3)) ||
                    serialNR[2] == ':'
            ) &&
            serialNR[5] == '-'
        ) {
            serialNR = serialNR.substring(6)
        }

        return if (rdSNNs.isEmpty() || rdGNNs.isEmpty()) {
            commonName
        } else {
            rdSNNs[0]
                .first.value
                .toString()
                .trim { it <= ' ' } + "," +
                rdGNNs[0]
                    .first.value
                    .toString()
                    .trim { it <= ' ' } + "," + serialNR
        }
    }

    private fun personalData(subject: X500Name): PersonalData {
        val friendlyName = extractFriendlyName(subject)
        val parts = friendlyName.split(",").map { it.trim() }

        require(parts.size >= 3) {
            "Unexpected signing certificate subject format"
        }

        return PersonalData(
            surname = parts[0],
            givenNames = parts[1],
            personalCode = parts[2],
        )
    }
}

data class PersonalData(
    val surname: String,
    val givenNames: String,
    val personalCode: String,
)
