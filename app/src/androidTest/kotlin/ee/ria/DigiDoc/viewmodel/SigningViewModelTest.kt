@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.test.AssetFile.Companion.getResourceFileAsFile
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class SigningViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var shouldResetSignedContainerObserver: Observer<Boolean?>

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    Initialization.init(InstrumentationRegistry.getInstrumentation().targetContext)
                } catch (_: Exception) {
                }
            }
        }
    }

    private lateinit var context: Context

    private lateinit var viewModel: SigningViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = SigningViewModel()
        viewModel.shouldResetSignedContainer.observeForever(shouldResetSignedContainerObserver)
    }

    @Test
    fun signingViewModel_handleBackButton_success() {
        viewModel.handleBackButton()

        verify(shouldResetSignedContainerObserver, atLeastOnce()).onChanged(true)
    }

    @Test
    fun signingViewModel_isExistingContainer_returnTrue() =
        runTest {
            val file =
                getResourceFileAsFile(
                    context,
                    "example_no_signatures.asice",
                    ee.ria.DigiDoc.common.R.raw.example_no_signatures,
                )

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isExistingContainer = viewModel.isExistingContainer(container)

            assertTrue(isExistingContainer)
        }

    @Test
    fun signingViewModel_isExistingContainer_returnFalse() =
        runTest {
            val file = File.createTempFile("temp", ".txt")

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isExistingContainer = viewModel.isExistingContainer(container)

            assertFalse(isExistingContainer)
        }

    @Test
    fun signingViewModel_isExistingContainer_returnFalseWhenContainerNull() =
        runTest {
            val isExistingContainer = viewModel.isExistingContainer(null)

            assertFalse(isExistingContainer)
        }

    @Test
    fun signingViewModel_isExistingContainerNoSignatures_returnTrue() =
        runTest {
            val file =
                getResourceFileAsFile(
                    context,
                    "example_no_signatures.asice",
                    ee.ria.DigiDoc.common.R.raw.example_no_signatures,
                )

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isExistingContainerNoSignatures = viewModel.isExistingContainerNoSignatures(container)

            assertTrue(isExistingContainerNoSignatures)
        }

    @Test
    fun signingViewModel_isExistingContainerNoSignatures_returnFalse() =
        runTest {
            val file = getResourceFileAsFile(context, "example.asice", ee.ria.DigiDoc.common.R.raw.example)

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isExistingContainerNoSignatures = viewModel.isExistingContainerNoSignatures(container)

            assertFalse(isExistingContainerNoSignatures)
        }

    @Test
    fun signingViewModel_isExistingContainerNoSignatures_returnFalseWhenContainerNull() =
        runTest {
            val isExistingContainerNoSignatures = viewModel.isExistingContainerNoSignatures(null)

            assertFalse(isExistingContainerNoSignatures)
        }

    @Test
    fun signingViewModel_isContainerWithoutSignatures_returnTrue() =
        runTest {
            val file =
                getResourceFileAsFile(
                    context,
                    "example_no_signatures.asice",
                    ee.ria.DigiDoc.common.R.raw.example_no_signatures,
                )

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isContainerWithoutSignatures = viewModel.isContainerWithoutSignatures(container)

            assertTrue(isContainerWithoutSignatures)
        }

    @Test
    fun signingViewModel_isContainerWithoutSignatures_returnFalse() =
        runTest {
            val file = getResourceFileAsFile(context, "example.asice", ee.ria.DigiDoc.common.R.raw.example)

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isContainerWithoutSignatures = viewModel.isContainerWithoutSignatures(container)

            assertFalse(isContainerWithoutSignatures)
        }

    @Test
    fun signingViewModel_isContainerWithoutSignatures_returnFalseWhenContainerNull() =
        runTest {
            val isContainerWithoutSignatures = viewModel.isContainerWithoutSignatures(null)
            assertFalse(isContainerWithoutSignatures)
        }

    @Test
    fun signingViewModel_getFormattedDate_success() {
        val signingTime = "1970-01-01T03:00:00Z"
        val expectedFormattedDate = "01.01.1970 03:00:00"

        val formattedDate = viewModel.getFormattedDate(signingTime)

        assertEquals(expectedFormattedDate, formattedDate)
    }

    @Test
    fun signingViewModel_getFormattedDate_emptyWithInvalidDateString() {
        val signingTime = "invalid_date_string"

        val formattedDate = viewModel.getFormattedDate(signingTime)

        assertEquals("", formattedDate)
    }
}
