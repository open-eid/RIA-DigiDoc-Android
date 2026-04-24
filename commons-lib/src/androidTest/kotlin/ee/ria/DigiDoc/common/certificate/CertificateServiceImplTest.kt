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

package ee.ria.DigiDoc.common.certificate

import ee.ria.DigiDoc.common.model.EIDType
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.AttributeTypeAndValue
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.CertificatePolicies
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.asn1.x509.PolicyInformation
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Base64
import java.util.Date

class CertificateServiceImplTest {
    private lateinit var certificateService: CertificateServiceImpl

    private val testCertificate =
        "MIIEMjCCAxqgAwIBAgIUKkNd+6/QiWFBaLFF2sQyqKfCTAAwDQYJKoZIhvcNAQELBQAwgasxCzAJBgNVBAYTAkV" +
            "FMRMwEQYDVQQIDApUZXN0IFN0YXRlMRIwEAYDVQQHDAlUZXN0IENpdHkxGjAYBgNVBAoMEVRlc3QgT3" +
            "JnYW5pemF0aW9uMR8wHQYDVQQLDBZUZXN0IE9yZ2FuaXphdGlvbiBVbml0MRUwEwYDVQQDDAxUZXN0I" +
            "FN1YmplY3QxHzAdBgkqhkiG9w0BCQEWEHRlc3RAZXhhbXBsZS5jb20wIBcNMjUxMDIyMjE0MTAwWhgP" +
            "MjA1MDEwMjIyMTQxMDBaMIGrMQswCQYDVQQGEwJFRTETMBEGA1UECAwKVGVzdCBTdGF0ZTESMBAGA1U" +
            "EBwwJVGVzdCBDaXR5MRowGAYDVQQKDBFUZXN0IE9yZ2FuaXphdGlvbjEfMB0GA1UECwwWVGVzdCBPcm" +
            "dhbml6YXRpb24gVW5pdDEVMBMGA1UEAwwMVGVzdCBTdWJqZWN0MR8wHQYJKoZIhvcNAQkBFhB0ZXN0Q" +
            "GV4YW1wbGUuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo0QIdu0CrGQofL2GmM4c" +
            "ZUbSStgEeJbQu+5sv50DU/32JKwjFXcZz2rc/aqgRL4FGcsBmCbahA2DwxRcdCvacW3dSyk1Z+mVIbv" +
            "O7Z5UakElxBpffS7ATT2EPsNpxE6SBoUpfTn+vujfhnVEi7jLZmdci1WOYQEgIArzCrIC2Krw5sVdHt" +
            "zFvbO4vjxIUemZYeyIXd/NDgU3Ejb8+PQBVgtQGS9aj75bse2NI/Ti9Rw1sQOSqiElGt46EEUvnrMpu" +
            "5VQwkGPqAtpx42LJ0VSV2J9r7FmLMQq13W0wu8KozvWSVleDg84IavcMBrJQk2koAa4Dw6apqx/DRC8" +
            "7pyh/wIDAQABo0owSDAnBgNVHREEIDAeggp0ZXN0LmxvY2FsghB0ZXN0LmV4YW1wbGUuY29tMB0GA1U" +
            "dDgQWBBQiStqNRiLVTt2I2lVNZR6zOlqY2zANBgkqhkiG9w0BAQsFAAOCAQEAiC2O3DNDbniZX2x8xf" +
            "eYsPmLijULU20m0xcbAlZNCPTypq34fBBYsgsSf+wshGQ236YQD89PKJ0ydPJ5VOvhH/P8tGGCHqa2t" +
            "uA2rOTJX4NzzgGCwTQdYH4Q/e5/xY8ds6mhb55b3SRIptgr3IkHn/bBfeBSFYy12oymU4HQmhnxFhRN" +
            "CEFl7Cp67aXUjLSc6ctF/S+m39Cfgm7qR7y5zkh9UiB4jBpziD3AzBRtFhEFUchNN72rWVQEBMXsNiI" +
            "oJQBGYusyaHOsoOQBphI1jFIAy1Yh2jlYR48/8QK31dEjfOq8/ZTlq2pbAx6i5TGjws9+pEhTvvsS7F" +
            "A5604YYg=="

    @Before
    fun setUp() {
        certificateService = CertificateServiceImpl()
    }

    @Test
    fun certificateService_parseCertificate_success() {
        val validCertificateData: ByteArray = Base64.getDecoder().decode(testCertificate)

        val result = certificateService.parseCertificate(validCertificateData)

        Assert.assertNotNull(result)
        Assert.assertTrue(
            result.isValidOn(
                Date.from(
                    LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant(),
                ),
            ),
        )
    }

    @Test(expected = IOException::class)
    fun certificateService_parseCertificate_throwIOExceptionForInvalidCertificateData() {
        val invalidCertificateData: ByteArray = byteArrayOf(0x00, 0x01, 0x02, 0x03)

        certificateService.parseCertificate(invalidCertificateData)
    }

