@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.Constant.ASICE_MIMETYPE
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DDOC_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DEFAULT_CONTAINER_EXTENSION
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
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
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepositoryImpl
import ee.ria.DigiDoc.domain.service.fileopening.FileOpeningService
import ee.ria.DigiDoc.domain.service.siva.SivaService
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.io.path.createTempDirectory

class FileOpeningRepositoryImplTest {
    private lateinit var context: Context
    private lateinit var fileOpeningService: FileOpeningService
    private lateinit var fileOpeningRepository: FileOpeningRepository
    private lateinit var sivaService: SivaService
    private lateinit var contentResolver: ContentResolver
    private lateinit var signedPdfDocument: File

    companion object {
        private lateinit var configurationLoader: ConfigurationLoader
        private lateinit var configurationRepository: ConfigurationRepository

        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val context = InstrumentationRegistry.getInstrumentation().targetContext
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

    @Before
    fun setUp() =
        runBlocking {
            fileOpeningService = mock(FileOpeningService::class.java)
            sivaService = mock(SivaService::class.java)
            fileOpeningRepository = FileOpeningRepositoryImpl(fileOpeningService, sivaService)
            context = InstrumentationRegistry.getInstrumentation().targetContext
            contentResolver = mock(ContentResolver::class.java)
            signedPdfDocument =
                AssetFile.getResourceFileAsFile(
                    context, "example_signed_pdf.pdf",
                    ee.ria.DigiDoc.common.R.raw.example_signed_pdf,
                )
        }

    @Test
    fun fileOpeningRepository_isFileSizeValid_validFileSuccess() {
        val file = mock(File::class.java)
        runBlocking {
            `when`(fileOpeningService.isFileSizeValid(file)).thenReturn(true)
            val result = fileOpeningRepository.isFileSizeValid(file)
            assertEquals(true, result)
        }
    }

    @Test
    fun fileOpeningRepository_isFileSizeValid_invalidFileUnsuccessful() {
        val file = mock(File::class.java)
        runBlocking {
            `when`(fileOpeningService.isFileSizeValid(file)).thenReturn(false)
            val result = fileOpeningRepository.isFileSizeValid(file)
            assertEquals(false, result)
        }
    }

    @Test
    fun fileOpeningRepository_uriToFile_success() {
        val uri = mock(Uri::class.java)
        val expectedFile = mock(File::class.java)
        runBlocking {
            `when`(fileOpeningService.uriToFile(context, contentResolver, uri)).thenReturn(expectedFile)
            val result = fileOpeningRepository.uriToFile(context, contentResolver, uri)
            assertEquals(expectedFile, result)
        }
    }

    @Test
    fun fileOpeningRepository_showFileChooser_success() =
        runBlocking {
            @Suppress("UNCHECKED_CAST")
            val activityResultLauncher = mock(ActivityResultLauncher::class.java) as ActivityResultLauncher<String>

            fileOpeningRepository.showFileChooser(activityResultLauncher, "*/*")

            verify(activityResultLauncher).launch("*/*")
        }

    @Test
    fun fileOpeningRepository_removeSignature_validSignatureSuccess() =
        runBlocking {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val existingContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, container, listOf(container), true)
                }

            runBlocking {
                existingContainer.removeSignature(existingContainer.getSignatures().first())
            }

