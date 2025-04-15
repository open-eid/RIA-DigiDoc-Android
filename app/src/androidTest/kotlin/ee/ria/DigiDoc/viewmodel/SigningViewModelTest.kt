@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.Constant.ASICE_MIMETYPE
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import ee.ria.DigiDoc.common.R
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile.Companion.getResourceFileAsFile
import ee.ria.DigiDoc.common.testfiles.file.TestFileUtil.Companion.createZipWithTextFile
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepository
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeCache
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolverImpl
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
import org.mockito.kotlin.anyOrNull
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
    lateinit var sivaRepository: SivaRepository

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var fileOpeningRepository: FileOpeningRepository

    @Mock
    lateinit var mimeTypeCache: MimeTypeCache

    lateinit var mimeTypeResolver: MimeTypeResolver

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
        mimeTypeResolver = MimeTypeResolverImpl(mimeTypeCache)
        viewModel =
            SigningViewModel(
                sivaRepository, mimeTypeResolver, fileOpeningRepository, contentResolver,
            )
        viewModel.shouldResetSignedContainer.observeForever(shouldResetSignedContainerObserver)
        sharedContainerViewModel =
            SharedContainerViewModel(
                mock(Context::class.java),
                contentResolver,
            )
    }

    @Test
    fun signingViewModel_handleBackButton_success() {
        viewModel.handleBackButton()

        verify(shouldResetSignedContainerObserver, atLeastOnce()).onChanged(true)
    }

    @Test
    fun signingViewModel_isSignButtonShown_returnTrue() =
        runTest {
            `when`(mimeTypeCache.getMimeType(anyOrNull())).thenReturn(ASICE_MIMETYPE)

            val file = File.createTempFile("temp", ".txt")
            Files.write(file.toPath(), "content".toByteArray(Charset.defaultCharset()))
            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isSignButtonShown = viewModel.isSignButtonShown(container, false, false, false)

            assertTrue(isSignButtonShown)
        }

    @Test
    fun signingViewModel_isSignButtonShown_emptyFileInContainerReturnFalse() =
        runTest {
            `when`(mimeTypeCache.getMimeType(anyOrNull())).thenReturn(ASICE_MIMETYPE)

            val file = File.createTempFile("temp", ".txt")

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isSignButtonShown = viewModel.isSignButtonShown(container, false, false, false)

            assertFalse(isSignButtonShown)
        }

    @Test
    fun signingViewModel_isSignButtonShown_containerIsNullReturnFalse() =
        runTest {
            val isSignButtonShown = viewModel.isSignButtonShown(null, false, false, false)

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
                    R.raw.example_no_signatures,
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
                    R.raw.example_no_signatures,
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
                    R.raw.example_no_signatures,
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
                    R.raw.example_no_signatures,
                )

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isExistingContainerNoSignatures = viewModel.isExistingContainerNoSignatures(container)

            assertTrue(isExistingContainerNoSignatures)
        }

    @Test
    fun signingViewModel_isExistingContainerNoSignatures_returnFalse() =
        runTest {
            val file = getResourceFileAsFile(context, "example.asice", R.raw.example)

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
                    R.raw.example_no_signatures,
                )

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val isContainerWithoutSignatures = viewModel.isContainerWithoutSignatures(container)

            assertTrue(isContainerWithoutSignatures)
        }

    @Test
    fun signingViewModel_isContainerWithoutSignatures_returnFalse() =
        runTest {
            val file = getResourceFileAsFile(context, "example.asice", R.raw.example)

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
    fun signingViewModel_openNestedContainer_success() =
        runTest {
            val isSivaConfirmed = true
            val file =
                getResourceFileAsFile(
                    context,
                    "example_nested_container.asice",
                    R.raw.example_nested_container,
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

    @Test
    fun signingViewModel_getTimestampedContainer_success() =
        runTest {
            val mockContainer = createZipWithTextFile(ASICS_MIMETYPE)
            val mockTimeStampedContainer = createZipWithTextFile(ASICS_MIMETYPE, "mockTimestamp")

            val signedContainer = SignedContainer.openOrCreate(context, mockContainer, listOf(mockContainer), true)
            val timestampedContainer =
                SignedContainer.openOrCreate(
                    context,
                    mockTimeStampedContainer,
                    listOf(mockTimeStampedContainer),
                    true,
                )

            `when`(sivaRepository.isTimestampedContainer(signedContainer, true)).thenReturn(true)
            `when`(sivaRepository.getTimestampedContainer(context, signedContainer)).thenReturn(timestampedContainer)

            val tsContainer = viewModel.getTimestampedContainer(context, signedContainer, true)

            assertNotNull(tsContainer)
        }

    @Test
    fun signingViewModel_getTimestampedContainer_successWhenSivaNotConfirmed() =
        runTest {
            val mockContainer = createZipWithTextFile(ASICS_MIMETYPE)

            val signedContainer = SignedContainer.openOrCreate(context, mockContainer, listOf(mockContainer), true)

            `when`(sivaRepository.isTimestampedContainer(signedContainer, false)).thenReturn(false)

            val container = viewModel.getTimestampedContainer(context, signedContainer, false)

            assertNotNull(container)
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
            `when`(mimeTypeCache.getMimeType(anyOrNull())).thenReturn(ASICE_MIMETYPE)

            val file =
                getResourceFileAsFile(
                    context,
                    "example_no_signatures.asice",
                    R.raw.example_no_signatures,
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

    @Test
    fun signingViewModel_getMimetype_success() =
        runTest {
            `when`(mimeTypeCache.getMimeType(anyOrNull())).thenReturn(ASICE_MIMETYPE)

            val file = File.createTempFile("temp", ".txt")
            Files.write(file.toPath(), "content".toByteArray(Charset.defaultCharset()))

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val containerFile = container.getContainerFile()

            if (containerFile != null) {
                val mimetype = viewModel.getMimetype(containerFile)
                assertEquals(ASICE_MIMETYPE, mimetype)
            } else {
                fail("containerFile is null")
            }
        }

    @Test
    fun signingViewModel_getMimetype_defaultMimeType() =
        runTest {
            `when`(mimeTypeCache.getMimeType(anyOrNull())).thenReturn("")

            val file = File.createTempFile("temp", ".txt")
            Files.write(file.toPath(), "content".toByteArray(Charset.defaultCharset()))

            val container = SignedContainer.openOrCreate(context, file, listOf(file), true)

            val containerFile = container.getContainerFile()

            if (containerFile != null) {
                val mimetype = viewModel.getMimetype(containerFile)
                assertEquals(DEFAULT_MIME_TYPE, mimetype)
            } else {
                fail("containerFile is null")
            }
        }

    @Test
    fun signingViewModel_getMimetype_successGettingFileMimetype() =
        runTest {
            `when`(mimeTypeCache.getMimeType(anyOrNull())).thenReturn("text/plain")

            val file = File.createTempFile("temp", ".txt")

            val mimetype = viewModel.getMimetype(file)

            assertEquals("text/plain", mimetype)
        }
}
