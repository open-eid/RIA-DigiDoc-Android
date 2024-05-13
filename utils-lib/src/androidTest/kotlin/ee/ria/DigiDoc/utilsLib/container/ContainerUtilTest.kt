@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.Constant.DIR_SIGNATURE_CONTAINERS
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
