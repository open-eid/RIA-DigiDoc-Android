@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.file

import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FilenameUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mockStatic
import java.io.File
import java.io.IOException

class FileUtilTest {
    @Mock
    private lateinit var expectedName: String

    @Before
    fun setUp() {
        expectedName = "test.txt"
    }

    @Test
    fun fileUtil_getNameFromFileName_success() {
        val fileName = "test.txt"

        val mockedFilenameUtils = mockStatic(FilenameUtils::class.java)
        mockedFilenameUtils.`when`<String> { FilenameUtils.getName(fileName) }.thenReturn(expectedName)

        val name = FileUtil.getNameFromFileName(fileName)

        assertEquals(expectedName, name)
    }

    @Test
    fun fileUtil_getFileInDirectory_success() =
        runBlocking {
            val file = File("/path/to/directory/test.txt")
            val directory = File("/path/to/directory")

            val result = FileUtil.getFileInDirectory(file, directory)

            assertEquals(file, result)
        }

    @Test(expected = IOException::class)
    fun fileUtil_getFileInDirectory_throwExceptionWithInvalidInput(): Unit =
        runBlocking {
            val file = File("/invalid/path/to/file.txt")
            val directory = File("/path/to/directory")

            FileUtil.getFileInDirectory(file, directory)
        }
}