            assertEquals(1, existingContainer.getSignatures().size)
        }

    @Test
    fun fileOpeningRepository_addFilesToContainer_validFileSuccess() =
        runBlocking {
            val uris = listOf(mock(Uri::class.java))
            val file = createTempFileWithStringContent("test", "Test content")

            val files =
                listOf(createTempFileWithStringContent("test2", "Another test file content"))

            `when`(fileOpeningService.uriToFile(context, contentResolver, uris.first())).thenReturn(file)
            `when`(fileOpeningService.isFileSizeValid(file)).thenReturn(true)

            val signedContainer =
                runBlocking {
                    fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)
                }

            runBlocking {
                fileOpeningRepository.addFilesToContainer(context, signedContainer, files)
            }

            assertEquals(2, signedContainer.getDataFiles().size)
        }

    @Test(expected = RuntimeException::class)
    fun fileOpeningRepository_addFilesToContainer_throwAlreadyFileExistsException(): Unit =
        runBlocking {
            val uris = listOf(mock(Uri::class.java))
            val file = createTempFileWithStringContent("test", "Test content")
            val files = listOf(file)

            `when`(fileOpeningService.uriToFile(context, contentResolver, uris.first())).thenReturn(file)
            `when`(fileOpeningService.isFileSizeValid(file)).thenReturn(true)

            val signedContainer =
                runBlocking {
                    fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true)
                }

            runBlocking {
                fileOpeningRepository.addFilesToContainer(context, signedContainer, files)
            }

            assertEquals(0, signedContainer.getDataFiles().size)
        }

    @Test
    fun fileOpeningRepository_openOrCreateContainer_validFilesSuccess() =
        runBlocking {
            val uris = listOf(mock(Uri::class.java))
            val file = createTempFileWithStringContent("test", "Test content")
            val mockContext = mock(Context::class.java)

            `when`(mockContext.filesDir).thenReturn(createTempDirectory("tempDirectory").toFile())
            `when`(fileOpeningService.uriToFile(mockContext, contentResolver, uris.first())).thenReturn(file)
            `when`(fileOpeningService.isFileSizeValid(file)).thenReturn(true)

            val signedContainer =
                runBlocking {
                    fileOpeningRepository.openOrCreateContainer(mockContext, contentResolver, uris, true)
                }

            assertEquals("${file.nameWithoutExtension}.$DEFAULT_CONTAINER_EXTENSION", signedContainer.getName())
            assertFalse(signedContainer.isExistingContainer())
        }

    @Test
    fun fileOpeningRepository_openOrCreateContainer_validFilesFilterValidFileSuccess() =
        runBlocking {
            val uris = listOf(mock(Uri::class.java), mock(Uri::class.java))
            val validFile = createTempFileWithStringContent("test", "Test content")
            val invalidFile = createTempFileWithStringContent("test2", "Test content 2")
            val mockContext = mock(Context::class.java)

            `when`(mockContext.filesDir).thenReturn(createTempDirectory("tempDirectory").toFile())
            `when`(fileOpeningService.uriToFile(mockContext, contentResolver, uris.first())).thenReturn(validFile)
            `when`(fileOpeningService.uriToFile(mockContext, contentResolver, uris[1])).thenReturn(invalidFile)
            `when`(fileOpeningService.isFileSizeValid(validFile)).thenReturn(true)
            `when`(fileOpeningService.isFileSizeValid(invalidFile)).thenReturn(false)

            val signedContainer =
                runBlocking {
                    fileOpeningRepository.openOrCreateContainer(mockContext, contentResolver, uris, true)
                }

            assertEquals(1, signedContainer.getDataFiles().size)
            assertEquals("${validFile.nameWithoutExtension}.$DEFAULT_CONTAINER_EXTENSION", signedContainer.getName())
            assertFalse(signedContainer.isExistingContainer())
        }

    @Test(expected = EmptyFileException::class)
    fun fileOpeningRepository_openOrCreateContainer_throwException(): Unit =
        runBlocking {
            val uris = listOf(mock(Uri::class.java))
            val file = mock(File::class.java)

            `when`(file.name).thenReturn("test.txt")
            `when`(fileOpeningService.uriToFile(context, contentResolver, uris.first())).thenReturn(file)
            `when`(fileOpeningService.isFileSizeValid(file)).thenReturn(false)

            runBlocking { fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris, true) }
        }

    @Test(expected = NoSuchElementException::class)
    fun fileOpeningRepository_checkForValidFiles_throwException() {
        runBlocking { fileOpeningRepository.checkForValidFiles(emptyList()) }
    }

    @Test
    fun fileOpeningRepository_checkForValidFiles_success() {
        val files = listOf(mock(File::class.java))
        runBlocking { fileOpeningRepository.checkForValidFiles(files) }
    }

    @Test
    fun fileOpeningRepository_isEmptyFileInList_success() {
        val files = listOf(mock(File::class.java))
        fileOpeningRepository.isEmptyFileInList(files)
    }

    @Test
    fun fileOpeningRepository_getFilesWithValidSize_success() {
        val files = listOf(mock(File::class.java))
        fileOpeningRepository.getFilesWithValidSize(files)
    }

    @Test
    fun fileOpeningRepository_isSivaConfirmationNeeded_returnTrueForDDOCContainer() {
        val file = createZipWithTextFile(DDOC_MIMETYPE)
        val files = listOf(file)
        `when`(sivaService.isSivaConfirmationNeeded(context, files)).thenReturn(true)
        val isSivaConfirmationNeeded = fileOpeningRepository.isSivaConfirmationNeeded(context, files)
        assertTrue(isSivaConfirmationNeeded)
    }

    @Test
    fun fileOpeningRepository_isSivaConfirmationNeeded_returnTrueForASICSContainer() {
        val file = createZipWithTextFile(ASICS_MIMETYPE)
        val files = listOf(file)
        `when`(sivaService.isSivaConfirmationNeeded(context, files)).thenReturn(true)
        val isSivaConfirmationNeeded = fileOpeningRepository.isSivaConfirmationNeeded(context, files)
        assertTrue(isSivaConfirmationNeeded)
    }

    @Test
    fun fileOpeningRepository_isSivaConfirmationNeeded_returnTrueForSignedPDF() {
        val files = listOf(signedPdfDocument)
        `when`(sivaService.isSivaConfirmationNeeded(context, files)).thenReturn(true)
        val isSivaConfirmationNeeded = fileOpeningRepository.isSivaConfirmationNeeded(context, files)
        assertTrue(isSivaConfirmationNeeded)
    }

    @Test
    fun fileOpeningRepository_isSivaConfirmationNeeded_returnFalse() {
        val file = createZipWithTextFile(ASICE_MIMETYPE)
        val files = listOf(file)
        `when`(sivaService.isSivaConfirmationNeeded(context, files)).thenReturn(false)
        val isSivaConfirmationNeeded = fileOpeningRepository.isSivaConfirmationNeeded(context, files)
        assertFalse(isSivaConfirmationNeeded)
    }

    @Test
    fun fileOpeningRepository_isSivaConfirmationNeeded_returnFalseForMultipleFiles() {
        val file = createZipWithTextFile(ASICE_MIMETYPE)
        val files = listOf(file, mock(File::class.java))
        `when`(sivaService.isSivaConfirmationNeeded(context, files)).thenReturn(false)
        val isSivaConfirmationNeeded = fileOpeningRepository.isSivaConfirmationNeeded(context, files)
        assertFalse(isSivaConfirmationNeeded)
    }

    private fun createTempFileWithStringContent(
        filename: String,
        content: String,
    ): File {
        val tempFile = File.createTempFile(filename, ".txt", context.cacheDir)
        Files.write(tempFile.toPath(), content.toByteArray(Charset.defaultCharset()))
        return tempFile
    }
}
