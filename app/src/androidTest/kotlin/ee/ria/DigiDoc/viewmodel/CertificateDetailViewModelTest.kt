@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import ee.ria.DigiDoc.CertificateCreator.Companion.createSelfSignedCertificate
import ee.ria.DigiDoc.utilsLib.extensions.formatHexString
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.util.encoders.Hex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate
import java.security.interfaces.DSAPublicKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.ECGenParameterSpec

class CertificateDetailViewModelTest {
    private lateinit var viewModel: CertificateDetailViewModel

    @Before
    fun setUp() {
        viewModel = CertificateDetailViewModel()
    }

    @Test
    fun certificateDetailViewModel_certificateToJcaX509_success() {
        val certificate = createSelfSignedCertificate()

        val result = viewModel.certificateToJcaX509(certificate)
        assertNotNull(result)
    }

    @Test
    fun certificateDetailViewModel_certificateToJcaX509_returnNullWhenCertificateNull() {
        val result = viewModel.certificateToJcaX509(null)
        assertNull(result)
    }

    @Test
    fun certificateDetailViewModel_certificateToJcaX509_returnNullWhenCertificateEncodingExceptionThrown() {
        val certificate = mock(X509Certificate::class.java)
        `when`(certificate.encoded).thenThrow(CertificateEncodingException())

        val result = viewModel.certificateToJcaX509(certificate)

        assertNull(result)
    }

    @Test
    fun certificateDetailViewModel_getRDNValue_success() {
        val mockX500NameBuilder = mock(X500NameBuilder::class.java)
        val rdn = mock(org.bouncycastle.asn1.x500.RDN::class.java)
        val value = "EE"

        val x500NameBuilder = X500NameBuilder(BCStyle.INSTANCE)
        x500NameBuilder.addRDN(BCStyle.C, "EE")
        val x500Name = x500NameBuilder.build()

        val mockX500Name = mockX500NameBuilder.build()

        `when`(mockX500Name).thenReturn(x500Name)
        `when`(rdn.first).thenReturn(mock(org.bouncycastle.asn1.x500.AttributeTypeAndValue::class.java))
        `when`(rdn.first.value).thenReturn(mock(org.bouncycastle.asn1.ASN1Encodable::class.java))
        `when`(rdn.first.value.toString()).thenReturn(value)

        val result = viewModel.getRDNValue(mockX500NameBuilder.build(), ASN1ObjectIdentifier(BCStyle.C.id))
        assertEquals(value, result)
    }

    @Test
    fun certificateDetailViewModel_getRDNValue_returnEmptyStringWhenNoRDNs() {
        val mockX500Name = mock(X500Name::class.java)
        val mockASN1ObjectIdentifier = mock(ASN1ObjectIdentifier::class.java)

        `when`(mockX500Name.getRDNs(mockASN1ObjectIdentifier)).thenReturn(arrayOf<RDN>())

        val result = viewModel.getRDNValue(mockX500Name, mockASN1ObjectIdentifier)
        assertEquals("", result)
    }

    @Test
    fun certificateDetailViewModel_addLeadingZeroToHex_success() {
        val hexString = "123"
        val result = viewModel.addLeadingZeroToHex(hexString)
        assertEquals("0123", result)
    }

    @Test
    fun certificateDetailViewModel_addLeadingZeroToHex_returnSameValue() {
        val hexString = "A1B2"
        val result = viewModel.addLeadingZeroToHex(hexString)
        assertEquals(hexString, result)
    }

    @Test
    fun certificateDetailViewModel_addLeadingZeroToHex_returnEmptyStringWhenInputNull() {
        val hexString: String? = null
        val result = viewModel.addLeadingZeroToHex(hexString)
        assertEquals("", result)
    }

    @Test
    fun certificateDetailViewModel_addLeadingZeroToHex_returnEmptyStringWhenInputEmpty() {
        val hexString = ""
        val result = viewModel.addLeadingZeroToHex(hexString)
        assertEquals("", result)
    }

    @Test
    fun certificateDetailViewModel_getPublicKeyString_RSAPublicKeySuccess() {
        val publicKey = mock(RSAPublicKey::class.java)
        val modulus = BigInteger("123456")

        `when`(publicKey.modulus).thenReturn(modulus)

        val result = viewModel.getPublicKeyString(publicKey)
        assertEquals("1E 24 0", result)
    }

    @Test
    fun certificateDetailViewModel_getPublicKeyString_ECPublicKeySuccess() {
        val ecSpec = ECGenParameterSpec("secp256r1")
        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        keyPairGenerator.initialize(ecSpec)
        val keyPair = keyPairGenerator.generateKeyPair()
        val publicKey = keyPair.public as ECPublicKey

        val x = publicKey.w.affineX.toString(16)
        val y = publicKey.w.affineY.toString(16)
        val publicKeyString = (x + y).formatHexString()

        val result = viewModel.getPublicKeyString(publicKey)
        assertEquals(publicKeyString, result)
    }

    @Test
    fun certificateDetailViewModel_getPublicKeyString_returnHexStringWithUnknownPublicKey() {
        val mockDSAPublicKey = mock(DSAPublicKey::class.java)
        val encoded = byteArrayOf(1, 2, 3)

        `when`(mockDSAPublicKey.encoded).thenReturn(encoded)

        val result = viewModel.getPublicKeyString(mockDSAPublicKey)
        assertEquals(Hex.toHexString(encoded), result)
    }

