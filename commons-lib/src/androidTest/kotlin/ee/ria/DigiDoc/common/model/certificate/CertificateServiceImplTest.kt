@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.model.certificate

import ee.ria.DigiDoc.common.certificate.CertificateServiceImpl
import ee.ria.DigiDoc.common.model.EIDType
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.AttributeTypeAndValue
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.CertificatePolicies
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.PolicyInformation
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
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

        assertNotNull(result)
        assertTrue(result.isValidOn(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())))
    }

    @Test(expected = IOException::class)
    fun certificateService_parseCertificate_throwIOExceptionForInvalidCertificateData() {
        val invalidCertificateData: ByteArray = byteArrayOf(0x00, 0x01, 0x02, 0x03)

        certificateService.parseCertificate(invalidCertificateData)
    }

    @Test
    fun certificateService_extractEIDType_success() {
        val certificateHolder = mock(X509CertificateHolder::class.java)
        val extensions = mock(Extensions::class.java)
        val certificatePolicies = mock(CertificatePolicies::class.java)

        `when`(certificateHolder.extensions).thenReturn(extensions)

        `when`(CertificatePolicies.fromExtensions(extensions)).thenReturn(certificatePolicies)

        val policyInformation = arrayOf(mock(PolicyInformation::class.java))
        `when`(certificatePolicies.policyInformation).thenReturn(policyInformation)
        `when`(
            policyInformation.first().policyIdentifier,
        ).thenReturn(ASN1ObjectIdentifier.tryFromID("1.3.6.1.4.1.10015.1.1"))

        val result = certificateService.extractEIDType(certificateHolder)

        assertEquals(EIDType.ID_CARD, result)
    }

    @Test
    fun certificateService_extractFriendlyName_success() {
        val certificateHolder = mock(X509CertificateHolder::class.java)
        val subject = mock(X500Name::class.java)

        val rdNs = arrayOf(mock(RDN::class.java))
        `when`(certificateHolder.subject).thenReturn(subject)
        `when`(subject.getRDNs(BCStyle.CN)).thenReturn(rdNs)

        val attributeTypeAndValueCN = mock(AttributeTypeAndValue::class.java)
        `when`(rdNs[0].first).thenReturn(attributeTypeAndValueCN)

        val commonName = "TestSurname,TestGivenName,12345678901"
        val cnEncodable = mock(ASN1Encodable::class.java)
        `when`(cnEncodable.toString()).thenReturn(commonName)

        `when`(attributeTypeAndValueCN.value).thenReturn(cnEncodable)

        val rdSNNs = arrayOf(mock(RDN::class.java))
        val rdGNNs = arrayOf(mock(RDN::class.java))
        val rdSERIALNs = arrayOf(mock(RDN::class.java))

        `when`(subject.getRDNs(BCStyle.SURNAME)).thenReturn(rdSNNs)
        `when`(subject.getRDNs(BCStyle.GIVENNAME)).thenReturn(rdGNNs)
        `when`(subject.getRDNs(BCStyle.SERIALNUMBER)).thenReturn(rdSERIALNs)

        val attributeTypeAndValueSurname = mock(AttributeTypeAndValue::class.java)
        val attributeTypeAndValueGivenName = mock(AttributeTypeAndValue::class.java)
        val attributeTypeAndValueSerialNumber = mock(AttributeTypeAndValue::class.java)

        `when`(rdSNNs[0].first).thenReturn(attributeTypeAndValueSurname)
        `when`(rdGNNs[0].first).thenReturn(attributeTypeAndValueGivenName)
        `when`(rdSERIALNs[0].first).thenReturn(attributeTypeAndValueSerialNumber)

        val surname = "TestSurname"
        val givenName = "TestGivenName"
        val serialNumber = "12345678901"

        val surnameEncodable = mock(ASN1Encodable::class.java)
        val givenNameEncodable = mock(ASN1Encodable::class.java)
        val serialNumberEncodable = mock(ASN1Encodable::class.java)

        `when`(surnameEncodable.toString()).thenReturn(surname)
        `when`(givenNameEncodable.toString()).thenReturn(givenName)
        `when`(serialNumberEncodable.toString()).thenReturn(serialNumber)

        `when`(attributeTypeAndValueSurname.value).thenReturn(surnameEncodable)
        `when`(attributeTypeAndValueGivenName.value).thenReturn(givenNameEncodable)
        `when`(attributeTypeAndValueSerialNumber.value).thenReturn(serialNumberEncodable)

        val result = certificateService.extractFriendlyName(certificateHolder)

        val expectedFriendlyName = "$surname,$givenName,$serialNumber"
        assertEquals(expectedFriendlyName, result)
    }

    @Test
    fun certificateService_extractFriendlyName_returnCommonNameIfNoSurnameAndGivenName() {
        val certificateHolder = mock(X509CertificateHolder::class.java)
        val subject = mock(X500Name::class.java)

        val rdNs = arrayOf(mock(RDN::class.java))
        `when`(certificateHolder.subject).thenReturn(subject)
        `when`(subject.getRDNs(BCStyle.CN)).thenReturn(rdNs)

        val attributeTypeAndValueCN = mock(AttributeTypeAndValue::class.java)
        `when`(rdNs[0].first).thenReturn(attributeTypeAndValueCN)

        val commonName = "TestSurname,TestGivenName,12345678901"
        val cnEncodable = mock(ASN1Encodable::class.java)
        `when`(cnEncodable.toString()).thenReturn(commonName)

        `when`(attributeTypeAndValueCN.value).thenReturn(cnEncodable)

        `when`(subject.getRDNs(BCStyle.SURNAME)).thenReturn(emptyArray())
        `when`(subject.getRDNs(BCStyle.GIVENNAME)).thenReturn(emptyArray())
        `when`(subject.getRDNs(BCStyle.SERIALNUMBER)).thenReturn(emptyArray())

        val result = certificateService.extractFriendlyName(certificateHolder)

        assertEquals(commonName, result)
    }

    @Test
    fun certificateService_isEllipticCurve_returnTrueWhenCertificateUsesEllipticCurveAlgorithm() {
        val certificateHolder = mock(X509CertificateHolder::class.java)
        val subjectPublicKeyInfo = mock(SubjectPublicKeyInfo::class.java)
        val algorithmIdentifier = mock(AlgorithmIdentifier::class.java)

        `when`(certificateHolder.subjectPublicKeyInfo).thenReturn(subjectPublicKeyInfo)
        `when`(subjectPublicKeyInfo.algorithm).thenReturn(algorithmIdentifier)
        `when`(algorithmIdentifier.algorithm).thenReturn(X9ObjectIdentifiers.id_ecPublicKey)

        val result = certificateService.isEllipticCurve(certificateHolder)

        assertTrue(result)
    }

    @Test
    fun certificateService_isEllipticCurve_returnFalseWhenCertificateDoesntUseEllipticCurveAlgorithm() {
        val certificateHolder = mock(X509CertificateHolder::class.java)
        val subjectPublicKeyInfo = mock(SubjectPublicKeyInfo::class.java)
        val algorithmIdentifier = mock(AlgorithmIdentifier::class.java)

        `when`(certificateHolder.subjectPublicKeyInfo).thenReturn(subjectPublicKeyInfo)
        `when`(subjectPublicKeyInfo.algorithm).thenReturn(algorithmIdentifier)
        `when`(algorithmIdentifier.algorithm).thenReturn(X9ObjectIdentifiers.id_publicKeyType)

        val result = certificateService.isEllipticCurve(certificateHolder)

        assertFalse(result)
    }
}