    @Test
    fun certificateService_extractKeyUsage_success() {
        val certificateHolder = Mockito.mock(X509CertificateHolder::class.java)
        val extensions = Mockito.mock(Extensions::class.java)
        val keyUsage = Mockito.mock(KeyUsage::class.java)

        Mockito.`when`(certificateHolder.extensions).thenReturn(extensions)

        Mockito.`when`(KeyUsage.fromExtensions(extensions)).thenReturn(keyUsage)

        val result = certificateService.extractKeyUsage(certificateHolder)

        Assert.assertEquals(keyUsage, result)
    }

    @Test
    fun certificateService_extractExtendedKeyUsage_success() {
        val certificateHolder = Mockito.mock(X509CertificateHolder::class.java)
        val extensions = Mockito.mock(Extensions::class.java)
        val extendedKeyUsage = Mockito.mock(ExtendedKeyUsage::class.java)

        Mockito.`when`(certificateHolder.extensions).thenReturn(extensions)

        Mockito.`when`(ExtendedKeyUsage.fromExtensions(extensions)).thenReturn(extendedKeyUsage)

        val result = certificateService.extractExtendedKeyUsage(certificateHolder)

        Assert.assertEquals(extendedKeyUsage, result)
    }

    @Test
    fun certificateService_extractExtendedKeyUsage_returnNullSuccess() {
        val certificateHolder = Mockito.mock(X509CertificateHolder::class.java)
        val extensions = Mockito.mock(Extensions::class.java)

        Mockito.`when`(certificateHolder.extensions).thenReturn(extensions)

        Mockito.`when`(ExtendedKeyUsage.fromExtensions(extensions)).thenReturn(null)

        val result = certificateService.extractExtendedKeyUsage(certificateHolder)

        Assert.assertEquals(ExtendedKeyUsage(arrayOf<KeyPurposeId?>()), result)
    }

    @Test
    fun certificateService_extractEIDType_success() {
        val certificateHolder = Mockito.mock(X509CertificateHolder::class.java)
        val extensions = Mockito.mock(Extensions::class.java)
        val certificatePolicies = Mockito.mock(CertificatePolicies::class.java)

        Mockito.`when`(certificateHolder.extensions).thenReturn(extensions)

        Mockito.`when`(CertificatePolicies.fromExtensions(extensions)).thenReturn(certificatePolicies)

        val policyInformation = arrayOf(Mockito.mock(PolicyInformation::class.java))
        Mockito.`when`(certificatePolicies.policyInformation).thenReturn(policyInformation)
        Mockito
            .`when`(
                policyInformation.first().policyIdentifier,
            ).thenReturn(ASN1ObjectIdentifier.tryFromID("1.3.6.1.4.1.10015.1.1"))

        val result = certificateService.extractEIDType(certificateHolder)

        Assert.assertEquals(EIDType.ID_CARD, result)
    }

    @Test
    fun certificateService_extractFriendlyName_success() {
        val certificateHolder = Mockito.mock(X509CertificateHolder::class.java)
        val subject = Mockito.mock(X500Name::class.java)

        val rdNs = arrayOf(Mockito.mock(RDN::class.java))
        Mockito.`when`(certificateHolder.subject).thenReturn(subject)
        Mockito.`when`(subject.getRDNs(BCStyle.CN)).thenReturn(rdNs)

        val attributeTypeAndValueCN = Mockito.mock(AttributeTypeAndValue::class.java)
        Mockito.`when`(rdNs[0].first).thenReturn(attributeTypeAndValueCN)

        val commonName = "TestSurname,TestGivenName,12345678901"
        val cnEncodable = Mockito.mock(ASN1Encodable::class.java)
        Mockito.`when`(cnEncodable.toString()).thenReturn(commonName)

        Mockito.`when`(attributeTypeAndValueCN.value).thenReturn(cnEncodable)

        val rdSNNs = arrayOf(Mockito.mock(RDN::class.java))
        val rdGNNs = arrayOf(Mockito.mock(RDN::class.java))
        val rdSERIALNs = arrayOf(Mockito.mock(RDN::class.java))

        Mockito.`when`(subject.getRDNs(BCStyle.SURNAME)).thenReturn(rdSNNs)
        Mockito.`when`(subject.getRDNs(BCStyle.GIVENNAME)).thenReturn(rdGNNs)
        Mockito.`when`(subject.getRDNs(BCStyle.SERIALNUMBER)).thenReturn(rdSERIALNs)

        val attributeTypeAndValueSurname = Mockito.mock(AttributeTypeAndValue::class.java)
        val attributeTypeAndValueGivenName = Mockito.mock(AttributeTypeAndValue::class.java)
        val attributeTypeAndValueSerialNumber = Mockito.mock(AttributeTypeAndValue::class.java)

        Mockito.`when`(rdSNNs[0].first).thenReturn(attributeTypeAndValueSurname)
        Mockito.`when`(rdGNNs[0].first).thenReturn(attributeTypeAndValueGivenName)
        Mockito.`when`(rdSERIALNs[0].first).thenReturn(attributeTypeAndValueSerialNumber)

        val surname = "TestSurname"
        val givenName = "TestGivenName"
        val serialNumber = "12345678901"

        val surnameEncodable = Mockito.mock(ASN1Encodable::class.java)
        val givenNameEncodable = Mockito.mock(ASN1Encodable::class.java)
        val serialNumberEncodable = Mockito.mock(ASN1Encodable::class.java)

        Mockito.`when`(surnameEncodable.toString()).thenReturn(surname)
        Mockito.`when`(givenNameEncodable.toString()).thenReturn(givenName)
        Mockito.`when`(serialNumberEncodable.toString()).thenReturn(serialNumber)

        Mockito.`when`(attributeTypeAndValueSurname.value).thenReturn(surnameEncodable)
        Mockito.`when`(attributeTypeAndValueGivenName.value).thenReturn(givenNameEncodable)
        Mockito.`when`(attributeTypeAndValueSerialNumber.value).thenReturn(serialNumberEncodable)

        val result = certificateService.extractFriendlyName(certificateHolder)

        val expectedFriendlyName = "$surname,$givenName,$serialNumber"
        Assert.assertEquals(expectedFriendlyName, result)
    }

