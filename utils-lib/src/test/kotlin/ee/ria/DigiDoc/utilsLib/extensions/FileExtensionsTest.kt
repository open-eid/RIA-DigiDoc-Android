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

package ee.ria.DigiDoc.utilsLib.extensions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileExtensionsTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun zipContaining(vararg entryNames: String): File {
        val zipFile = temporaryFolder.newFile("container-${entryNames.size}-${entryNames.hashCode()}.asics")
        ZipOutputStream(zipFile.outputStream()).use { zip ->
            entryNames.forEach { name ->
                zip.putNextEntry(ZipEntry(name))
                zip.write("content".toByteArray())
                zip.closeEntry()
            }
        }
        return zipFile
    }

    @Test
    fun fileExtensions_containsDdoc_returnTrueWhenContainerHasDdocEntry() {
        val container = zipContaining("mimetype", "sample.ddoc")

        assertTrue(container.containsDdoc())
    }

    @Test
    fun fileExtensions_containsDdoc_returnFalseWhenContainerHasNoDdocEntry() {
        val container = zipContaining("mimetype", "sample.bdoc", "META-INF/signatures0.xml")

        assertFalse(container.containsDdoc())
    }

    @Test
    fun fileExtensions_containsDdoc_returnTrueWhenDdocExtensionIsUppercase() {
        val container = zipContaining("SAMPLE.DDOC")

        assertTrue(container.containsDdoc())
    }

    @Test
    fun fileExtensions_containsDdoc_returnTrueWhenDdocIsInNestedDirectory() {
        val container = zipContaining("nested/folder/sample.ddoc")

        assertTrue(container.containsDdoc())
    }

    @Test
    fun fileExtensions_containsDdoc_returnFalseWhenNameOnlyContainsDdocAsSubstring() {
        val container = zipContaining("notaddoc.txt", "ddoc.xml")

        assertFalse(container.containsDdoc())
    }

    @Test
    fun fileExtensions_containsDdoc_returnFalseWhenFileIsNotZip() {
        val notAZip = temporaryFolder.newFile("plain.ddoc")
        notAZip.writeText("this is not a zip archive")

        assertFalse(notAZip.containsDdoc())
    }

    @Test
    fun fileExtensions_containsDdoc_returnFalseWhenContainerIsEmpty() {
        val container = zipContaining()

        assertFalse(container.containsDdoc())
    }
}
