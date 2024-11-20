@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.file

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FilenameUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
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

    @Test
    fun fileUtil_getExternalFileUris_successWithSingleUriIntentData() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)
            val mockUri = mock(Uri::class.java)

            `when`(mockIntent.data).thenReturn(mockUri)

            val externalFileUris = FileUtil.getExternalFileUris(mockIntent)

            assertEquals(1, externalFileUris.size)
            assertEquals(externalFileUris.first(), mockIntent.data)
        }

    @Test
    fun fileUtil_getExternalFileUris_returnEmptyListWithoutUriIntentData() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)

            val externalFileUris = FileUtil.getExternalFileUris(mockIntent)

            assertEquals(0, externalFileUris.size)
            assertEquals(listOf<Uri>(), externalFileUris)
        }

    @Test
    fun fileUtil_getExternalFileUris_successWithMultipleUrisIntentClipData() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)
            val mockClipData = mock(ClipData::class.java)
            val mockClipDataItem1 = mock(ClipData.Item::class.java)
            val mockClipDataItem2 = mock(ClipData.Item::class.java)
            val mockUri1 = mock(Uri::class.java)
            val mockUri2 = mock(Uri::class.java)

            `when`(mockClipDataItem1.uri).thenReturn(mockUri1)
            `when`(mockClipDataItem2.uri).thenReturn(mockUri2)

            `when`(mockClipData.getItemAt(0)).thenReturn(mockClipDataItem1)
            `when`(mockClipData.getItemAt(1)).thenReturn(mockClipDataItem2)
            `when`(mockClipData.itemCount).thenReturn(2)

            `when`(mockIntent.clipData).thenReturn(mockClipData)

            val externalFileUris = FileUtil.getExternalFileUris(mockIntent)

            assertEquals(2, externalFileUris.size)
            assertEquals(mockUri1, externalFileUris.first())
            assertEquals(mockUri2, externalFileUris.last())
        }

    @Test
    fun fileUtil_getExternalFileUris_returnEmptyListWithoutIntentClipData() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)
            val mockClipData = mock(ClipData::class.java)

            `when`(mockIntent.clipData).thenReturn(mockClipData)

            val externalFileUris = FileUtil.getExternalFileUris(mockIntent)

            assertEquals(0, externalFileUris.size)
            assertEquals(listOf<Uri>(), externalFileUris)
        }
}
