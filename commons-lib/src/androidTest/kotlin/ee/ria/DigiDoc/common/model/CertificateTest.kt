@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.model

import ee.ria.DigiDoc.common.certificate.CertificateService
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class CertificateTest {
    @Mock
    private lateinit var certificateService: CertificateService

    @Mock
    private lateinit var x509CertificateHolder: X509CertificateHolder

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun certificate_create_success() {
        val testData = byteArrayOf(1, 2, 3)
        val testName = "Test name"

        `when`(certificateService.extractFriendlyName(anyOrNull())).thenReturn(testName)

        val certificate = Certificate.create(testData, certificateService)

        assertEquals(testName, certificate.friendlyName)
    }

    @Test(expected = IOException::class)
    fun certificate_create_throwIOExceptionWhenInvalidCertificate() {
        val testData = byteArrayOf(1, 2, 3)

        `when`(certificateService.parseCertificate(anyOrNull()))
            .thenThrow(IOException())

        Certificate.create(testData, certificateService)
    }
}
