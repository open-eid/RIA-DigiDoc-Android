@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import ee.ria.DigiDoc.CertificateCreator.Companion.createSelfSignedCertificate
import junit.framework.TestCase.assertEquals
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate

class SignerDetailViewModelTest {
    private lateinit var viewModel: SignerDetailViewModel

    @Before
    fun setUp() {
        viewModel = SignerDetailViewModel()
    }

    @Test
    fun signerDetailViewModel_getIssuerCommonName_success() {
        val certificate = createSelfSignedCertificate()
        val x500Name = mock(X500Name::class.java)
        val rdn = mock(RDN::class.java)
        val jcaX509CertificateHolder = mock(JcaX509CertificateHolder::class.java)

        `when`(jcaX509CertificateHolder.issuer).thenReturn(x500Name)
        `when`(x500Name.getRDNs(BCStyle.CN)).thenReturn(arrayOf(rdn))

        val result = viewModel.getIssuerCommonName(certificate)
        assertEquals("Test Common Name", result)
    }

    @Test
    fun signerDetailViewModel_getIssuerCommonName_returnEmptyStringWithNullCertificate() {
        val result = viewModel.getIssuerCommonName(null)
        assertEquals("", result)
    }

    @Test
    fun signerDetailViewModel_getIssuerCommonName_returnEmptyStringWhenCertificateEncodingExceptionThrown() {
        val certificate = mock(X509Certificate::class.java)
        `when`(certificate.encoded).thenThrow(CertificateEncodingException("Mock exception"))

        val commonName = viewModel.getIssuerCommonName(certificate)

        assertEquals("", commonName)
    }

    @Test
    fun signerDetailViewModel_getSubjectCommonName_success() {
        val certificate = createSelfSignedCertificate()
        val x500Name = mock(X500Name::class.java)
        val rdn = mock(RDN::class.java)
        val jcaX509CertificateHolder = mock(JcaX509CertificateHolder::class.java)

        `when`(jcaX509CertificateHolder.subject).thenReturn(x500Name)
        `when`(x500Name.getRDNs(BCStyle.CN)).thenReturn(arrayOf(rdn))

        val result = viewModel.getSubjectCommonName(certificate)
        assertEquals("Test Common Name", result)
    }

    @Test
    fun signerDetailViewModel_getSubjectCommonName_returnEmptyStringWithNullCertificate() {
        val result = viewModel.getSubjectCommonName(null)
        assertEquals("", result)
    }

    @Test
    fun signerDetailViewModel_getSubjectCommonName_returnEmptyStringWhenCertificateEncodingExceptionThrown() {
        val certificate = mock(X509Certificate::class.java)
        `when`(certificate.encoded).thenThrow(CertificateEncodingException("Mock exception"))

        val commonName = viewModel.getSubjectCommonName(certificate)

        assertEquals("", commonName)
    }
}