    @Test
    fun certificateService_extractFriendlyName_returnCommonNameIfNoSurnameAndGivenName() {
        val certificateHolder = Mockito.mock(X509CertificateHolder::class.java)
        val subject = Mockito.mock(X500Name::class.java)

        val rdNs = arrayOf(Mockito.mock(RDN::class.java))
        Mockito.`when`(certificateHolder.subject).thenReturn(subject)
        Mockito.`when`(subject.getRDNs(BCStyle.CN)).thenReturn(rdNs)

        val attributeTypeAndValueCN = Mockito.mock(AttributeTypeAndValue::class.java)
        Mockito.`when`(rdNs[0].first).thenReturn(attributeTypeAndValueCN)

        val commonName = "TestSurname,TestGivenName,12345678901"
        val cnEncodable = Mockito.mock(ASN1Encodable::class.java)
        Mockito.`when`(cnEncodable.toString()).thenReturn(commonName)

        Mockito.`when`(attributeTypeAndValueCN.value).thenReturn(cnEncodable)

        Mockito.`when`(subject.getRDNs(BCStyle.SURNAME)).thenReturn(emptyArray())
        Mockito.`when`(subject.getRDNs(BCStyle.GIVENNAME)).thenReturn(emptyArray())
        Mockito.`when`(subject.getRDNs(BCStyle.SERIALNUMBER)).thenReturn(emptyArray())

        val result = certificateService.extractFriendlyName(certificateHolder)

        Assert.assertEquals(commonName, result)
    }

    @Test
    fun certificateService_isEllipticCurve_returnTrueWhenCertificateUsesEllipticCurveAlgorithm() {
        val certificateHolder = Mockito.mock(X509CertificateHolder::class.java)
        val subjectPublicKeyInfo = Mockito.mock(SubjectPublicKeyInfo::class.java)
        val algorithmIdentifier = Mockito.mock(AlgorithmIdentifier::class.java)

        Mockito.`when`(certificateHolder.subjectPublicKeyInfo).thenReturn(subjectPublicKeyInfo)
        Mockito.`when`(subjectPublicKeyInfo.algorithm).thenReturn(algorithmIdentifier)
        Mockito.`when`(algorithmIdentifier.algorithm).thenReturn(X9ObjectIdentifiers.id_ecPublicKey)

        val result = certificateService.isEllipticCurve(certificateHolder)

        Assert.assertTrue(result)
    }

    @Test
    fun certificateService_isEllipticCurve_returnFalseWhenCertificateDoesntUseEllipticCurveAlgorithm() {
        val certificateHolder = Mockito.mock(X509CertificateHolder::class.java)
        val subjectPublicKeyInfo = Mockito.mock(SubjectPublicKeyInfo::class.java)
        val algorithmIdentifier = Mockito.mock(AlgorithmIdentifier::class.java)

        Mockito.`when`(certificateHolder.subjectPublicKeyInfo).thenReturn(subjectPublicKeyInfo)
        Mockito.`when`(subjectPublicKeyInfo.algorithm).thenReturn(algorithmIdentifier)
        Mockito.`when`(algorithmIdentifier.algorithm).thenReturn(X9ObjectIdentifiers.id_publicKeyType)

        val result = certificateService.isEllipticCurve(certificateHolder)

        Assert.assertFalse(result)
    }
}
