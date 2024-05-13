@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SharedContainerViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var fileOpeningRepository: FileOpeningRepository

    private lateinit var viewModel: SharedContainerViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = SharedContainerViewModel()
    }

    @Test
    fun sharedContainerViewModel_setSignedContainer_success() {
        val uri: Uri = Mockito.mock()
        val uris = listOf(uri)
        val signedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris)
            }

        viewModel.setSignedContainer(signedContainer)

        assertEquals(signedContainer, viewModel.signedContainer.value)
    }

    @Test
    fun sharedContainerViewModel_resetSignedContainer_success() {
        val uri: Uri = Mockito.mock()
        val uris = listOf(uri)
        val signedContainer =
            runBlocking {
                fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris)
            }

        viewModel.setSignedContainer(signedContainer)

        viewModel.resetSignedContainer()

        assertEquals(null, viewModel.signedContainer.value)
    }
}
