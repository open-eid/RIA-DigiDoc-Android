@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TSLUtilTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        val cacheDir = File(context.cacheDir, "schema")
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun tslUtil_setupTSLFiles_success() {
        TSLUtil.setupTSLFiles(context)

        val destinationDir = File(context.cacheDir, "schema")
        assertTrue(destinationDir.exists())
        assertTrue(destinationDir.isDirectory)
    }

    @Test(expected = IOException::class)
    fun tslUtil_setupTSLFiles_throwExceptionWhenCannotAccessAssets() {
        val mockContext = mock(Context::class.java)
        `when`(mockContext.assets).thenAnswer {
            throw IOException("Mocked exception")
        }
        TSLUtil.setupTSLFiles(mockContext)
    }

    @Test
    fun tslUtil_setupTSLFiles_overwriteExistingFileSuccess() {
        val schema = File(context.cacheDir, "schema")
        val eeFile = File(schema, "EE.xml")
        schema.mkdirs()
        eeFile.createNewFile()

        TSLUtil.setupTSLFiles(context)

        assertTrue(eeFile.exists())
        assertTrue(eeFile.isFile)
    }
}
