@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

import android.content.Context
import android.content.SharedPreferences
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.properties.ConfigurationProperties
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ConfigurationUtilTest {
    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var configurationLoader: ConfigurationLoader

    @Mock
    lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    lateinit var configurationProperties: ConfigurationProperties

    private lateinit var configurationFlow: MutableStateFlow<ConfigurationProvider?>

    @Before
    fun setup() {
        configurationFlow = MutableStateFlow(null)
        `when`(configurationLoader.getConfigurationFlow()).thenReturn(configurationFlow)
    }

    @Test
    fun configurationUtil_isSerialNewerThanCached_returnTrueWhenNoCachedSerial() {
        val result = ConfigurationUtil.isSerialNewerThanCached(null, 10)
        assertTrue(result)
    }

    @Test
    fun configurationUtil_isSerialNewerThanCached_returnTrueWithNewerSerial() {
        val result = ConfigurationUtil.isSerialNewerThanCached(5, 10)
        assertTrue(result)
    }

    @Test
    fun configurationUtil_isSerialNewerThanCached_returnFalseWithOlderSerial() {
        val result = ConfigurationUtil.isSerialNewerThanCached(10, 5)
        assertFalse(result)
    }

    @Test
    fun configurationUtil_isBase64_returnTrueWithCorrectValue() {
        val validBase64 = "VGVzdA=="
        val result = ConfigurationUtil.isBase64(validBase64)
        assertTrue(result)
    }

    @Test
    fun configurationUtil_isBase64_returnFalseWithIncorrectValue() {
        val invalidBase64 = "NotBase64!#%"
        val result = ConfigurationUtil.isBase64(invalidBase64)
        assertFalse(result)
    }
}
