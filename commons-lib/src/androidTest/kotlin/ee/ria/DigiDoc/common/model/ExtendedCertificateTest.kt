@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.model

import ee.ria.DigiDoc.common.certificate.CertificateService
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class ExtendedCertificateTest {
    @Mock
    private lateinit var certificateService: CertificateService

    @Mock
    private lateinit var x509CertificateHolder: X509CertificateHolder

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun idCardCertificate_create_success() {
        val testData = byteArrayOf(1, 2, 3)
        val testName = "Test name"

        `when`(certificateService.parseCertificate(anyOrNull()))
            .thenReturn(x509CertificateHolder)
        `when`(certificateService.extractEIDType(any()))
            .thenReturn(EIDType.ID_CARD)
        `when`(certificateService.extractFriendlyName(anyOrNull())).thenReturn(testName)
        `when`(certificateService.isEllipticCurve(anyOrNull())).thenReturn(true)

        val certificate = ExtendedCertificate.create(testData, certificateService)

        assertEquals(EIDType.ID_CARD, certificate.type)
        assertEquals(testData, certificate.data)
        assertTrue(certificate.ellipticCurve)
    }

    @Test(expected = IOException::class)
    fun idCardCertificate_create_throwIOExceptionWhenInvalidCertificate() {
        val testData = byteArrayOf(1, 2, 3)

        `when`(certificateService.parseCertificate(anyOrNull()))
            .thenThrow(IOException())

        ExtendedCertificate.create(testData, certificateService)
    }
}
