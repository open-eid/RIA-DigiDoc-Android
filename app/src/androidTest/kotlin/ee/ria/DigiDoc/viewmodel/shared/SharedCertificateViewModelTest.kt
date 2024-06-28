@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import java.security.cert.X509Certificate

class SharedCertificateViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SharedCertificateViewModel

    @Before
    fun setUp() {
        viewModel = SharedCertificateViewModel()
    }

    @Test
    fun sharedCertificateViewModel_setCertificate_success() {
        val certificate = mock(X509Certificate::class.java)
        viewModel.setCertificate(certificate)

        assertEquals(certificate, viewModel.certificate.value)
    }

    @Test
    fun sharedCertificateViewModel_resetCertificate_success() {
        val certificate = mock(X509Certificate::class.java)
        viewModel.setCertificate(certificate)
        viewModel.resetCertificate()

        assertNull(viewModel.certificate.value)
    }
}
