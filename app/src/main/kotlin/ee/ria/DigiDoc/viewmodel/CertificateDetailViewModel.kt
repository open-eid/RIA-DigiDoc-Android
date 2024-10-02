@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.utilsLib.extensions.formatHexString
import ee.ria.DigiDoc.utilsLib.extensions.hexString
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import org.apache.commons.text.WordUtils
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.util.encoders.Hex
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.PublicKey
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import javax.inject.Inject

@HiltViewModel
class CertificateDetailViewModel
    @Inject
    constructor() : ViewModel() {
        private val logTag = "CertificateDetailViewModel"

        fun certificateToJcaX509(certificate: X509Certificate?): JcaX509CertificateHolder? {
            if (certificate == null) {
                return null
            }

            return try {
                JcaX509CertificateHolder(certificate)
            } catch (e: CertificateEncodingException) {
                errorLog(logTag, "Unable to encode certificate", e)
                null
            }
        }

        fun getRDNValue(
            x500Name: X500Name,
            asn1ObjectIdentifier: ASN1ObjectIdentifier,
        ): String {
            val rdns = x500Name.getRDNs(asn1ObjectIdentifier)
            return if (rdns.isNotEmpty()) {
                IETFUtils.valueToString(rdns.first().first.value)
            } else {
                ""
            }
        }

        fun addLeadingZeroToHex(hexString: String?): String {
            if (hexString.isNullOrEmpty()) {
                return ""
            }

            return if (hexString.length % 2 != 0) {
                "0$hexString"
            } else {
                hexString
            }
        }

        fun getPublicKeyString(pk: PublicKey): String =
            when (pk) {
                is RSAPublicKey -> {
                    val modulus: BigInteger = pk.modulus
                    modulus.toString(16).formatHexString()
                }
                is ECPublicKey -> {
                    val x = pk.w.affineX.toString(16)
                    val y = pk.w.affineY.toString(16)
                    (x + y).formatHexString()
                }
                else -> Hex.toHexString(pk.encoded)
            }

        fun getKeyUsages(certificateKeyUsages: BooleanArray?): String =
            certificateKeyUsages
                ?.asList()
                ?.mapIndexedNotNull { index, usage ->
                    if (usage) getKeyUsageDescription(index) else null
                }?.joinToString(", ") ?: ""

        fun getExtensionsData(
            certificateHolder: JcaX509CertificateHolder,
            certificate: X509Certificate,
        ): String {
            return buildString {
                certificateHolder.extensions.extensionOIDs.forEach { oid ->
                    val extension = certificateHolder.extensions.getExtension(oid)
                    append(
                        """
                        Extension
                        ${getExtensionName(extension)} ( $oid )
                        ${indentText("Critical:")} ${extension.isCritical} ${"\n"}
                        """.trimIndent(),
                    )

                    try {
                        val extensionValue = certificate.getExtensionValue(oid.id)
                        if (extensionValue != null) {
                            val parsedValue = JcaX509ExtensionUtils.parseExtensionValue(extensionValue)
                            val eVs = parsedValue.toString().split(",")
                            eVs.forEach { ext ->
                                append("${indentText(getIDOrURIString(ext.trim()))} \n\n")
                            }
                        }
                    } catch (ioe: IOException) {
                        errorLog(logTag, "Unable to parse extension value", ioe)
                        return ""
                    }
                }
            }.replace("[", "").replace("]", "")
        }

        private fun getExtensionName(extension: Extension): String {
            val extnId = extension.extnId.id
            return getExtensionFields()
                .entries
                .firstOrNull { it.value == extnId }
                ?.key
                ?: ""
        }

        fun getCertificateSHA256Fingerprint(certificate: X509Certificate): String =
            try {
                val sha256 = MessageDigest.getInstance("SHA-256")
                sha256.digest(certificate.encoded).hexString().uppercase()
            } catch (e: Exception) {
                errorLog(logTag, "Unable to get SHA256 digest", e)
                ""
            }

        fun getCertificateSHA1Fingerprint(certificate: X509Certificate): String =
            try {
                val sha1 = MessageDigest.getInstance("SHA-1")
                sha1.digest(certificate.encoded).hexString().uppercase()
            } catch (e: Exception) {
                errorLog(logTag, "Unable to get SHA1 digest", e)
                ""
            }

        fun isValidParametersData(params: String): Boolean {
            for (character in params) {
                if (!character.isISOControl()) {
                    return true
                }
            }
            return false
        }

        private fun wrapText(text: String): String {
            return WordUtils.wrap(text, 40, "\n${indentText("")}", true)
        }

        private fun indentText(
            text: String,
            padding: Int = 4,
        ): String = " ".repeat(padding) + text

        private fun getIDOrURIString(extensionValue: String): String {
            return when {
                URLUtil.isValidUrl(extensionValue) -> {
                    "URI: ${wrapText(extensionValue)}"
                }
                extensionValue.contains("#") -> {
                    val extracted = extensionValue.split("#")
                    "ID: ${wrapText(extracted[1].formatHexString())}"
                }
                else -> {
                    if (extensionValue.isEmpty()) {
                        ""
                    } else {
                        wrapText(extensionValue)
                    }
                }
            }
        }

        private fun getKeyUsageDescription(keyUsageNum: Int): String {
            return when (keyUsageNum) {
                0 -> "Digital Signature"
                1 -> "Non-Repudiation"
                2 -> "Key Encipherment"
                3 -> "Data Encipherment"
                4 -> "Key Agreement"
                5 -> "Key Cert Sign"
                6 -> "cRL Sign"
                7 -> "Encipher Only"
                8 -> "Decipher Only"
                else -> ""
            }
        }

        private fun getExtensionFields(): Map<String, String> {
            val extensionFields: MutableMap<String, String> = HashMap()

            extensionFields["subjectDirectoryAttributes"] = "2.5.29.9"
            extensionFields["subjectKeyIdentifier"] = "2.5.29.14"
            extensionFields["keyUsage"] = "2.5.29.15"
            extensionFields["privateKeyUsagePeriod"] = "2.5.29.16"
            extensionFields["subjectAlternativeName"] = "2.5.29.17"
            extensionFields["issuerAlternativeName"] = "2.5.29.18"
            extensionFields["basicConstraints"] = "2.5.29.19"
            extensionFields["cRLNumber"] = "2.5.29.20"
            extensionFields["reasonCode"] = "2.5.29.21"
            extensionFields["instructionCode"] = "2.5.29.23"
            extensionFields["invalidityDate"] = "2.5.29.24"
            extensionFields["deltaCRLIndicator"] = "2.5.29.27"
            extensionFields["issuingDistributionPoint"] = "2.5.29.28"
            extensionFields["certificateIssuer"] = "2.5.29.29"
            extensionFields["nameConstraints"] = "2.5.29.30"
            extensionFields["cRLDistributionPoints"] = "2.5.29.31"
            extensionFields["certificatePolicies"] = "2.5.29.32"
            extensionFields["policyMappings"] = "2.5.29.33"
            extensionFields["authorityKeyIdentifier"] = "2.5.29.35"
            extensionFields["policyConstraints"] = "2.5.29.36"
            extensionFields["extendedKeyUsage"] = "2.5.29.37"
            extensionFields["freshestCRL"] = "2.5.29.46"
            extensionFields["inhibitAnyPolicy"] = "2.5.29.54"
            extensionFields["authorityInfoAccess"] = "1.3.6.1.5.5.7.1.1"
            extensionFields["subjectInfoAccess"] = "1.3.6.1.5.5.7.1.11"
            extensionFields["logoType"] = "1.3.6.1.5.5.7.1.12"
            extensionFields["biometricInfo"] = "1.3.6.1.5.5.7.1.2"
            extensionFields["qCStatements"] = "1.3.6.1.5.5.7.1.3"
            extensionFields["auditIdentity"] = "1.3.6.1.5.5.7.1.4"
            extensionFields["noRevAvail"] = "2.5.29.56"
            extensionFields["targetInformation"] = "2.5.29.55"
            extensionFields["expiredCertsOnCRL"] = "2.5.29.60"

            return extensionFields
        }
    }
