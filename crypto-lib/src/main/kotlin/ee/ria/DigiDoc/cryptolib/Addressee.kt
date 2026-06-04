/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.cdoc.Lock.parseLabel
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.PolicyInformation
import java.io.Serializable
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Date
import java.util.Objects

private const val LOG_TAG = "Addressee"

data class Addressee(
    val data: ByteArray,
    val identifier: String,
    val serialNumber: String?,
    val givenName: String?,
    val surname: String?,
    val certType: CertType,
    val validTo: Date?,
    val concatKDFAlgorithmURI: String?,
    val keyLabel: String? = null,
    val serverId: String? = null,
    val transactionId: String? = null,
    val lockLabel: String? = null,
    val lockType: String? = null,
    val lockIndex: Int? = null,
) : Serializable {
    companion object {
        fun fromCN(
            cn: String,
            sn: String,
            certType: CertType,
            validTo: Date?,
            data: ByteArray,
        ): Addressee {
            val split = cn.split(',').map { it.trim() }
            return if (split.size > 1) {
                Addressee(
                    data = data,
                    identifier = split[2],
                    serialNumber = sn,
                    givenName = split[1],
                    surname = split[0],
                    certType = certType,
                    validTo = validTo,
                    concatKDFAlgorithmURI = "",
                )
            } else {
                Addressee(
                    data = data,
                    identifier = cn,
                    serialNumber = sn,
                    givenName = null,
                    surname = null,
                    certType = certType,
                    validTo = validTo,
                    concatKDFAlgorithmURI = "",
                )
            }
        }

        fun fromCert(cert: ByteArray): Addressee =
            fromCN(
                cn = extractCNFromCertificate(cert),
                sn = extractSerialNumberFromCertificate(cert),
                certType = extractCertTypeFromCertificate(cert),
                validTo = extractValidToFromCertificate(cert),
                data = cert,
            )

        fun fromLabel(
            label: String,
            pub: ByteArray,
            concatKDFAlgorithmURI: String,
        ): Addressee {
            val info = parseLabel(label)
            val cn = info["cn"]
            val type = info["type"]
            val serverExp = info["server_exp"]
            val sn = info["serial_number"]

            val certType =
                when (type) {
                    "cert" -> CertType.IDCardType
                    "ID-card" -> CertType.IDCardType
                    "Digi-ID" -> CertType.DigiIDType
                    "Digi-ID E-RESIDENT" -> CertType.EResidentType
                    else -> CertType.UnknownType
                }

            val validTo = serverExp?.toLongOrNull()?.let { Date(it * 1000) }

            val split = cn?.split(',')?.map { it.trim() }
            val surname: String?
            val givenName: String?
            val identifier: String?

            if (split != null && split.size > 1) {
                surname = split[0]
                givenName = split[1]
                identifier = split[2]
            } else {
                surname = null
                givenName = null
                identifier = cn
            }

            return Addressee(
                data = pub,
                identifier = identifier ?: "",
                serialNumber = sn,
                givenName = givenName,
                surname = surname,
                certType = certType,
                validTo = validTo,
                concatKDFAlgorithmURI = concatKDFAlgorithmURI,
            )
        }

        private fun extractCNFromCertificate(cert: ByteArray): String =
            try {
                val certificate =
                    CertificateFactory
                        .getInstance("X.509")
                        .generateCertificate(cert.inputStream()) as X509Certificate
                val principal = certificate.subjectX500Principal

                val x500Name = X500Name.getInstance(principal.encoded)
                val cnAttributes = x500Name.getRDNs(BCStyle.CN)

                if (cnAttributes.isNotEmpty()) {
                    cnAttributes
                        .flatMap { rdn ->
                            rdn.typesAndValues.map { IETFUtils.valueToString(it.value) }
                        }.joinToString(",")
                } else {
                    ""
                }
            } catch (e: Exception) {
                errorLog(LOG_TAG, "Unable to extract CN from certificate", e)
                ""
            }

        private fun extractSerialNumberFromCertificate(cert: ByteArray): String =
            try {
                val certificate =
                    CertificateFactory
                        .getInstance("X.509")
                        .generateCertificate(cert.inputStream()) as X509Certificate
                val principal = certificate.subjectX500Principal

                val x500Name = X500Name.getInstance(principal.encoded)
                val serialNumberAttributes = x500Name.getRDNs(BCStyle.SERIALNUMBER)

                if (serialNumberAttributes.isNotEmpty()) {
                    serialNumberAttributes
                        .flatMap { rdn ->
                            rdn.typesAndValues.map { IETFUtils.valueToString(it.value) }
                        }.joinToString(",")
                } else {
                    ""
                }
            } catch (e: Exception) {
                errorLog(LOG_TAG, "Unable to extract serial number from certificate", e)
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

                            if (policyInfo.policyQualifiers != null) {
                                return certType(listOf(oid))
                            }
                        }
                    }
                }
                CertType.UnknownType
            } catch (e: Exception) {
                errorLog(LOG_TAG, "Unable to extract certificate type", e)
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
            } catch (e: Exception) {
                errorLog(LOG_TAG, "Unable to extract validTo from certificate", e)
                null
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Addressee) return false
        return data.contentEquals(other.data) &&
            identifier == other.identifier &&
            serialNumber == other.serialNumber &&
            givenName == other.givenName &&
            surname == other.surname &&
            certType == other.certType &&
            validTo == other.validTo &&
            concatKDFAlgorithmURI == other.concatKDFAlgorithmURI &&
            keyLabel == other.keyLabel &&
            serverId == other.serverId &&
            transactionId == other.transactionId &&
            lockLabel == other.lockLabel &&
            lockType == other.lockType
    }

    override fun hashCode(): Int =
        Objects.hash(
            data.contentHashCode(),
            identifier,
            serialNumber,
            givenName,
            surname,
            certType,
            validTo,
            concatKDFAlgorithmURI,
            keyLabel,
            serverId,
            transactionId,
            lockLabel,
            lockType,
        )
}
