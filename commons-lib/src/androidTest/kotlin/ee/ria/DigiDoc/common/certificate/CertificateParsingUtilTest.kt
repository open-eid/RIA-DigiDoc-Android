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

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.x500.AttributeTypeAndValue
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import java.math.BigInteger
import java.security.Principal
import java.security.PublicKey
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

class CertificateParsingUtilTest {
    @Test
    fun certificateParsingUtil_extractFriendlyName_success() {
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

        val result = CertificateParsingUtil.extractFriendlyName(certificateHolder)

        val expectedFriendlyName = "$surname,$givenName,$serialNumber"
        Assert.assertEquals(expectedFriendlyName, result)
    }

    @Test
    fun certificateParsingUtil_extractFriendlyName_returnCommonNameIfNoSurnameAndGivenName() {
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

        val result = CertificateParsingUtil.extractFriendlyName(certificateHolder)

        Assert.assertEquals(commonName, result)
    }

    @Test
    fun certificateParsingUtil_extractFriendlyName_removeSerialNumberPrefix_success() {
        val certificateHolder = Mockito.mock(X509CertificateHolder::class.java)
        val subject = Mockito.mock(X500Name::class.java)

        val rdNs = arrayOf(Mockito.mock(RDN::class.java))
        Mockito.`when`(certificateHolder.subject).thenReturn(subject)
        Mockito.`when`(subject.getRDNs(BCStyle.CN)).thenReturn(rdNs)

        val attributeTypeAndValueCN = Mockito.mock(AttributeTypeAndValue::class.java)
        Mockito.`when`(rdNs[0].first).thenReturn(attributeTypeAndValueCN)

        val commonName = "TestSurname,TestGivenName,PNOEE-12345678901"
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

        val surnameEncodable = Mockito.mock(ASN1Encodable::class.java)
        val givenNameEncodable = Mockito.mock(ASN1Encodable::class.java)
        val serialNumberEncodable = Mockito.mock(ASN1Encodable::class.java)

        Mockito.`when`(surnameEncodable.toString()).thenReturn("TestSurname")
        Mockito.`when`(givenNameEncodable.toString()).thenReturn("TestGivenName")
        Mockito.`when`(serialNumberEncodable.toString()).thenReturn("PNOEE-12345678901")

        Mockito.`when`(attributeTypeAndValueSurname.value).thenReturn(surnameEncodable)
        Mockito.`when`(attributeTypeAndValueGivenName.value).thenReturn(givenNameEncodable)
        Mockito.`when`(attributeTypeAndValueSerialNumber.value).thenReturn(serialNumberEncodable)

        val result = CertificateParsingUtil.extractFriendlyName(certificateHolder)

        Assert.assertEquals("TestSurname,TestGivenName,12345678901", result)
    }

    @Test
    fun certificateParsingUtil_personalData_success() {
        val certificate = FakeX509Certificate("CN=TestSurname\\,TestGivenName\\,12345678901")

        val result = CertificateParsingUtil.personalData(certificate)

        Assert.assertEquals("TestSurname", result.surname)
        Assert.assertEquals("TestGivenName", result.givenNames)
        Assert.assertEquals("12345678901", result.personalCode)
    }

    @Test
    fun certificateParsingUtil_personalData_throwIllegalArgumentExceptionForUnexpectedCertificateSubjectFormat() {
        val certificate = FakeX509Certificate("CN=OnlySurname")

        try {
            CertificateParsingUtil.personalData(certificate)
            Assert.fail("Expected IllegalArgumentException to be thrown")
        } catch (exception: IllegalArgumentException) {
            Assert.assertEquals("Unexpected signing certificate subject format", exception.message)
        }
    }

    private class FakeX509Certificate(
        private val distinguishedName: String,
    ) : X509Certificate() {
        override fun getSubjectX500Principal(): X500Principal = X500Principal(distinguishedName)

        override fun checkValidity() = Unit

        override fun checkValidity(date: Date?) = Unit

        override fun getVersion(): Int = 3

        override fun getSerialNumber(): BigInteger = BigInteger.ONE

        override fun getIssuerDN(): Principal? = null

        override fun getSubjectDN(): Principal? = null

        override fun getNotBefore(): Date = Date()

        override fun getNotAfter(): Date = Date()

        override fun getTBSCertificate(): ByteArray = byteArrayOf()

        override fun getSignature(): ByteArray = byteArrayOf()

        override fun getSigAlgName(): String = ""

        override fun getSigAlgOID(): String = ""

        override fun getSigAlgParams(): ByteArray? = null

        override fun getIssuerUniqueID(): BooleanArray? = null

        override fun getSubjectUniqueID(): BooleanArray? = null

        override fun getKeyUsage(): BooleanArray? = null

        override fun getBasicConstraints(): Int = -1

        override fun getEncoded(): ByteArray = byteArrayOf()

        override fun verify(key: PublicKey?) = Unit

        override fun verify(
            key: PublicKey?,
            sigProvider: String?,
        ) = Unit

        override fun toString(): String = distinguishedName

        override fun getPublicKey(): PublicKey? = null

        override fun hasUnsupportedCriticalExtension(): Boolean = false

        override fun getCriticalExtensionOIDs(): MutableSet<String>? = null

        override fun getNonCriticalExtensionOIDs(): MutableSet<String>? = null

        override fun getExtensionValue(oid: String?): ByteArray? = null
    }
}
