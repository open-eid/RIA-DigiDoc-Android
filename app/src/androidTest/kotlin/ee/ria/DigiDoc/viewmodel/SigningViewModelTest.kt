@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile.Companion.getResourceFileAsFile
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
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files

@RunWith(MockitoJUnitRunner::class)
class SigningViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var shouldResetSignedContainerObserver: Observer<Boolean?>

    @Mock
    lateinit var contentResolver: ContentResolver

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

    private lateinit var sharedContainerViewModel: SharedContainerViewModel
    private lateinit var viewModel: SigningViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = SigningViewModel()
        viewModel.shouldResetSignedContainer.observeForever(shouldResetSignedContainerObserver)
        sharedContainerViewModel = SharedContainerViewModel(mock(Context::class.java), contentResolver)
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
            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isSignButtonShown = viewModel.isSignButtonShown(context, container, false)

            assertTrue(isSignButtonShown)
        }

    @Test
    fun signingViewModel_isSignButtonShown_emptyFileInContainerReturnFalse() =
        runTest {
            val file = File.createTempFile("temp", ".txt")

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isSignButtonShown = viewModel.isSignButtonShown(context, container, false)

            assertFalse(isSignButtonShown)
        }

    @Test
    fun signingViewModel_isSignButtonShown_containerIsNullReturnFalse() =
        runTest {
            val isSignButtonShown = viewModel.isSignButtonShown(context, null, false)

            assertFalse(isSignButtonShown)
        }

    @Test
    fun signingViewModel_isRoleEmpty_returnTrue() =
        runTest {
            val signatureInterface =
                mock<SignatureInterface> {
                    on { signerRoles } doReturn emptyList()
                    on { city } doReturn ""
                    on { stateOrProvince } doReturn ""
                    on { countryName } doReturn ""
                    on { postalCode } doReturn ""
                }

            val result = viewModel.isRoleEmpty(signatureInterface)

            assertTrue(result)
        }

    @Test
    fun signingViewModel_isRoleEmpty_returnFalse() =
        runTest {
            val signatureInterface =
                mock<SignatureInterface> {
                    on { signerRoles } doReturn listOf("role")
                    on { city } doReturn "Tallinn"
                    on { stateOrProvince } doReturn "Harju"
                    on { countryName } doReturn "Estonia"
                    on { postalCode } doReturn "10152"
                }

            val result = viewModel.isRoleEmpty(signatureInterface)

            assertFalse(result)
        }

    @Test
    fun signingViewModel_isEmptyFileInContainer_returnTrue() =
        runTest {
            val file = File.createTempFile("temp", ".txt")

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isEmptyFileInContainer = viewModel.isEmptyFileInContainer(container)

            assertTrue(isEmptyFileInContainer)
        }

    @Test
    fun signingViewModel_isEmptyFileInContainer_returnFalse() =
        runTest {
            val file = File.createTempFile("temp", ".txt")
            Files.write(file.toPath(), "content".toByteArray(Charset.defaultCharset()))

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

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

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isEncryptButtonShown = viewModel.isEncryptButtonShown(container, false)

            assertTrue(isEncryptButtonShown)
        }

    @Test
    fun signingViewModel_isEncryptButtonShown_returnFalse() =
        runTest {
            val file = File.createTempFile("temp", ".txt")

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isEncryptButtonShown = viewModel.isEncryptButtonShown(container, false)

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

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isShareButtonShown = viewModel.isShareButtonShown(container, false)

            assertTrue(isShareButtonShown)
        }

    @Test
    fun signingViewModel_isShareButtonShown_returnFalse() =
        runTest {
            val file = File.createTempFile("temp", ".txt")

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isShareButtonShown = viewModel.isShareButtonShown(container, false)

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

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isExistingContainer = viewModel.isExistingContainer(container)

            assertTrue(isExistingContainer)
        }

    @Test
    fun signingViewModel_isExistingContainer_returnFalse() =
        runTest {
            val file = File.createTempFile("temp", ".txt")

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

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

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isExistingContainerNoSignatures = viewModel.isExistingContainerNoSignatures(container)

            assertTrue(isExistingContainerNoSignatures)
        }

    @Test
    fun signingViewModel_isExistingContainerNoSignatures_returnFalse() =
        runTest {
            val file = getResourceFileAsFile(context, "example.asice", ee.ria.DigiDoc.common.R.raw.example)

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

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

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isContainerWithoutSignatures = viewModel.isContainerWithoutSignatures(container)

            assertTrue(isContainerWithoutSignatures)
        }

    @Test
    fun signingViewModel_isContainerWithoutSignatures_returnFalse() =
        runTest {
            val file = getResourceFileAsFile(context, "example.asice", ee.ria.DigiDoc.common.R.raw.example)

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

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

    @Test
    fun signingViewModel_openNestedContainer_success() =
        runTest {
            val isSivaConfirmed = true
            val file =
                getResourceFileAsFile(
                    context,
                    "example_nested_container.asice",
                    ee.ria.DigiDoc.common.R.raw.example_nested_container,
                )

            val signedContainer = SignedContainer.openOrCreate(context, file, listOf(file), isSivaConfirmed)

            sharedContainerViewModel.setSignedContainer(signedContainer)

            val nestedFile =
                sharedContainerViewModel.getContainerDataFile(
                    signedContainer,
                    signedContainer.getDataFiles().first(),
                )

            if (nestedFile != null) {
                viewModel.openNestedContainer(context, nestedFile, sharedContainerViewModel, isSivaConfirmed)
                assertEquals(2, sharedContainerViewModel.nestedContainers.size)
            } else {
                fail("Nested file is null")
            }
        }

    @Test(expected = Exception::class)
    fun signingViewModel_openNestedContainer_throwExceptionWhenOpeningContainerUnsuccessful() =
        runTest {
            val file = mock(File::class.java)
            `when`(file.length()).thenReturn(0L)

            viewModel.openNestedContainer(context, file, sharedContainerViewModel, true)
        }

    @Test
    fun signingViewModel_getViewIntent_success() =
        runTest {
            val file =
                getResourceFileAsFile(
                    context,
                    "example_no_signatures.asice",
                    ee.ria.DigiDoc.common.R.raw.example_no_signatures,
                )

            val viewIntent = viewModel.getViewIntent(context, file)

            assertNotNull(viewIntent)
        }

    @Test
    fun signingViewModel_isContainerWithTimestamps_returnFalse() =
        runTest {
            val file = File.createTempFile("temp", ".txt")
            Files.write(file.toPath(), "content".toByteArray(Charset.defaultCharset()))

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isContainerWithTimestamps = viewModel.isContainerWithTimestamps(container)

            assertFalse(isContainerWithTimestamps)
        }
}
