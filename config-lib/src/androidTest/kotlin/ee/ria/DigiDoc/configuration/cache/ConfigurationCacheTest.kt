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
import ee.ria.DigiDoc.configuration.utils.Constant.CACHE_CONFIG_FOLDER
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileNotFoundException
import kotlin.io.path.createTempDirectory

@RunWith(MockitoJUnitRunner::class)
class ConfigurationCacheTest {
    @Mock
    lateinit var context: Context

    @Mock
    lateinit var configDir: File

    private val testStringData = "testData"
    private val testByteArrayData = "testData".toByteArray()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(context.cacheDir).thenReturn(createTempDirectory("cacheDirectory").toFile())
        `when`(configDir.exists()).thenReturn(false)
        `when`(configDir.mkdirs()).thenReturn(true)
    }

    @Test
    fun configurationCache_cacheConfigurationFiles_success() {
        ConfigurationCache.cacheConfigurationFiles(context, testStringData, testStringData, testByteArrayData)

        assertEquals(3, File(context.cacheDir, CACHE_CONFIG_FOLDER).listFiles()?.size ?: 0)
        assertTrue(File(File(context.cacheDir, CACHE_CONFIG_FOLDER), "active-config.json").exists())
        assertTrue(File(File(context.cacheDir, CACHE_CONFIG_FOLDER), "active-config.pub").exists())
        assertTrue(File(File(context.cacheDir, CACHE_CONFIG_FOLDER), "active-config.rsa").exists())
    }

    @Test
    fun configurationCache_getCachedFile_success() {
        val cacheConfigFolder = File(context.cacheDir, CACHE_CONFIG_FOLDER)
        cacheConfigFolder.mkdirs()
        val existingFile = File.createTempFile("config", ".json", cacheConfigFolder)

        val cachedFile = ConfigurationCache.getCachedFile(context, existingFile.name)
        assertNotNull(cachedFile)
        assertEquals(existingFile.name, cachedFile.name)
        assertEquals(existingFile.length(), cachedFile.length())

        existingFile.delete()
        cacheConfigFolder.delete()
    }

    @Test(expected = FileNotFoundException::class)
    fun configurationCache_getCachedFile_throwsFileNotFoundException() {
        ConfigurationCache.getCachedFile(context, "nonExistent.txt")
    }
}
