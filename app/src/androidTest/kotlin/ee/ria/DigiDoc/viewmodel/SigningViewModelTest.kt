@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.test.AssetFile.Companion.getResourceFileAsFile
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
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
import java.nio.charset.Charset
import java.nio.file.Files

@RunWith(MockitoJUnitRunner::class)
class SigningViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var shouldResetSignedContainerObserver: Observer<Boolean?>

    companion object {
        private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        private lateinit var configurationLoader: ConfigurationLoader
        private lateinit var configurationRepository: ConfigurationRepository

        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    configurationLoader =
                        ConfigurationLoaderImpl(
                            Gson(),
                            CentralConfigurationRepositoryImpl(
                                CentralConfigurationServiceImpl("Tests", ConfigurationProperty()),
                            ),
                            ConfigurationProperty(),
                            ConfigurationPropertiesImpl(),
                            ConfigurationSignatureVerifierImpl(),
                        )
                    configurationRepository = ConfigurationRepositoryImpl(context, configurationLoader)
                    Initialization(configurationRepository).init(context)
                } catch (_: Exception) {
                }
            }
        }
    }

    private lateinit var viewModel: SigningViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = SigningViewModel()
        viewModel.shouldResetSignedContainer.observeForever(shouldResetSignedContainerObserver)
    }

    @Test
    fun signingViewModel_handleBackButton_success() {
        viewModel.handleBackButton()

        verify(shouldResetSignedContainerObserver, atLeastOnce()).onChanged(true)
    }

    @Test
    fun signingViewModel_isSignButtonShown_returnTrue() =
        runTest {
            val file = File.createTempFile("temp", ".txt")
            Files.write(file.toPath(), "content".toByteArray(Charset.defaultCharset()))
            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isSignButtonShown = viewModel.isSignButtonShown(container)

            assertTrue(isSignButtonShown)
        }

    @Test
    fun signingViewModel_isSignButtonShown_emptyFileInContainerReturnFalse() =
        runTest {
            val file = File.createTempFile("temp", ".txt")

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isSignButtonShown = viewModel.isSignButtonShown(container)

            assertFalse(isSignButtonShown)
        }

    @Test
    fun signingViewModel_isSignButtonShown_containerIsNullReturnFalse() =
        runTest {
            val isSignButtonShown = viewModel.isSignButtonShown(null)

            assertFalse(isSignButtonShown)
        }

    @Test
    fun signingViewModel_isEmptyFileInContainer_returnTrue() =
        runTest {
            val file = File.createTempFile("temp", ".txt")

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isEmptyFileInContainer = viewModel.isEmptyFileInContainer(container)

            assertTrue(isEmptyFileInContainer)
        }

    @Test
    fun signingViewModel_isEmptyFileInContainer_returnFalse() =
        runTest {
            val file = File.createTempFile("temp", ".txt")
            Files.write(file.toPath(), "content".toByteArray(Charset.defaultCharset()))

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isEmptyFileInContainer = viewModel.isEmptyFileInContainer(container)

            assertFalse(isEmptyFileInContainer)
        }

    @Test
    fun signingViewModel_isEncryptButtonShown_returnTrue() =
        runTest {
            val file =
                getResourceFileAsFile(
                    context,
                    "example_no_signatures.asice",
                    ee.ria.DigiDoc.common.R.raw.example_no_signatures,
                )

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isEncryptButtonShown = viewModel.isEncryptButtonShown(container)

            assertTrue(isEncryptButtonShown)
        }

    @Test
    fun signingViewModel_isEncryptButtonShown_returnFalse() =
        runTest {
            val file = File.createTempFile("temp", ".txt")

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isEncryptButtonShown = viewModel.isEncryptButtonShown(container)

            assertFalse(isEncryptButtonShown)
        }

    @Test
    fun signingViewModel_isShareButtonShown_returnTrue() =
        runTest {
            val file =
                getResourceFileAsFile(
                    context,
                    "example_no_signatures.asice",
                    ee.ria.DigiDoc.common.R.raw.example_no_signatures,
                )

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isShareButtonShown = viewModel.isShareButtonShown(container)

            assertTrue(isShareButtonShown)
        }

    @Test
    fun signingViewModel_isShareButtonShown_returnFalse() =
        runTest {
            val file = File.createTempFile("temp", ".txt")

            val container = SignedContainer.openOrCreate(context, file, listOf(file))

            val isShareButtonShown = viewModel.isShareButtonShown(container)

            assertFalse(isShareButtonShown)
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
        val signingTime = "1970-01-01T00:00:00Z"
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
