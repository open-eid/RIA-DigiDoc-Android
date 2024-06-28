@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class SharedSignatureViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SharedSignatureViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = SharedSignatureViewModel()
    }

    @Test
    fun sharedSignatureViewModel_setCertificate_success() {
        val signature = mock(SignatureInterface::class.java)
        val observer = mock(Observer::class.java) as Observer<SignatureInterface?>

        viewModel.signature.observeForever(observer)
        viewModel.setSignature(signature)

        verify(observer).onChanged(signature)
        assertEquals(signature, viewModel.signature.value)
    }

    @Test
    fun sharedSignatureViewModel_resetSignature_success() {
        val signature = mock(SignatureInterface::class.java)
        val observer = mock(Observer::class.java) as Observer<SignatureInterface?>

        viewModel.signature.observeForever(observer)
        viewModel.setSignature(signature)
        viewModel.resetSignature()

        verify(observer).onChanged(null)
        assertNull(viewModel.signature.value)
    }
}
