@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import ee.ria.cdoc.Recipient.parseLabel
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.PolicyInformation
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Date

class Addressee(
    var data: ByteArray,
    var identifier: String,
    var serialNumber: String?,
    var givenName: String?,
    var surname: String?,
    var certType: CertType,
    var validTo: Date?,
    var concatKDFAlgorithmURI: String?,
) {
    constructor(cn: String, sn: String, certType: CertType, validTo: Date?, data: ByteArray) : this(
        data = data,
        identifier = "",
        serialNumber = "",
        givenName = null,
        surname = null,
        certType = certType,
        validTo = validTo,
        concatKDFAlgorithmURI = "",
    ) {
        val split = cn.split(',').map { it.trim() }
        if (split.size > 1) {
            surname = split[0]
            givenName = split[1]
            identifier = split[2]
        } else {
            surname = null
            givenName = null
            identifier = cn
        }
        serialNumber = sn
    }

    constructor(cert: ByteArray) : this(
        cn = extractCNFromCertificate(cert),
        sn = extractSerialNumberFromCertificate(cert),
        certType = extractCertTypeFromCertificate(cert),
        validTo = extractValidToFromCertificate(cert),
        data = cert,
    )

    constructor(label: String, pub: ByteArray, concatKDFAlgorithmURI: String) : this(
        data = pub,
        identifier = "",
        serialNumber = "",
        givenName = null,
        surname = null,
        certType = CertType.UnknownType,
        validTo = null,
        concatKDFAlgorithmURI = concatKDFAlgorithmURI,
    ) {
        val info = parseLabel(label)
        val cn = info["cn"]
        val type = info["type"]
        val serverExp = info["server_exp"]
        val sn = info["serial_number"]

        val certType =
            when (type) {
                "ID-card" -> CertType.IDCardType
                "Digi-ID" -> CertType.DigiIDType
                "Digi-ID E-RESIDENT" -> CertType.EResidentType
                else -> CertType.UnknownType
            }

        val validTo = serverExp?.toLongOrNull()?.let { Date(it * 1000) }

        val split = cn?.split(',')?.map { it.trim() }
        if (split != null) {
            if (split.size > 1) {
                this.surname = split[0]
                this.givenName = split[1]
                this.identifier = split[2]
            } else {
                this.surname = null
                this.givenName = null
                this.identifier = cn
            }
        }
        this.serialNumber = sn
        this.certType = certType
        this.validTo = validTo
        this.data = pub
        this.concatKDFAlgorithmURI = concatKDFAlgorithmURI
    }

    private companion object {
        private fun extractCNFromCertificate(cert: ByteArray): String =
            try {
                val certificate =
                    CertificateFactory
                        .getInstance("X.509")
                        .generateCertificate(cert.inputStream()) as X509Certificate
                val principal = certificate.subjectX500Principal

                // Use Bouncy Castle for proper DN parsing
                val x500Name = X500Name.getInstance(principal.encoded)
                val cnAttributes = x500Name.getRDNs(BCStyle.CN)

                if (cnAttributes.isNotEmpty()) {
                    // Get all CN values and join them with commas (like the Swift version)
                    cnAttributes
                        .flatMap { rdn ->
                            rdn.typesAndValues.map { IETFUtils.valueToString(it.value) }
                        }.joinToString(",")
                } else {
                    ""
                }
            } catch (_: Exception) {
                ""
            }

        private fun extractSerialNumberFromCertificate(cert: ByteArray): String =
            try {
                val certificate =
                    CertificateFactory
                        .getInstance("X.509")
                        .generateCertificate(cert.inputStream()) as X509Certificate
                val principal = certificate.subjectX500Principal

                // Use Bouncy Castle for proper DN parsing
                val x500Name = X500Name.getInstance(principal.encoded)
                val serialNumberAttributes = x500Name.getRDNs(BCStyle.SERIALNUMBER)

                if (serialNumberAttributes.isNotEmpty()) {
                    // Get all Serial number values and join them with commas
                    serialNumberAttributes
                        .flatMap { rdn ->
                            rdn.typesAndValues.map { IETFUtils.valueToString(it.value) }
                        }.joinToString(",")
                } else {
                    ""
                }
            } catch (_: Exception) {
                ""
            }

        private fun extractCertTypeFromCertificate(cert: ByteArray): CertType {
            return try {
                val certificate =
                    CertificateFactory
                        .getInstance("X.509")
                        .generateCertificate(cert.inputStream()) as X509Certificate

                val extensionValue = certificate.getExtensionValue(Extension.certificatePolicies.id)
                extensionValue?.let { ev ->
                    val octetString = ASN1OctetString.getInstance(ev)
                    ASN1InputStream(octetString.octets).use { ais ->
                        val seq = ASN1Sequence.getInstance(ais.readObject())

                        for (element in seq) {
                            val policyInfo = PolicyInformation.getInstance(element)
                            val oid = policyInfo.policyIdentifier.id

                            when {
                                oid.startsWith(OID.ID_CARD_POLICY_PREFIX) ||
                                    oid.startsWith(OID.ALTERNATE_ID_CARD_POLICY) ->
                                    return CertType.IDCardType

                                oid.startsWith(OID.DIGI_ID_POLICY_PREFIX) ||
                                    oid.startsWith(OID.ALTERNATE_DIGI_ID_POLICY1) ||
                                    oid.startsWith(OID.ALTERNATE_DIGI_ID_POLICY2) ->
                                    return CertType.DigiIDType

                                oid.startsWith(OID.MOBILE_ID_POLICY_PREFIX) ||
                                    oid.startsWith(OID.ALTERNATE_MOBILE_ID_POLICY) ->
                                    return CertType.MobileIDType

                                oid.startsWith(OID.ESEAL_POLICY_PREFIX1) ||
                                    oid.startsWith(OID.ESEAL_POLICY_PREFIX2) ||
                                    oid.startsWith(OID.ESEAL_POLICY_PREFIX3) ->
                                    return CertType.ESealType
                            }
                        }
                    }
                }
                CertType.UnknownType
            } catch (_: Exception) {
                CertType.UnknownType
            }
        }

        private fun extractValidToFromCertificate(cert: ByteArray): Date? =
            try {
                val certificate =
                    CertificateFactory
                        .getInstance("X.509")
                        .generateCertificate(cert.inputStream()) as X509Certificate
                certificate.notAfter
            } catch (_: Exception) {
                null
            }
    }
}
