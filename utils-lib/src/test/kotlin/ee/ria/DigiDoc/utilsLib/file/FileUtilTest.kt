/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

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
    fun fileUtil_getExternalFileUris_successWithContentSchemeUri() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)
            val mockUri = mock(Uri::class.java)

            `when`(mockUri.scheme).thenReturn("content")
            `when`(mockIntent.data).thenReturn(mockUri)

            val externalFileUris = FileUtil.getExternalFileUris(mockIntent)

            assertEquals(1, externalFileUris.size)
            assertEquals(mockUri, externalFileUris.first())
        }

    @Test
    fun fileUtil_getExternalFileUris_successWithFileSchemeUri() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)
            val mockUri = mock(Uri::class.java)

            `when`(mockUri.scheme).thenReturn("file")
            `when`(mockIntent.data).thenReturn(mockUri)

            val externalFileUris = FileUtil.getExternalFileUris(mockIntent)

            assertEquals(1, externalFileUris.size)
            assertEquals(mockUri, externalFileUris.first())
        }

    @Test
    fun fileUtil_getExternalFileUris_excludesHttpsSchemeUri() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)
            val mockUri = mock(Uri::class.java)

            `when`(mockUri.scheme).thenReturn("https")
            `when`(mockIntent.data).thenReturn(mockUri)

            val externalFileUris = FileUtil.getExternalFileUris(mockIntent)

            assertEquals(0, externalFileUris.size)
        }

    @Test
    fun fileUtil_getExternalFileUris_excludesCustomSchemeUri() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)
            val mockUri = mock(Uri::class.java)

            `when`(mockUri.scheme).thenReturn("web-eid-mobile")
            `when`(mockIntent.data).thenReturn(mockUri)

            val externalFileUris = FileUtil.getExternalFileUris(mockIntent)

            assertEquals(0, externalFileUris.size)
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

            `when`(mockUri1.scheme).thenReturn("content")
            `when`(mockUri2.scheme).thenReturn("file")
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
    fun fileUtil_getExternalFileUris_clipDataFiltersOutNonAllowedSchemes() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)
            val mockClipData = mock(ClipData::class.java)
            val mockClipDataItem1 = mock(ClipData.Item::class.java)
            val mockClipDataItem2 = mock(ClipData.Item::class.java)
            val mockUri1 = mock(Uri::class.java)
            val mockUri2 = mock(Uri::class.java)

            `when`(mockUri1.scheme).thenReturn("content")
            `when`(mockUri2.scheme).thenReturn("https")
            `when`(mockClipDataItem1.uri).thenReturn(mockUri1)
            `when`(mockClipDataItem2.uri).thenReturn(mockUri2)

            `when`(mockClipData.getItemAt(0)).thenReturn(mockClipDataItem1)
            `when`(mockClipData.getItemAt(1)).thenReturn(mockClipDataItem2)
            `when`(mockClipData.itemCount).thenReturn(2)

            `when`(mockIntent.clipData).thenReturn(mockClipData)

            val externalFileUris = FileUtil.getExternalFileUris(mockIntent)

            assertEquals(1, externalFileUris.size)
            assertEquals(mockUri1, externalFileUris.first())
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
