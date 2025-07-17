@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.certificate

import ee.ria.DigiDoc.common.model.EIDType
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.CertificatePolicies
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.cert.X509CertificateHolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateServiceImpl
    @Inject
    constructor() : CertificateService {
        override fun parseCertificate(data: ByteArray): X509CertificateHolder = X509CertificateHolder(data)

        override fun extractEIDType(certificate: X509CertificateHolder): EIDType {
            val extensions = certificate.extensions
            val certificatePolicies = CertificatePolicies.fromExtensions(extensions)
            return EIDType.parse(certificatePolicies)
        }

        override fun extractKeyUsage(certificate: X509CertificateHolder): KeyUsage {
            val extensions = certificate.extensions
            return KeyUsage.fromExtensions(extensions)
        }

        override fun extractExtendedKeyUsage(certificate: X509CertificateHolder): ExtendedKeyUsage {
            val extensions = certificate.extensions
            var extendedKeyUsage = ExtendedKeyUsage.fromExtensions(extensions)
            if (extendedKeyUsage == null) {
                extendedKeyUsage = ExtendedKeyUsage(arrayOf<KeyPurposeId?>())
            }

            return extendedKeyUsage
        }

        override fun extractFriendlyName(certificate: X509CertificateHolder): String {
            val rdNs = certificate.subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.CN))
            val commonName =
                rdNs[0]
                    .first.value
                    .toString()
                    .trim { it <= ' ' }

            val rdSNNs = certificate.subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.SURNAME))
            val rdGNNs = certificate.subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.GIVENNAME))
            val rdSERIALNs = certificate.subject.getRDNs(ASN1ObjectIdentifier.getInstance(BCStyle.SERIALNUMBER))

            // http://www.etsi.org/deliver/etsi_en/319400_319499/31941201/01.01.01_60/en_31941201v010101p.pdf
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
                    types.contains(
                        serialNR.substring(
                            0,
                            3,
                        ),
                    ) ||
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

        override fun isEllipticCurve(certificate: X509CertificateHolder): Boolean =
            certificate.subjectPublicKeyInfo.algorithm.algorithm
                .equals(X9ObjectIdentifiers.id_ecPublicKey)
    }
