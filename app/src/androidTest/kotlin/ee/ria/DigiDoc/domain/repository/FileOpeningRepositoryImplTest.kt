@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.Constant.DEFAULT_CONTAINER_EXTENSION
import ee.ria.DigiDoc.common.test.AssetFile
import ee.ria.DigiDoc.domain.service.FileOpeningService
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.utilsLib.file.FileStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    private lateinit var fileOpeningService: FileOpeningService
    private lateinit var fileOpeningRepository: FileOpeningRepository
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver

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

    @Before
    fun setUp() =
        runBlocking {
            fileOpeningService = mock(FileOpeningService::class.java)
            fileOpeningRepository = FileOpeningRepositoryImpl(fileOpeningService)
            context = InstrumentationRegistry.getInstrumentation().targetContext
            contentResolver = mock(ContentResolver::class.java)
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
                    SignedContainer.openOrCreate(context, container, listOf(container))
                }

            val signedContainer =
                runBlocking {
                    fileOpeningRepository.removeSignature(existingContainer.getSignatures().first())
                }

            assertEquals(1, signedContainer.getSignatures().size)
        }

//    @Test
//    fun fileOpeningRepository_addFilesToContainer_validFileSuccess() =
//        runBlocking {
//            val uris = listOf(mock(Uri::class.java))
//            val file = createTempFileWithStringContent("test", "Test content")
//            val fileStreams =
//                listOf(FileStream.create(createTempFileWithStringContent("test2", "Another test file content")))
//            val mockContext = mock(Context::class.java)
//
//            `when`(mockContext.filesDir).thenReturn(createTempDirectory("tempDirectory").toFile())
//            `when`(mockContext.cacheDir).thenReturn(createTempDirectory("cacheDirectory").toFile())
//            `when`(fileOpeningService.uriToFile(mockContext, contentResolver, uris.first())).thenReturn(file)
//            `when`(fileOpeningService.isFileSizeValid(file)).thenReturn(true)
//
//            runBlocking {
//                fileOpeningRepository.openOrCreateContainer(mockContext, contentResolver, uris)
//            }
//
//            val signedContainer =
//                runBlocking {
//                    fileOpeningRepository.addFilesToContainer(mockContext, fileStreams)
//                }
//
//            assertEquals(2, signedContainer.getDataFiles().size)
//        }

    @Test
    fun fileOpeningRepository_addFilesToContainer_validFileSuccess() =
        runBlocking {
            val uris = listOf(mock(Uri::class.java))
            val file = createTempFileWithStringContent("test", "Test content")
            val fileStreams =
                listOf(FileStream.create(createTempFileWithStringContent("test2", "Another test file content")))
            val mockContext = mock(Context::class.java)

            `when`(mockContext.filesDir).thenReturn(createTempDirectory("tempDirectory").toFile())
            `when`(mockContext.cacheDir).thenReturn(createTempDirectory("cacheDirectory").toFile())
            `when`(fileOpeningService.uriToFile(mockContext, contentResolver, uris.first())).thenReturn(file)
            `when`(fileOpeningService.isFileSizeValid(file)).thenReturn(true)

            runBlocking {
                fileOpeningRepository.openOrCreateContainer(mockContext, contentResolver, uris)
            }

            val signedContainer =
                runBlocking {
                    fileOpeningRepository.addFilesToContainer(mockContext, fileStreams)
                }

            assertEquals(2, signedContainer.getDataFiles().size)
        }

    @Test
    fun fileOpeningRepository_addFilesToContainer_skipFileWithNoFilename() =
        runBlocking {
            val uris = listOf(mock(Uri::class.java))
            val file = createTempFileWithStringContent("test", "Test content")
            val fileStreams = listOf(FileStream(null, com.google.common.io.Files.asByteSource(file), file.length()))
            val mockContext = mock(Context::class.java)

            `when`(mockContext.filesDir).thenReturn(createTempDirectory("tempDirectory").toFile())
            `when`(mockContext.cacheDir).thenReturn(createTempDirectory("cacheDirectory").toFile())
            `when`(fileOpeningService.uriToFile(mockContext, contentResolver, uris.first())).thenReturn(file)
            `when`(fileOpeningService.isFileSizeValid(file)).thenReturn(true)

            runBlocking {
                fileOpeningRepository.openOrCreateContainer(mockContext, contentResolver, uris)
            }

            val signedContainer =
                runBlocking {
                    fileOpeningRepository.addFilesToContainer(mockContext, fileStreams)
                }

            assertEquals(1, signedContainer.getDataFiles().size)
        }

    @Test(expected = RuntimeException::class)
    fun fileOpeningRepository_addFilesToContainer_throwAlreadyFileExistsException(): Unit =
        runBlocking {
            val uris = listOf(mock(Uri::class.java))
            val file = createTempFileWithStringContent("test", "Test content")
            val fileStreams = listOf(FileStream.create(file))
            val mockContext = mock(Context::class.java)

            `when`(mockContext.filesDir).thenReturn(createTempDirectory("tempDirectory").toFile())
            `when`(mockContext.cacheDir).thenReturn(createTempDirectory("cacheDirectory").toFile())
            `when`(fileOpeningService.uriToFile(mockContext, contentResolver, uris.first())).thenReturn(file)
            `when`(fileOpeningService.isFileSizeValid(file)).thenReturn(true)

            runBlocking {
                fileOpeningRepository.openOrCreateContainer(mockContext, contentResolver, uris)
            }

            val signedContainer =
                runBlocking {
                    fileOpeningRepository.addFilesToContainer(mockContext, fileStreams)
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
                    fileOpeningRepository.openOrCreateContainer(mockContext, contentResolver, uris)
                }

            assertEquals("${file.name}.$DEFAULT_CONTAINER_EXTENSION", signedContainer.getName())
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
                    fileOpeningRepository.openOrCreateContainer(mockContext, contentResolver, uris)
                }

            assertEquals(1, signedContainer.getDataFiles().size)
            assertEquals("${validFile.name}.$DEFAULT_CONTAINER_EXTENSION", signedContainer.getName())
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

            runBlocking { fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris) }
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
        val fileStreams = listOf(FileStream.create(mock(File::class.java)))
        fileOpeningRepository.isEmptyFileInList(fileStreams)
    }

    @Test
    fun fileOpeningRepository_parseUris_success() {
        val uris = listOf(mock(Uri::class.java))
        fileOpeningRepository.parseUris(contentResolver, uris)
    }

    @Test
    fun fileOpeningRepository_getFilesWithValidSize_success() {
        val fileStreams = listOf(FileStream.create(mock(File::class.java)))
        fileOpeningRepository.getFilesWithValidSize(fileStreams)
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
