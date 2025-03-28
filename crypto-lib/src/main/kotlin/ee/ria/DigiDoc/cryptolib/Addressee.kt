@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import ee.ria.DigiDoc.utilsLib.extensions.x509Certificate
import ee.ria.cdoc.Recipient.parseLabel
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.PolicyInformation
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Date
import java.util.Objects

class Addressee(
    var data: ByteArray,
    var identifier: String,
    var givenName: String?,
    var surname: String?,
    var certType: CertType,
    var validTo: Date?,
) {
    constructor(cn: String, certType: CertType, validTo: Date?, data: ByteArray) : this(
        data = data,
        identifier = "",
        givenName = null,
        surname = null,
        certType = certType,
        validTo = validTo,
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
    }

    constructor(cert: ByteArray) : this(
        cn = extractCNFromCertificate(cert),
        certType = extractCertTypeFromCertificate(cert),
        validTo = extractValidToFromCertificate(cert),
        data = cert,
    )

    constructor(label: String, pub: ByteArray) : this(
        data = pub,
        identifier = "",
        givenName = null,
        surname = null,
        certType = CertType.UnknownType,
        validTo = null,
    ) {
        val info = parseLabel(label)
        val cn = info["cn"]
        val type = info["type"]
        val serverExp = info["server_exp"]

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
        this.certType = certType
        this.validTo = validTo
        this.data = pub
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Addressee) return false

        return data.contentEquals(other.data) &&
            identifier == other.identifier &&
            givenName == other.givenName &&
            surname == other.surname &&
            certType == other.certType &&
            validTo == other.validTo
    }

    override fun hashCode(): Int {
        return Objects.hash(
            data.contentHashCode(),
            identifier,
            givenName,
            surname,
            certType,
            validTo,
        )
    }

    private companion object {
        private fun extractCNFromCertificate(cert: ByteArray): String {
            return try {
                val certificate =
                    CertificateFactory.getInstance("X.509")
                        .generateCertificate(cert.inputStream()) as X509Certificate
                val principal = certificate.subjectX500Principal
                val name = principal.name
                parseCommonName(name)
            } catch (e: Exception) {
                ""
            }
        }

        private fun parseCommonName(name: String): String {
            val attributes = name.split(",").map { it.trim() }
            for (attr in attributes) {
                if (attr.startsWith("CN=")) {
                    return attr.substringAfter("CN=")
                }
            }
            return ""
        }

        private fun extractCertTypeFromCertificate(cert: ByteArray): CertType {
            return try {
                val certificate = cert.x509Certificate()

                val extensionValue = certificate?.getExtensionValue(Extension.certificatePolicies.id)
                extensionValue?.let { ev ->
                    ASN1InputStream(ev.inputStream()).use { ais ->
                        val sequence =
                            ais.readObject()?.let {
                                PolicyInformation.getInstance(it)
                            } ?: return CertType.UnknownType

                        val oid = sequence.policyIdentifier.id
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

                            else -> {
                                return CertType.UnknownType
                            }
                        }
                    }
                }
                CertType.UnknownType
            } catch (e: Exception) {
                CertType.UnknownType
            }
        }

        private fun extractValidToFromCertificate(cert: ByteArray): Date? {
            return try {
                val certificate =
                    CertificateFactory.getInstance("X.509")
                        .generateCertificate(cert.inputStream()) as X509Certificate
                certificate.notAfter
            } catch (e: Exception) {
                null
            }
        }
    }
}
