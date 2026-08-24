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
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_PUB
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_RSA
import ee.ria.DigiDoc.configuration.utils.Constant.CACHE_CONFIG_FOLDER
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
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
    fun configurationCache_cacheConfigurationFiles_writesAllThreeFiles() {
        ConfigurationCache.cacheConfigurationFiles(context, "{}", "public-key", byteArrayOf(1, 2, 3))

        assertEquals(3, configDir.listFiles()?.size ?: 0)
        assertEquals("{}", File(configDir, CACHED_CONFIG_JSON).readText())
        assertEquals("public-key", File(configDir, CACHED_CONFIG_PUB).readText())
        assertArrayEquals(byteArrayOf(1, 2, 3), File(configDir, CACHED_CONFIG_RSA).readBytes())
    }

    @Test
    fun configurationCache_cacheConfigurationFiles_leavesNoTemporaryFilesBehind() {
        ConfigurationCache.cacheConfigurationFiles(context, "{}", "public-key", byteArrayOf(1))

        assertTrue("Temporary files were left behind", temporaryFilesIn(configDir).isEmpty())
    }

    @Test
    fun configurationCache_cacheConfigurationFiles_replacesThePreviousSetCompletely() {
        ConfigurationCache.cacheConfigurationFiles(context, "{\"old\":1}", "old-key", byteArrayOf(9))

        ConfigurationCache.cacheConfigurationFiles(context, "{\"new\":2}", "new-key", byteArrayOf(8))

        assertEquals("{\"new\":2}", File(configDir, CACHED_CONFIG_JSON).readText())
        assertEquals("new-key", File(configDir, CACHED_CONFIG_PUB).readText())
        assertArrayEquals(byteArrayOf(8), File(configDir, CACHED_CONFIG_RSA).readBytes())
    }

    @Test
    fun configurationCache_cacheConfigurationFiles_keepsThePreviousSetWhenTheWriteFails() {
        ConfigurationCache.cacheConfigurationFiles(context, "{\"old\":1}", "old-key", byteArrayOf(9))

        assertTrue(configDir.setWritable(false))
        try {
            assertThrows(IOException::class.java) {
                ConfigurationCache.cacheConfigurationFiles(context, "{\"new\":2}", "new-key", byteArrayOf(8))
            }
        } finally {
            assertTrue(configDir.setWritable(true))
        }

        assertEquals("{\"old\":1}", File(configDir, CACHED_CONFIG_JSON).readText())
        assertEquals("old-key", File(configDir, CACHED_CONFIG_PUB).readText())
        assertArrayEquals(byteArrayOf(9), File(configDir, CACHED_CONFIG_RSA).readBytes())
    }

    @Test
    fun configurationCache_cacheConfigurationFiles_reportsAFailureInTheRenamePhase() {
        ConfigurationCache.cacheConfigurationFiles(context, "{\"old\":1}", "old-key", byteArrayOf(9))

        val blocked = File(configDir, CACHED_CONFIG_PUB)
        assertTrue(blocked.delete())
        assertTrue(blocked.mkdirs())

        assertThrows(IOException::class.java) {
            ConfigurationCache.cacheConfigurationFiles(context, "{\"new\":2}", "new-key", byteArrayOf(8))
        }

        assertTrue("Temporary files were left behind", temporaryFilesIn(configDir).isEmpty())
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

    private fun temporaryFilesIn(directory: File): List<File> =
        directory.listFiles()?.filter { it.name.endsWith(".tmp") } ?: emptyList()
}
