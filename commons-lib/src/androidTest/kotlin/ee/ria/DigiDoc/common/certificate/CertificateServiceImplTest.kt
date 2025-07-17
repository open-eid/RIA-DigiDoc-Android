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
        "MIIDXzCCAkegAwIBAgIUHms7EyI7NCLOc3eWJKQPwxmsEoYwDQYJKoZIhvcNAQELBQ" +
            "AwWDELMAkGA1UEBhMCRUUxDTALBgNVBAgMBFRlc3QxDTALBgNVBAcMBFRlc3QxDTALBgNVBAoMBFRlc3QxDT" +
            "ALBgNVBAsMBFRlc3QxDTALBgNVBAMMBFRlc3QwHhcNMjQxMDE2MjEzMTEyWhcNMjUxMDE2MjEzMTEyWjBYMQ" +
            "swCQYDVQQGEwJFRTENMAsGA1UECAwEVGVzdDENMAsGA1UEBwwEVGVzdDENMAsGA1UECgwEVGVzdDENMAsGA1" +
            "UECwwEVGVzdDENMAsGA1UEAwwEVGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAI6+oLA+a6" +
            "HmunwQcGcJx1OgDuK/F74sigEM0L2rlgQrTKuwmRjPQhlgsPpet9yJ5MzJkwkKEEdho/YfEeXIGRPBZW0IDr" +
            "9enVbVxwF2vWdg7vm8W11aol3UDj5LEn0yJblxODlAcq9rDHO7YmMaLlXkX7C5qVwmFK44z71sWJ6wNG+vOa" +
            "5MXIDOUr4VkvopIJ0Mj690T5LV92k3ZsNugonitFD1jD8fapqrSgRusIugXB8W+0B1t6QswTHlBRJKCzGao" +
            "v/nRsAFw790baQrP2802y8juddo99laQBJgrvLQat/pfldN/hVB4zLV2eiENya8I3/BGdWkExDiJn5pPBMCA" +
            "wEAAaMhMB8wHQYDVR0OBBYEFI8CTTGq928hv5vfogUfgumg516CMA0GCSqGSIb3DQEBCwUAA4IBAQAx6LR27" +
            "5jHfmCdnTVr8SBW6Q8AaiIqyaMpSsnlrLxdJQ0ovGUKOGzO9Cbw9081FIyJqJQk7JZmzZYLt//U9slE+TOTc" +
            "dbI0L38rDA99rmqqP/96qm/ax6RP6dVpnm0xgoEt6dDlQUxqtX1iQYG0MFUY+87P+Ikt47+nWFWNAZvi8wil6" +
            "UN/MSM64UI6DAcp8wGSkoOzBjEG9xGguuXxC05mMjD7k2V0eYBIbM1xmtsNh4QahCeTR4iY9racnHo3jQg8yZ" +
            "bONVYxy8TrdofY8zSVYDu9woyUIQLHiVOm3RAPZWwB9e6Jvl0A510U5PmrLMYwuDvMybPzoC1ozK6DXot"

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
