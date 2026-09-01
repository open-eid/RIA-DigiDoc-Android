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

package ee.ria.DigiDoc.configuration.cache

import android.content.Context
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_ECC
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.CACHE_CONFIG_FOLDER
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files

@RunWith(MockitoJUnitRunner::class)
class ConfigurationCacheTest {
    @Mock
    lateinit var context: Context

    private lateinit var cacheDir: File

    private val configDir: File
        get() = File(cacheDir, CACHE_CONFIG_FOLDER)

    @Before
    fun setUp() {
        cacheDir = Files.createTempDirectory("configuration-cache-test").toFile()
        cacheDir.deleteOnExit()
        `when`(context.cacheDir).thenReturn(cacheDir)
    }

    @Test
    fun configurationCache_cacheConfigurationFiles_writesBothFiles() {
        ConfigurationCache.cacheConfigurationFiles(context, "{}", byteArrayOf(1, 2, 3))

        assertEquals(2, configDir.listFiles()?.size ?: 0)
        assertEquals("{}", File(configDir, CACHED_CONFIG_JSON).readText())
        assertArrayEquals(byteArrayOf(1, 2, 3), File(configDir, CACHED_CONFIG_ECC).readBytes())
    }

    @Test
    fun configurationCache_cacheConfigurationFiles_leavesNoTemporaryOrBackupFilesBehind() {
        ConfigurationCache.cacheConfigurationFiles(context, "{}", byteArrayOf(1))

        assertTrue("Leftover files: ${leftoverFilesIn(configDir)}", leftoverFilesIn(configDir).isEmpty())
    }

    @Test
    fun configurationCache_cacheConfigurationFiles_replacesThePreviousSetCompletely() {
        ConfigurationCache.cacheConfigurationFiles(context, "{\"old\":1}", byteArrayOf(9))

        ConfigurationCache.cacheConfigurationFiles(context, "{\"new\":2}", byteArrayOf(8))

        assertEquals(2, configDir.listFiles()?.size ?: 0)
        assertEquals("{\"new\":2}", File(configDir, CACHED_CONFIG_JSON).readText())
        assertArrayEquals(byteArrayOf(8), File(configDir, CACHED_CONFIG_ECC).readBytes())
    }

    @Test
    fun configurationCache_cacheConfigurationFiles_keepsThePreviousSetWhenTheWriteFails() {
        ConfigurationCache.cacheConfigurationFiles(context, "{\"old\":1}", byteArrayOf(9))

        assertTrue(configDir.setWritable(false))
        try {
            assertThrows(IOException::class.java) {
                ConfigurationCache.cacheConfigurationFiles(context, "{\"new\":2}", byteArrayOf(8))
            }
        } finally {
            assertTrue(configDir.setWritable(true))
        }

        assertEquals("{\"old\":1}", File(configDir, CACHED_CONFIG_JSON).readText())
        assertArrayEquals(byteArrayOf(9), File(configDir, CACHED_CONFIG_ECC).readBytes())
        assertTrue("Leftover files: ${leftoverFilesIn(configDir)}", leftoverFilesIn(configDir).isEmpty())
    }

    @Test
    fun configurationCache_restorePreviousFiles_bringsBackEveryBackedUpFile() {
        assertTrue(configDir.mkdirs())
        val json = writeFile(CACHED_CONFIG_JSON, "{\"new\":2}")
        val signature = writeFile(CACHED_CONFIG_ECC, "new-signature")
        val jsonBackup = writeFile("$CACHED_CONFIG_JSON.bak", "{\"old\":1}")
        val signatureBackup = writeFile("$CACHED_CONFIG_ECC.bak", "old-signature")

        ConfigurationCache.restorePreviousFiles(
            listOf(json.toPath(), signature.toPath()),
            mapOf(json.toPath() to jsonBackup.toPath(), signature.toPath() to signatureBackup.toPath()),
        )

        assertEquals("{\"old\":1}", json.readText())
        assertEquals("old-signature", signature.readText())
        assertFalse(jsonBackup.exists())
        assertFalse(signatureBackup.exists())
        assertEquals(2, configDir.listFiles()?.size ?: 0)
    }

    @Test
    fun configurationCache_restorePreviousFiles_removesFilesThatHadNoPreviousVersion() {
        assertTrue(configDir.mkdirs())
        val json = writeFile(CACHED_CONFIG_JSON, "{\"new\":2}")

        ConfigurationCache.restorePreviousFiles(listOf(json.toPath()), emptyMap())

        assertFalse(json.exists())
        assertEquals(0, configDir.listFiles()?.size ?: 0)
    }

    @Test
    fun configurationCache_getCachedFile_success() {
        assertTrue(configDir.mkdirs())
        val existingFile = File.createTempFile("config", ".json", configDir)

        val cachedFile = ConfigurationCache.getCachedFile(context, existingFile.name)

        assertNotNull(cachedFile)
        assertEquals(existingFile.name, cachedFile.name)
        assertEquals(existingFile.length(), cachedFile.length())
    }

    @Test
    fun configurationCache_getCachedFile_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException::class.java) {
            ConfigurationCache.getCachedFile(context, "nonExistent.txt")
        }
    }

    @Test
    fun configurationCache_cacheConfigurationFiles_throwsWhenTheCacheDirectoryCannotBeCreated() {
        assertTrue(File(cacheDir, "config").createNewFile())

        assertThrows(IOException::class.java) {
            ConfigurationCache.cacheConfigurationFiles(context, "{}", byteArrayOf(1))
        }
    }

    @Test
    fun configurationCache_getCachedFile_throwsWhenThePathIsADirectory() {
        assertTrue(File(configDir, "a-directory").mkdirs())

        assertThrows(FileNotFoundException::class.java) {
            ConfigurationCache.getCachedFile(context, "a-directory")
        }
    }

    private fun writeFile(
        fileName: String,
        content: String,
    ): File = File(configDir, fileName).apply { writeText(content) }

    private fun leftoverFilesIn(directory: File): List<File> =
        directory.listFiles()?.filter { it.name.endsWith(".tmp") || it.name.endsWith(".bak") } ?: emptyList()
}
