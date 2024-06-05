@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.utils

import android.content.Context
import android.content.res.Resources
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.getDataFileMimetype
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.getSchemaDir
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.getSchemaPath
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.initSchema
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipOutputStream
import kotlin.io.path.createTempDirectory

class FileUtilsTest {
    private val mockCacheDir = createTempDirectory("tempDirectory").toFile()
    private lateinit var context: Context
    private lateinit var resources: Resources

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val configurationRepository = mock(ConfigurationRepository::class.java)
                    Initialization(configurationRepository)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        resources = mock(Resources::class.java)
        `when`(context.cacheDir).thenReturn(mockCacheDir)
        `when`(context.resources).thenReturn(resources)
    }

    @Test
    fun fileUtils_initSchema_success() {
        `when`(context.cacheDir).thenReturn(mockCacheDir)

        val schemaDir = File(mockCacheDir, "schema")
        schemaDir.mkdirs()
        val inputStream =
            createZipInputStream(mapOf("tempFile1.txt" to "TempContent1", "tempFile2.txt" to "TempContent2"))
        `when`(resources.openRawResource(anyInt())).thenReturn(inputStream)

        try {
            initSchema(context)
        } catch (e: IOException) {
            fail("Unable to initialize schema. ${e.message}")
        }

        assertTrue(File(schemaDir, "tempFile1.txt").exists())
        assertTrue(File(schemaDir, "tempFile2.txt").exists())
    }

    @Test(expected = Resources.NotFoundException::class)
    fun fileUtils_initSchema_resourceNotFound() {
        `when`(context.cacheDir).thenReturn(mockCacheDir)
        `when`(resources.openRawResource(anyInt())).thenThrow(Resources.NotFoundException::class.java)

        initSchema(context)
    }

    @Test(expected = ZipException::class)
    fun fileUtils_initSchema_badZipEntry() {
        val schemaDir = createTempDirectory("schema")
        val inputStream = createZipInputStream(mapOf("../file.txt" to "Content"))
        val cacheDir = File.createTempFile("tmp", null, schemaDir.toFile())

        `when`(context.cacheDir).thenReturn(cacheDir)
        `when`(resources.openRawResource(anyInt())).thenReturn(inputStream)

        try {
            initSchema(context)
        } catch (e: ZipException) {
            assertEquals(ZipException::class.java, e.javaClass)
            assertEquals("Bad zip entry: ../file.txt", e.message)
            throw e
        }
    }

    @Test
    fun fileUtils_getSchemaPath_success() {
        val expectedPath = File(mockCacheDir, "schema").absolutePath
        val actualPath = getSchemaPath(context)
        assertEquals(expectedPath, actualPath)
    }

    @Test
    fun fileUtils_getSchemaDir_successWhenSchemaDirAlreadyExists() {
        val schemaDir = getSchemaDir(context)
        assertEquals(File(mockCacheDir, "schema"), schemaDir)
    }

    @Test
    fun fileUtils_getSchemaDir_successWhenSchemaDirIsCreated() {
        val schemaDir = getSchemaDir(context)
        assertEquals(File(mockCacheDir, "schema"), schemaDir)
    }

    @Test
    fun fileUtils_getSchemaDir_throwsExceptionWhenCacheDirIsNull() {
        `when`(context.cacheDir).thenReturn(null)
        try {
            getSchemaDir(context)
        } catch (e: IllegalArgumentException) {
            assertEquals("Cache directory is null", e.message)
            return
        }
        fail("Must throw exception when cacheDir is null")
    }

    @Test
    fun signedContainer_mimeType_returnMimeType() =
        runTest {
            val file = File.createTempFile("testFile", ".bmp", context.filesDir)
            val dataFiles = listOf(file)

            val signedContainer = SignedContainer.openOrCreate(context, file, dataFiles)

            val result = getDataFileMimetype(signedContainer.getDataFiles().first())

            assertEquals("image/x-ms-bmp", result)
        }

    @Test
    fun signedContainer_mimeType_extensionEmptyReturnMimeType() =
        runTest {
            val file = File.createTempFile("testFile", "", context.filesDir)
            val dataFiles = listOf(file)

            val signedContainer = SignedContainer.openOrCreate(context, file, dataFiles)

            val result = getDataFileMimetype(signedContainer.getDataFiles().first())

            assertEquals("application/octet-stream", result)
        }

    private fun createZipInputStream(files: Map<String, String>): InputStream {
        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { zipOutputStream ->
            for ((name, content) in files) {
                zipOutputStream.putNextEntry(ZipEntry(name))
                zipOutputStream.write(content.toByteArray())
                zipOutputStream.closeEntry()
            }
        }
        return ByteArrayInputStream(outputStream.toByteArray())
    }
}
