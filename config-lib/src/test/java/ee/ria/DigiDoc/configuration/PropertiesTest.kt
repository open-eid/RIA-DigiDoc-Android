@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import android.content.res.AssetManager
import ee.ria.DigiDoc.configuration.utils.Constant.PROPERTIES_FILE_NAME
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException
import java.io.InputStream
import java.util.Objects
import java.util.Optional

@RunWith(MockitoJUnitRunner::class)
class PropertiesTest {
    @Mock
    private val assetManager: AssetManager? = null

    @Test
    @Throws(IOException::class)
    fun loadProperties() {
        val classLoader =
            Optional.ofNullable(javaClass.getClassLoader())
                .orElseThrow {
                    IllegalStateException(
                        "Unable to get ClassLoader",
                    )
                }
        Objects.requireNonNull<InputStream>(
            classLoader.getResourceAsStream(PROPERTIES_FILE_NAME),
            "Unable to open properties file",
        ).use { inputStream ->
            `when`(assetManager!!.open(anyString())).thenReturn(inputStream)
            val configurationProperties =
                Properties(assetManager)
            assertEquals(
                "https://id.eesti.ee/",
                configurationProperties.centralConfigurationServiceUrl,
            )
            assertSame(4, configurationProperties.configurationUpdateInterval)
        }
    }
}
