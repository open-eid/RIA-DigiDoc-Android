@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.io.Files
import ee.ria.DigiDoc.common.Constant.DIR_SIGNATURE_CONTAINERS
import ee.ria.DigiDoc.utilsLib.file.FileStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FilenameUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

@ExperimentalCoroutinesApi
class ContainerUtilTest {
    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockDirectory: File

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun containerUtil_addSignatureContainer_success(): Unit =
        runBlocking {
            val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
            val testFileContent = "Test content"
            val testFile = File.createTempFile("testFile", ".txt")
            FileWriter(testFile).use { writer ->
                writer.write(testFileContent)
            }

            `when`(mockContext.filesDir).thenReturn(testFile.parentFile)
            `when`(mockContext.cacheDir).thenReturn(targetContext.cacheDir)

            val containerFile = ContainerUtil.addSignatureContainer(mockContext, testFile)

            assertEquals(File(File(mockContext.cacheDir, DIR_SIGNATURE_CONTAINERS), testFile.name), containerFile)
            assertEquals(testFileContent, FileInputStream(containerFile).bufferedReader().use { it.readText() })

            testFile.delete()
            containerFile.delete()
        }

    @Test
    fun containerUtil_cache_success(): Unit =
        runBlocking {
            val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
            val testFileContent = "Test content"
            val testFile = File.createTempFile("testFile", ".txt")
            val testFileStream = FileStream.create(testFile)
            FileWriter(testFile).use { writer ->
                writer.write(testFileContent)
            }
            `when`(mockContext.cacheDir).thenReturn(targetContext.cacheDir)

            val file = ContainerUtil.cache(mockContext, testFileStream)

            assertEquals(file, testFile)
            testFile.delete()
        }

    @Test
    fun containerUtil_cache_returnNull(): Unit =
        runBlocking {
            val testFileContent = "Test content"
            val testFile = File.createTempFile("testFile", ".txt")
            val testFileStream = FileStream(null, Files.asByteSource(testFile), testFile.length())
            FileWriter(testFile).use { writer ->
                writer.write(testFileContent)
            }

            val file = ContainerUtil.cache(mockContext, testFileStream)

            assertNull(file)
            testFile.delete()
        }

    @Test
    fun containerUtil_getContainerDataFilesDir_success() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testFileContent = "Test content"
        val testFile = File.createTempFile("testFile", ".txt")
        FileWriter(testFile).use { writer ->
            writer.write(testFileContent)
        }
        `when`(mockContext.filesDir).thenReturn(testFile.parentFile)
        `when`(mockContext.cacheDir).thenReturn(targetContext.cacheDir)

        val file = ContainerUtil.getContainerDataFilesDir(mockContext, testFile)