    @Test
    fun certificateDetailViewModel_getKeyUsages_successWithPartialCertificateKeyUsages() {
        val certificateKeyUsages = booleanArrayOf(true, false, true, false)

        val expectedOutput = "Digital Signature, Key Encipherment"

        val result = viewModel.getKeyUsages(certificateKeyUsages)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun certificateDetailViewModel_getKeyUsages_returnEmptyStringWhenCertificateKeyUsagesNull() {
        val result = viewModel.getKeyUsages(null)
        assertEquals("", result)
    }

    @Test
    fun certificateDetailViewModel_getKeyUsages_returnEmptyStringWhenCertificateKeyUsagesEmpty() {
        val result = viewModel.getKeyUsages(BooleanArray(0))
        assertEquals("", result)
    }

    @Test
    fun certificateDetailViewModel_getKeyUsages_successWithAllCertificateKeyUsages() {
        val certificateKeyUsages =
            booleanArrayOf(true, true, true, true, true, true, true, true, true)

        val result: String = viewModel.getKeyUsages(certificateKeyUsages)

        assertEquals(
            "Digital Signature, Non-Repudiation, Key Encipherment, Data Encipherment, Key Agreement," +
                " Key Cert Sign, cRL Sign, Encipher Only, Decipher Only",
            result,
        )
    }

    @Test
    fun certificateDetailViewModel_getKeyUsages_returnEmptyStringWhenAllCertificateKeyUsagesFalse() {
        val certificateKeyUsages =
            booleanArrayOf(false, false, false, false, false, false, false, false, false)

        val result: String = viewModel.getKeyUsages(certificateKeyUsages)

        assertEquals("", result)
    }

    @Test
    fun certificateDetailViewModel_getKeyUsages_returnEmptyStringWithInvalidIndex() {
        val certificateKeyUsages = BooleanArray(10)
        certificateKeyUsages[9] = true

        val result: String = viewModel.getKeyUsages(certificateKeyUsages)

        assertEquals("", result)
    }

    @Test
    fun certificateDetailViewModel_getExtensionsData_success() {
        val mockCertificateHolder = mock(JcaX509CertificateHolder::class.java)
        val mockExtensions = mock(Extensions::class.java)
        val mockExtension = Extension(BCStyle.C, false, byteArrayOf(1, 2, 3))

        `when`(mockCertificateHolder.extensions).thenReturn(mockExtensions)
        `when`(mockCertificateHolder.extensions.extensionOIDs).thenReturn(arrayOf(BCStyle.C))
        `when`(mockCertificateHolder.extensions.getExtension(BCStyle.C)).thenReturn(mockExtension)

        val result =
            createSelfSignedCertificate()?.let {
                viewModel.getExtensionsData(
                    mockCertificateHolder,
                    it,
                )
            }
        assertEquals("Extension\n ( 2.5.4.6 )\n    Critical: false \n    ID: 45 45 \n\n", result)
    }

    @Test
    fun certificateDetailViewModel_getExtensionsData_invalidExtensionValue() {
        val mockCertificate = mock(X509Certificate::class.java)
        val mockCertificateHolder = mock(JcaX509CertificateHolder::class.java)
        val mockExtensions = mock(Extensions::class.java)
        val mockExtension = Extension(BCStyle.C, false, byteArrayOf(1, 2, 3))

        `when`(mockCertificateHolder.extensions).thenReturn(mockExtensions)
        `when`(mockCertificateHolder.extensions.extensionOIDs).thenReturn(arrayOf(BCStyle.C))
        `when`(mockCertificateHolder.extensions.getExtension(BCStyle.C)).thenReturn(mockExtension)

        `when`(mockCertificate.getExtensionValue(anyString())).thenReturn(byteArrayOf(4, 4, 4, 1, 1, 1))

        val result = viewModel.getExtensionsData(mockCertificateHolder, mockCertificate)

        assertEquals("", result)
    }

    @Test
    fun certificateDetailViewModel_getCertificateSHA256Fingerprint_success() {
        val certificate = mock(X509Certificate::class.java)
        `when`(certificate.encoded).thenReturn(byteArrayOf(1, 2, 3))

        val result = viewModel.getCertificateSHA256Fingerprint(certificate)
        assertNotNull(result)
    }

    @Test
    fun certificateDetailViewModel_getCertificateSHA256Fingerprint_returnEmptyStringWhenExceptionThrown() {
        val certificate = mock(X509Certificate::class.java)
        `when`(certificate.encoded).thenThrow(CertificateEncodingException("Mock exception"))

        val fingerprint = viewModel.getCertificateSHA256Fingerprint(certificate)

        assertEquals("", fingerprint)
    }

    @Test
    fun certificateDetailViewModel_getCertificateSHA1Fingerprint_success() {
        val certificate = mock(X509Certificate::class.java)
        `when`(certificate.encoded).thenReturn(byteArrayOf(1, 2, 3))

        val result = viewModel.getCertificateSHA1Fingerprint(certificate)
        assertNotNull(result)
    }

    @Test
    fun certificateDetailViewModel_getCertificateSHA1Fingerprint_returnEmptyStringWhenExceptionThrown() {
        val certificate = mock(X509Certificate::class.java)
        `when`(certificate.encoded).thenThrow(CertificateEncodingException("Mock exception"))

        val fingerprint = viewModel.getCertificateSHA1Fingerprint(certificate)

        assertEquals("", fingerprint)
    }

    @Test
    fun certificateDetailViewModel_isValidParametersData_success() {
        val result = viewModel.isValidParametersData("ValidData")
        assertTrue(result)
    }

    @Test
    fun certificateDetailViewModel_isValidParametersData_returnFalseWithInvalidInput() {
        val result = viewModel.isValidParametersData("\u0000")
        assertFalse(result)
    }
}
