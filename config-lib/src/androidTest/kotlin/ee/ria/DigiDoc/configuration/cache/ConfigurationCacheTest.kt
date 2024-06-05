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