        assertEquals(testFile.path + "-data-files", file.path)
        testFile.delete()
    }

    @Test
    fun containerUtil_getContainerDataFilesDir_successSignatureContainersDir() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testFileContent = "Test content"

        val dir = File(targetContext.cacheDir, DIR_SIGNATURE_CONTAINERS)
        dir.mkdirs()

        val testFile = File.createTempFile("testFile", ".txt", dir)
        FileWriter(testFile).use { writer ->
            writer.write(testFileContent)
        }
        `when`(mockContext.filesDir).thenReturn(targetContext.cacheDir)
        `when`(mockContext.cacheDir).thenReturn(targetContext.cacheDir)

        val file = ContainerUtil.getContainerDataFilesDir(mockContext, testFile)

        assertEquals(targetContext.cacheDir.path + "/" + testFile.name + "-data-files", file.path)
        testFile.delete()
    }

    @Test
    fun containerUtil_isEmptyFileInList_returnFalse() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testFileContent = "Test content"
        val testFile = File.createTempFile("testFile", ".txt")
        FileWriter(testFile).use { writer ->
            writer.write(testFileContent)
        }
        `when`(mockContext.filesDir).thenReturn(testFile.parentFile)
        `when`(mockContext.cacheDir).thenReturn(targetContext.cacheDir)

        val result = ContainerUtil.isEmptyFileInList(listOf(FileStream.create(testFile)))

        assertFalse(result)
        testFile.delete()
    }

    @Test
    fun containerUtil_isEmptyFileInList_returnTrue() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testFile = File.createTempFile("testFile", ".txt")

        `when`(mockContext.filesDir).thenReturn(testFile.parentFile)
        `when`(mockContext.cacheDir).thenReturn(targetContext.cacheDir)

        val result = ContainerUtil.isEmptyFileInList(listOf(FileStream.create(testFile)))

        assertTrue(result)
        testFile.delete()
    }

    @Test
    fun containerUtil_addExtensionToContainerFilename_success() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testFile = File.createTempFile("testFile", ".txt")

        `when`(mockContext.filesDir).thenReturn(testFile.parentFile)
        `when`(mockContext.cacheDir).thenReturn(targetContext.cacheDir)

        val result = ContainerUtil.addExtensionToContainerFilename(testFile.name)

        assertEquals(FilenameUtils.removeExtension(testFile.name) + ".asice", result)
        testFile.delete()
    }

    @Test
    fun containerUtil_addExtensionToContainerFilename_addExtension() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testFile = File.createTempFile("testFile", "")

        `when`(mockContext.filesDir).thenReturn(testFile.parentFile)
        `when`(mockContext.cacheDir).thenReturn(targetContext.cacheDir)

        val result = ContainerUtil.addExtensionToContainerFilename(testFile.name)

        assertEquals(testFile.name + ".asice", result)
        testFile.delete()
    }

    @Test
    fun containerUtil_addExtensionToContainerFilename_returnName() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testFile = File.createTempFile("testFile", ".bdoc")

        `when`(mockContext.filesDir).thenReturn(testFile.parentFile)
        `when`(mockContext.cacheDir).thenReturn(targetContext.cacheDir)

        val result = ContainerUtil.addExtensionToContainerFilename(testFile.name)

        assertEquals(testFile.name, result)
        testFile.delete()
    }

    @Test
    fun containerUtil_removeExtensionFromContainerFilename_success() {
        val result = ContainerUtil.removeExtensionFromContainerFilename("testFile.name")

        assertEquals("testFile", result)
    }

    @Test
    fun containerUtil_getFilesWithValidSize_success() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testFile = File.createTempFile("testFile", ".txt")
        FileWriter(testFile).use { writer ->
            writer.write("Test content")
        }
        val anotherTestFile = File.createTempFile("testFile", ".txt")

        `when`(mockContext.filesDir).thenReturn(testFile.parentFile)
        `when`(mockContext.cacheDir).thenReturn(targetContext.cacheDir)

        val result =
            ContainerUtil.getFilesWithValidSize(
                listOf(FileStream.create(testFile), FileStream.create(anotherTestFile)),
            )

        assertEquals(1, result.size)
        testFile.delete()
    }

    @Test
    fun containerUtil_parseUris_success() {
        val contentResolver = mock(ContentResolver::class.java)
        val uris = listOf(mock(Uri::class.java), mock(Uri::class.java))
        val result = ContainerUtil.parseUris(contentResolver, uris)

        assertEquals(2, result.size)
    }

    @Test
    fun containerUtil_generateSignatureContainerFile_success() =
        runBlocking {
            val mockContext = mock(Context::class.java)
            val mockDirectoryPath = File("/path/to/mockDirectory")
            val mockFileName = "test.txt"
            val mockFileInDirectoryPath = File("$mockDirectoryPath/signed_containers/$mockFileName")

            val mockFile = mock(File::class.java)
            `when`(mockContext.filesDir).thenReturn(mockDirectoryPath)
            `when`(mockFile.name).thenReturn(mockFileName)
            `when`(mockFile.parentFile).thenReturn(mockDirectory)

            val containerFile = ContainerUtil.generateSignatureContainerFile(mockContext, mockFile.name)

            assertNotNull(containerFile)
            assertEquals(mockFileName, containerFile.name)
            assertEquals(mockFileInDirectoryPath.path, containerFile.path)
        }

    @Test
    fun containerUtil_generateSignatureContainerFile_successWithExistingFileWithSameName(): Unit =
        runBlocking {
            val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
            val signedContainersDirectory = File(targetContext.filesDir, DIR_SIGNATURE_CONTAINERS)
            val testFile = File(signedContainersDirectory, "test.txt")
            testFile.mkdirs()

            val containerFile = ContainerUtil.generateSignatureContainerFile(targetContext, testFile.name)

            testFile.delete()
            signedContainersDirectory.delete()

            assertNotNull(containerFile)
            assertEquals("test (1).txt", containerFile.name)
            assertEquals(File(signedContainersDirectory, "test (1).txt").path, containerFile.path)
        }

    @Test
    fun containerUtil_generateSignatureContainerFile_successCreatingDirectories() =
        runBlocking {
            val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
            val mockDirectory = File(targetContext.filesDir, "mockDirectory")
            mockDirectory.mkdirs()
            val expectedFile = File.createTempFile("testFile", ".txt", mockDirectory)

            `when`(mockContext.filesDir).thenReturn(File(targetContext.filesDir, mockDirectory.name))

            ContainerUtil.generateSignatureContainerFile(mockContext, "test.txt")

            expectedFile.delete()
            mockDirectory.delete()

            assertTrue(expectedFile.parentFile?.exists() ?: false)
        }
}
