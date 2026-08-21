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
import android.webkit.URLUtil
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FilenameUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
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

        mockStatic(FilenameUtils::class.java).use { mockedFilenameUtils ->
            mockedFilenameUtils.`when`<String> { FilenameUtils.getName(fileName) }.thenReturn(expectedName)

            val name = FileUtil.getNameFromFileName(fileName)

            assertEquals(expectedName, name)
        }
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
    fun fileUtil_getExternalFileUris_returnsSingleUriWhenFileInBothDataAndClipData() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)
            val mockClipData = mock(ClipData::class.java)
            val mockClipDataItem = mock(ClipData.Item::class.java)
            val mockUri = mock(Uri::class.java)

            `when`(mockUri.scheme).thenReturn("content")
            `when`(mockIntent.data).thenReturn(mockUri)
            `when`(mockClipDataItem.uri).thenReturn(mockUri)
            `when`(mockClipData.getItemAt(0)).thenReturn(mockClipDataItem)
            `when`(mockClipData.itemCount).thenReturn(1)
            `when`(mockIntent.clipData).thenReturn(mockClipData)

            val externalFileUris = FileUtil.getExternalFileUris(mockIntent)

            assertEquals(1, externalFileUris.size)
            assertEquals(mockUri, externalFileUris.first())
        }

    @Test
    fun fileUtil_getExternalFileUris_fallsBackToExtraStreamForActionSend() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)
            val mockUri = mock(Uri::class.java)

            `when`(mockUri.scheme).thenReturn("content")
            `when`(mockIntent.action).thenReturn(Intent.ACTION_SEND)
            `when`(mockIntent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)).thenReturn(mockUri)

            val externalFileUris = FileUtil.getExternalFileUris(mockIntent)

            assertEquals(1, externalFileUris.size)
            assertEquals(mockUri, externalFileUris.first())
        }

    @Test
    fun fileUtil_getExternalFileUris_fallsBackToExtraStreamForActionSendMultiple() =
        runBlocking {
            val mockIntent = mock(Intent::class.java)
            val mockUri1 = mock(Uri::class.java)
            val mockUri2 = mock(Uri::class.java)

            `when`(mockUri1.scheme).thenReturn("content")
            `when`(mockUri2.scheme).thenReturn("content")
            `when`(mockIntent.action).thenReturn(Intent.ACTION_SEND_MULTIPLE)
            `when`(mockIntent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java))
                .thenReturn(arrayListOf(mockUri1, mockUri2))

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

    @Test
    fun fileUtil_sanitizeString_keepsCharactersThatFileSystemsAllow() {
        mockStatic(URLUtil::class.java).use { urlUtil ->
            urlUtil.`when`<Boolean> { URLUtil.isValidUrl(anyString()) }.thenReturn(false)

            val fileName = "pikk nimi2 !#\u00a4%&=`@\u00a3\${[]}\u00bd';,\u00a7^\u00d6\u00f6.txt"

            assertEquals(fileName, FileUtil.sanitizeString(fileName, ""))
        }
    }

    @Test
    fun fileUtil_sanitizeString_removesCharactersFileSystemsRejectAndKeepsZeroWidthJoiner() {
        mockStatic(URLUtil::class.java).use { urlUtil ->
            urlUtil.`when`<Boolean> { URLUtil.isValidUrl(anyString()) }.thenReturn(false)

            val rightToLeftOverride = Char(0x202E)
            val zeroWidthJoiner = Char(0x200D)
            val fileName = "na<m>e:wi\"th|ba?d*chars" + rightToLeftOverride + zeroWidthJoiner + ".txt"

            assertEquals("namewithbadchars" + zeroWidthJoiner + ".txt", FileUtil.sanitizeString(fileName, ""))
        }
    }

    @Test
    fun fileUtil_sanitizeString_returnsDefaultNameWhenEverythingIsRemoved() {
        mockStatic(URLUtil::class.java).use { urlUtil ->
            urlUtil.`when`<Boolean> { URLUtil.isValidUrl(anyString()) }.thenReturn(false)

            assertEquals("newFile", FileUtil.sanitizeString("<>:|?*", ""))
        }
    }

    @Test
    fun fileUtil_sanitizeString_shortensTooLongNameAndKeepsExtension() {
        mockStatic(URLUtil::class.java).use { urlUtil ->
            urlUtil.`when`<Boolean> { URLUtil.isValidUrl(anyString()) }.thenReturn(false)

            val fileName = "\u00f5".repeat(200) + ".txt"

            val name = FileUtil.sanitizeString(fileName, "")

            assertTrue(name.toByteArray().size <= 230)
            assertTrue(name.endsWith(".txt"))
            assertTrue(fileName.startsWith(name.removeSuffix(".txt")))
        }
    }

    @Test
    fun fileUtil_sanitizeString_keepsOnlyTheNameOfARawUrl() {
        assertEquals(
            "test.txt",
            FileUtil.sanitizeString("raw:/storage/emulated/0/Download/test.txt", ""),
        )
    }

    @Test
    fun fileUtil_sanitizeString_doesNotLetARawUrlEscapeTheDirectory() {
        assertEquals(
            "test.txt",
            FileUtil.sanitizeString("raw:/storage/emulated/0/../../test.txt", ""),
        )
    }

    @Test
    fun fileUtil_sanitizeString_removesSpaceLeftBehindByARemovedCharacter() {
        mockStatic(URLUtil::class.java).use { urlUtil ->
            urlUtil.`when`<Boolean> { URLUtil.isValidUrl(anyString()) }.thenReturn(false)

            assertEquals("report", FileUtil.sanitizeString("report *", ""))
        }
    }

    @Test
    fun fileUtil_sanitizeString_leavesRoomForTheNamesTheAppBuildsFromIt() {
        mockStatic(URLUtil::class.java).use { urlUtil ->
            urlUtil.`when`<Boolean> { URLUtil.isValidUrl(anyString()) }.thenReturn(false)

            val sanitized = FileUtil.sanitizeString("\u00f5".repeat(200) + ".cdoc2", "")
            val duplicate = FilenameUtils.getBaseName(sanitized) + " (99)." + FilenameUtils.getExtension(sanitized)
            val dataFileDirectory = "$duplicate-data-files9"

            assertTrue(sanitized.toByteArray().size <= 230)
            assertTrue(duplicate.toByteArray().size <= 255)
            assertTrue(
                "directory name is ${dataFileDirectory.toByteArray().size} bytes",
                dataFileDirectory.toByteArray().size <= 255,
            )
        }
    }

    @Test
    fun fileUtil_truncateFileName_returnsSameNameWhenItFits() {
        assertEquals("test.txt", FileUtil.truncateFileName("test.txt", 240))
    }

    @Test
    fun fileUtil_truncateFileName_cutsBetweenCharactersNotBytes() {
        val fileName = "\u03b1".repeat(20) + ".txt"

        val name = FileUtil.truncateFileName(fileName, 20)

        assertEquals("\u03b1\u03b1\u03b1\u03b1\u03b1\u03b1\u03b1\u03b1.txt", name)
        assertEquals(20, name.toByteArray().size)
    }

    @Test
    fun fileUtil_truncateFileName_dropsExtensionThatDoesNotFit() {
        val name = FileUtil.truncateFileName("name.extensionthatistoolong", 10)

        assertEquals("name.exten", name)
    }

    @Test
    fun fileUtil_truncateFileName_dropsExtensionThatWouldLeaveNoName() {
        val name = FileUtil.truncateFileName("name." + "test".repeat(30), 20)

        assertTrue(name.isNotEmpty())
        assertTrue(!name.startsWith("."))
        assertEquals(20, name.toByteArray().size)
    }

    @Test
    fun fileUtil_truncateFileName_keepsStartOfNameWhenOnlyExtensionWouldFit() {
        val name = FileUtil.truncateFileName("\u00e4\u00e4\u00e4." + "test".repeat(17), 20)

        assertTrue(name.isNotEmpty())
        assertTrue(!name.startsWith("."))
        assertTrue(name.toByteArray().size <= 20)
    }

    @Test
    fun fileUtil_truncateFileName_staysWithinBudgetSmallerThanTheExtension() {
        val name = FileUtil.truncateFileName("\u00e4.\u0424\u0430\u0439\u043b\u0420\u0430\u0441\u0448", 5)

        assertTrue(name.toByteArray().size <= 5)
    }

    @Test
    fun fileUtil_truncateFileName_fallsBackToDefaultNameWhenNoCharacterFits() {
        val family = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67"

        val name = FileUtil.truncateFileName("$family.txt", 12)

        assertEquals("newFile.txt", name)
        assertTrue(name.toByteArray().size <= 12)
    }

    @Test
    fun fileUtil_truncateFileName_doesNotSplitCharacterMadeOfSeveralCodeUnits() {
        val emoji = "\uD83D\uDE00"

        val name = FileUtil.truncateFileName(emoji + emoji + ".txt", 9)

        assertEquals(emoji + ".txt", name)
    }

    @Test
    fun fileUtil_truncateFileName_doesNotSplitAccentFromItsLetter() {
        val letterWithAccent = "e\u0301"

        val name = FileUtil.truncateFileName(letterWithAccent + letterWithAccent + ".txt", 8)

        assertEquals(letterWithAccent + ".txt", name)
    }

    @Test
    fun fileUtil_uniqueFileName_returnsSameNameWhenNotTaken() {
        assertEquals("test.txt", FileUtil.uniqueFileName("test.txt", setOf("other.txt")))
    }

    @Test
    fun fileUtil_uniqueFileName_addsCounterToTakenName() {
        assertEquals("test (1).txt", FileUtil.uniqueFileName("test.txt", setOf("test.txt")))
        assertEquals(
            "test (2).txt",
            FileUtil.uniqueFileName("test.txt", setOf("test.txt", "test (1).txt")),
        )
    }

    @Test
    fun fileUtil_uniqueFileName_usesDefaultNameWhenThereIsNoBaseName() {
        assertEquals("newFile (1).txt", FileUtil.uniqueFileName(".txt", setOf(".txt")))
    }

    @Test
    fun fileUtil_uniqueFileName_addsCounterToNameWithoutExtension() {
        assertEquals("test (1)", FileUtil.uniqueFileName("test", setOf("test")))
    }
}
