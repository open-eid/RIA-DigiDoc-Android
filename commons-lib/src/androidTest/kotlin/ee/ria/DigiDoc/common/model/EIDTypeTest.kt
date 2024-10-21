@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.model

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.CertificatePolicies
import org.bouncycastle.asn1.x509.PolicyInformation
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class EIDTypeTest {
    @Test
    fun eidType_parse_returnIdCardWhenIdentifierStartsWith1_3_6_1_4_1_10015_1_1() {
        val policyInformation = mock(PolicyInformation::class.java)
        val policyIdentifier = mock(ASN1ObjectIdentifier::class.java)
        `when`(policyIdentifier.id).thenReturn("1.3.6.1.4.1.10015.1.1")
        `when`(policyInformation.policyIdentifier).thenReturn(policyIdentifier)

        val certificatePolicies = mock(CertificatePolicies::class.java)
        `when`(certificatePolicies.policyInformation).thenReturn(arrayOf(policyInformation))

        val result = EIDType.parse(certificatePolicies)

        assertEquals(EIDType.ID_CARD, result)
    }

    @Test
    fun eidType_parse_returnIdCardWhenIdentifierStartsWith1_3_6_1_4_1_51361_1_1_1() {
        val policyInformation = mock(PolicyInformation::class.java)
        val policyIdentifier = mock(ASN1ObjectIdentifier::class.java)
        `when`(policyIdentifier.id).thenReturn("1.3.6.1.4.1.51361.1.1.1")
        `when`(policyInformation.policyIdentifier).thenReturn(policyIdentifier)

        val certificatePolicies = mock(CertificatePolicies::class.java)
        `when`(certificatePolicies.policyInformation).thenReturn(arrayOf(policyInformation))

        val result = EIDType.parse(certificatePolicies)

        assertEquals(EIDType.ID_CARD, result)
    }

    @Test
    fun eidType_parse_returnDigiIdWhenIdentifierStartsWith1_3_6_1_4_1_10015_1_2() {
        val policyInformation = mock(PolicyInformation::class.java)
        val policyIdentifier = mock(ASN1ObjectIdentifier::class.java)
        `when`(policyIdentifier.id).thenReturn("1.3.6.1.4.1.10015.1.2")
        `when`(policyInformation.policyIdentifier).thenReturn(policyIdentifier)

        val certificatePolicies = mock(CertificatePolicies::class.java)
        `when`(certificatePolicies.policyInformation).thenReturn(arrayOf(policyInformation))

        val result = EIDType.parse(certificatePolicies)

        assertEquals(EIDType.DIGI_ID, result)
    }

    @Test
    fun eidType_parse_returnDigiIdWhenIdentifierStartsWith1_3_6_1_4_1_51361_1_1() {
        val policyInformation = mock(PolicyInformation::class.java)
        val policyIdentifier = mock(ASN1ObjectIdentifier::class.java)
        `when`(policyIdentifier.id).thenReturn("1.3.6.1.4.1.51361.1.1")
        `when`(policyInformation.policyIdentifier).thenReturn(policyIdentifier)

        val certificatePolicies = mock(CertificatePolicies::class.java)
        `when`(certificatePolicies.policyInformation).thenReturn(arrayOf(policyInformation))

        val result = EIDType.parse(certificatePolicies)

        assertEquals(EIDType.DIGI_ID, result)
    }

    @Test
    fun eidType_parse_returnDigiIdWhenIdentifierStartsWith1_3_6_1_4_1_51455_1_1() {
        val policyInformation = mock(PolicyInformation::class.java)
        val policyIdentifier = mock(ASN1ObjectIdentifier::class.java)
        `when`(policyIdentifier.id).thenReturn("1.3.6.1.4.1.51455.1.1")
        `when`(policyInformation.policyIdentifier).thenReturn(policyIdentifier)

        val certificatePolicies = mock(CertificatePolicies::class.java)
        `when`(certificatePolicies.policyInformation).thenReturn(arrayOf(policyInformation))

        val result = EIDType.parse(certificatePolicies)

        assertEquals(EIDType.DIGI_ID, result)
    }

    @Test
    fun eidType_parse_returnMobileIdWhenIdentifierStartsWith1_3_6_1_4_1_10015_1_3() {
        val policyInformation = mock(PolicyInformation::class.java)
        val policyIdentifier = mock(ASN1ObjectIdentifier::class.java)
        `when`(policyIdentifier.id).thenReturn("1.3.6.1.4.1.10015.1.3")
        `when`(policyInformation.policyIdentifier).thenReturn(policyIdentifier)

        val certificatePolicies = mock(CertificatePolicies::class.java)
        `when`(certificatePolicies.policyInformation).thenReturn(arrayOf(policyInformation))

        val result = EIDType.parse(certificatePolicies)

        assertEquals(EIDType.MOBILE_ID, result)
    }

    @Test
    fun eidType_parse_returnMobileIdWhenIdentifierStartsWith1_3_6_1_4_1_10015_11_1() {
        val policyInformation = mock(PolicyInformation::class.java)
        val policyIdentifier = mock(ASN1ObjectIdentifier::class.java)
        `when`(policyIdentifier.id).thenReturn("1.3.6.1.4.1.10015.11.1")
        `when`(policyInformation.policyIdentifier).thenReturn(policyIdentifier)

        val certificatePolicies = mock(CertificatePolicies::class.java)
        `when`(certificatePolicies.policyInformation).thenReturn(arrayOf(policyInformation))

        val result = EIDType.parse(certificatePolicies)

        assertEquals(EIDType.MOBILE_ID, result)
    }

    @Test
    fun eidType_parse_returnESealWhenIdentifierStartsWith1_3_6_1_4_1_10015_7_3() {
        val policyInformation = mock(PolicyInformation::class.java)
        val policyIdentifier = mock(ASN1ObjectIdentifier::class.java)
        `when`(policyIdentifier.id).thenReturn("1.3.6.1.4.1.10015.7.3")
        `when`(policyInformation.policyIdentifier).thenReturn(policyIdentifier)

        val certificatePolicies = mock(CertificatePolicies::class.java)
        `when`(certificatePolicies.policyInformation).thenReturn(arrayOf(policyInformation))

        val result = EIDType.parse(certificatePolicies)

        assertEquals(EIDType.E_SEAL, result)
    }

    @Test
    fun eidType_parse_returnESealWhenIdentifierStartsWith1_3_6_1_4_1_10015_7_1() {
        val policyInformation = mock(PolicyInformation::class.java)
        val policyIdentifier = mock(ASN1ObjectIdentifier::class.java)
        `when`(policyIdentifier.id).thenReturn("1.3.6.1.4.1.10015.7.1")
        `when`(policyInformation.policyIdentifier).thenReturn(policyIdentifier)

        val certificatePolicies = mock(CertificatePolicies::class.java)
        `when`(certificatePolicies.policyInformation).thenReturn(arrayOf(policyInformation))

        val result = EIDType.parse(certificatePolicies)

        assertEquals(EIDType.E_SEAL, result)
    }

    @Test
    fun eidType_parse_returnESealWhenIdentifierStartsWith1_3_6_1_4_1_10015_2_1() {
        val policyInformation = mock(PolicyInformation::class.java)
        val policyIdentifier = mock(ASN1ObjectIdentifier::class.java)
        `when`(policyIdentifier.id).thenReturn("1.3.6.1.4.1.10015.2.1")
        `when`(policyInformation.policyIdentifier).thenReturn(policyIdentifier)

        val certificatePolicies = mock(CertificatePolicies::class.java)
        `when`(certificatePolicies.policyInformation).thenReturn(arrayOf(policyInformation))

        val result = EIDType.parse(certificatePolicies)

        assertEquals(EIDType.E_SEAL, result)
    }

    @Test
    fun eidType_parse_returnUnknownWhenCertificatePoliciesNull() {
        val result = EIDType.parse(null)

        assertEquals(EIDType.UNKNOWN, result)
    }

    @Test
    fun eidType_parse_returnUnknownWhenNoneOfTheIdentifiersExist() {
        val policyInformation = mock(PolicyInformation::class.java)
        val policyIdentifier = mock(ASN1ObjectIdentifier::class.java)
        `when`(policyIdentifier.id).thenReturn("1.2.3.4.5.6.7.8.9")
        `when`(policyInformation.policyIdentifier).thenReturn(policyIdentifier)

        val certificatePolicies = mock(CertificatePolicies::class.java)
        `when`(certificatePolicies.policyInformation).thenReturn(arrayOf(policyInformation))

        val result = EIDType.parse(certificatePolicies)

        assertEquals(EIDType.UNKNOWN, result)
    }
}
