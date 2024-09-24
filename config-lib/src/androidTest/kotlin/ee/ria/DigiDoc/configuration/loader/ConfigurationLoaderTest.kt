@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.loader

import android.content.Context
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.io.Files
import com.google.gson.Gson
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifier
import ee.ria.DigiDoc.configuration.properties.ConfigurationProperties
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepository
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_PUB
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_RSA
import ee.ria.DigiDoc.configuration.utils.Constant.CACHE_CONFIG_FOLDER
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_PREFERENCES
import ee.ria.DigiDoc.configuration.utils.Constant.PROPERTIES_FILE_NAME
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.bouncycastle.util.encoders.Base64
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ConfigurationLoaderTest {
    private lateinit var context: Context
    private lateinit var gson: Gson

    private lateinit var configurationLoader: ConfigurationLoader

    @Mock
    private lateinit var configurationProperties: ConfigurationProperties

    @Mock
    private lateinit var centralConfigurationRepository: CentralConfigurationRepository

    @Mock
    private lateinit var configurationSignatureVerifier: ConfigurationSignatureVerifier

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    private lateinit var propertiesFile: File
    private lateinit var confFile: File
    private lateinit var publicKeyFile: File
    private lateinit var signatureFile: File

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        gson = Gson()
        val configurationProperty = ConfigurationProperty()
        configurationLoader =
            ConfigurationLoaderImpl(
                Gson(),
                centralConfigurationRepository,
                configurationProperty, ConfigurationPropertiesImpl(),
                configurationSignatureVerifier,
            )
        configurationProperties = mock(ConfigurationProperties::class.java)

        propertiesFile = AssetFile.getAssetFileAsFile(context, "config/configuration.properties")
        confFile = AssetFile.getAssetFileAsFile(context, "config/default-config.json")
        publicKeyFile = AssetFile.getAssetFileAsFile(context, "config/default-config.pub")
        signatureFile = AssetFile.getAssetFileAsFile(context, "config/default-config.rsa")

        File(context.cacheDir, CACHE_CONFIG_FOLDER).mkdirs()
        Files.copy(confFile, File(File(context.cacheDir, CACHE_CONFIG_FOLDER), CACHED_CONFIG_JSON))
        Files.copy(publicKeyFile, File(File(context.cacheDir, CACHE_CONFIG_FOLDER), CACHED_CONFIG_PUB))
        Files.copy(signatureFile, File(File(context.cacheDir, CACHE_CONFIG_FOLDER), CACHED_CONFIG_RSA))
    }

    @After
    fun tearDown() {
        File(context.cacheDir, PROPERTIES_FILE_NAME).delete()
        File(File(context.cacheDir, CACHE_CONFIG_FOLDER), CACHED_CONFIG_JSON).delete()
        File(File(context.cacheDir, CACHE_CONFIG_FOLDER), CACHED_CONFIG_PUB).delete()
        File(File(context.cacheDir, CACHE_CONFIG_FOLDER), CACHED_CONFIG_RSA).delete()
        File(context.cacheDir, CACHE_CONFIG_FOLDER).delete()
    }

    @Test
    fun configurationLoader_initConfiguration_success() =
        runBlocking {
            val confData = mockConfigurationProvider()

            val centralConfig = gson.toJson(confData)
            val centralSignature = "dGVzdA=="
            `when`(centralConfigurationRepository.fetchSignature()).thenReturn(centralSignature)
            `when`(centralConfigurationRepository.fetchConfiguration()).thenReturn(centralConfig)
            `when`(configurationProperties.getConfigurationProperties(context)).thenReturn(
                ConfigurationProperty(
                    "",
                    4,
                    1,
                    LocalDateTime.now(),
                ),
            )

            configurationLoader.initConfiguration(context)

            assertNotNull(configurationLoader.getConfigurationFlow())
        }

    @Test
    fun configurationLoader_loadConfigurationProperty_success() =
        runBlocking {
            val configurationProperty = configurationLoader.loadConfigurationProperty(context)

            assertNotNull(configurationProperty)
            assertEquals("https://id.eesti.ee", configurationProperty.centralConfigurationServiceUrl)
        }

    @Test
    fun configurationLoader_loadCachedConfiguration_success() =
        runBlocking {
            configurationLoader.loadCachedConfiguration(context, false)

            val value = configurationLoader.getConfigurationFlow().value
            assertNotNull(value)
            assertEquals(179, value?.metaInf?.serial)
        }

    @Test
    fun configurationLoader_loadDefaultConfiguration_success() =
        runBlocking {
            configurationLoader.loadDefaultConfiguration(context)

            val configurationProvider = configurationLoader.getConfigurationFlow().value

            assertNotNull(configurationProvider)
        }

    @Test
    fun configurationLoader_loadCentralConfiguration_success(): Unit =
        runBlocking {
            val confData = mockConfigurationProvider()

            val centralConfig = gson.toJson(confData)
            val centralPublicKey = "Public Key"
            val centralSignature = "dGVzdA=="
            `when`(centralConfigurationRepository.fetchConfiguration()).thenReturn(centralConfig)
            `when`(centralConfigurationRepository.fetchPublicKey()).thenReturn(centralPublicKey)
            `when`(centralConfigurationRepository.fetchSignature()).thenReturn(centralSignature)

            doNothing().`when`(configurationSignatureVerifier)
                .verifyConfigurationSignature(centralConfig, centralPublicKey, Base64.decode(centralSignature))

            `when`(configurationProperties.getConfigurationProperties(context)).thenReturn(
                ConfigurationProperty(
                    "",
                    4,
                    1,
                    LocalDateTime.now(),
                ),
            )

            // Write random text to file, to make sure that current signature and new signature are different
            val configFolder = File(context.cacheDir, CACHE_CONFIG_FOLDER)
            configFolder.mkdirs()
            val signatureFile = File(configFolder, CACHED_CONFIG_RSA)

            FileWriter(signatureFile).use { writer ->
                writer.write("dGVzdDI=")
            }

            configurationLoader.loadCentralConfiguration(context)

            assertNotNull(configurationLoader.getConfigurationFlow().value)

            signatureFile.delete()
            configFolder.delete()
        }

    @Test
    fun configurationLoader_loadCentralConfiguration_signaturesMatchLoadCachedConfiguration(): Unit =
        runBlocking {
            val confData = mockConfigurationProvider()

            val centralConfig = gson.toJson(confData)
            val centralPublicKey = "Public Key"
            val centralSignature = "dGVzdA=="
            `when`(centralConfigurationRepository.fetchConfiguration()).thenReturn(centralConfig)
            `when`(centralConfigurationRepository.fetchPublicKey()).thenReturn(centralPublicKey)
            `when`(centralConfigurationRepository.fetchSignature()).thenReturn(centralSignature)

            doNothing().`when`(configurationSignatureVerifier)
                .verifyConfigurationSignature(centralConfig, centralPublicKey, Base64.decode(centralSignature))

            `when`(configurationProperties.getConfigurationProperties(context)).thenReturn(
                ConfigurationProperty(
                    "/",
                    4,
                    1,
                    LocalDateTime.now(),
                ),
            )

            // Write same signature to file, to make sure that current signature and new signature match
            val configFolder = File(context.cacheDir, CACHE_CONFIG_FOLDER)
            configFolder.mkdirs()
            val signatureFile = File(configFolder, CACHED_CONFIG_RSA)
            FileWriter(signatureFile).use { writer ->
                writer.write(String(Base64.decode(centralSignature)))
            }

            configurationLoader.loadCentralConfiguration(context)

            assertNotNull(configurationLoader.getConfigurationFlow().value)

            signatureFile.delete()
            configFolder.delete()
        }

    @Test
    fun configurationLoader_loadCentralConfigurationData_success() =
        runBlocking {
            val confData = mockConfigurationProvider()

            val centralConfig = gson.toJson(confData)
            val centralPublicKey = "Public Key"
            val centralSignature = "dGVzdA=="
            `when`(centralConfigurationRepository.fetchConfiguration()).thenReturn(centralConfig)
            `when`(centralConfigurationRepository.fetchPublicKey()).thenReturn(centralPublicKey)
            `when`(centralConfigurationRepository.fetchSignature()).thenReturn(centralSignature)

            doNothing().`when`(configurationSignatureVerifier)
                .verifyConfigurationSignature(centralConfig, centralPublicKey, Base64.decode(centralSignature))

            val configurationData =
                configurationLoader
                    .loadCentralConfigurationData("serviceUri", "Tests")

            assertNotNull(configurationData)
        }

    @Test
    fun configurationLoader_shouldCheckForUpdates_returnTrueWhenEnoughDaysPassed() =
        runBlocking {
            val mockContext = mock(Context::class.java)
            val currentMinusTwoDays = LocalDateTime.now().minusDays(5)
            val currentMinusTwoDaysDate = Date.from(currentMinusTwoDays.atZone(ZoneId.systemDefault()).toInstant())
            val expectedLastUpdateDate = DateUtil.dateFormat.format(currentMinusTwoDaysDate)

            `when`(
                mockContext.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE),
            ).thenReturn(mockSharedPreferences)

            `when`(
                mockSharedPreferences.contains(CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME),
            ).thenReturn(true)

            `when`(
                mockSharedPreferences.getString(CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME, null),
            ).thenReturn(expectedLastUpdateDate)

            val result =
                configurationLoader.shouldCheckForUpdates(
                    mockContext,
                )
            assertTrue(result)
        }

    @Test
    fun configurationLoader_shouldCheckForUpdates_returnFalseWhenNotEnoughDaysPassed() =
        runBlocking {
            val mockContext = mock(Context::class.java)
            val currentMinusTwoDays = LocalDateTime.now().minusDays(2)
            val currentMinusTwoDaysDate = Date.from(currentMinusTwoDays.atZone(ZoneId.systemDefault()).toInstant())
            val expectedLastUpdateDate = DateUtil.dateFormat.format(currentMinusTwoDaysDate)

            `when`(
                mockContext.getSharedPreferences(CONFIGURATION_PREFERENCES, Context.MODE_PRIVATE),
            ).thenReturn(mockSharedPreferences)

            `when`(
                mockSharedPreferences.contains(CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME),
            ).thenReturn(true)

            `when`(
                mockSharedPreferences.getString(CONFIGURATION_LAST_UPDATE_CHECK_DATE_PROPERTY_NAME, null),
            ).thenReturn(expectedLastUpdateDate)

            `when`(
                configurationProperties.getConfigurationLastCheckDate(context),
            ).thenReturn(currentMinusTwoDaysDate)

            val result =
                configurationLoader.shouldCheckForUpdates(
                    mockContext,
                )
            assertFalse(result)
        }

    private fun mockConfigurationProvider(): ConfigurationProvider {
        return ConfigurationProvider(
            metaInf =
                ConfigurationProvider.MetaInf(
                    url = "https://www.example.com",
                    date = "2021-09-01",
                    serial = 1,
                    version = 1,
                ),
            sivaUrl = "https://www.example.com",
            tslUrl = "https://www.example.com",
            tslCerts =
                listOf(
                    "MIIDuzCCAqOgAwIBAgIUBkYXJdruP6EuH/+I4YoXxIQ3WcowDQYJKoZIhvcNAQELBQAw" +
                        "bTELMAkGA1UEBhMCRUUxDTALBgNVBAgMBFRlc3QxDTALBgNVBAcMBFRlc3QxDTALBgNV" +
                        "BAoMBFRlc3QxDTALBgNVBAsMBFRlc3QxDTALBgNVBAMMBFRlc3QxEzARBgkqhkiG9w0B" +
                        "CQEWBHRlc3QwHhcNMjQwNjEwMTI1OTA3WhcNMjUwNjEwMTI1OTA3WjBtMQswCQYDVQQG" +
                        "EwJFRTENMAsGA1UECAwEVGVzdDENMAsGA1UEBwwEVGVzdDENMAsGA1UECgwEVGVzdDEN" +
                        "MAsGA1UECwwEVGVzdDENMAsGA1UEAwwEVGVzdDETMBEGCSqGSIb3DQEJARYEdGVzdDCC" +
                        "ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANNQx56UkGcNvUrEsdzqhn94nHb3" +
                        "X8oa1+JUWLHE9KUe2ZiNaIMjMOEuMKtss3tKHHBwLig0by24cwySNozoL156i9a5J8VX" +
                        "zkuEr0dKlkGm13BnSBVY+gdRB47oh1ZocSewyyJmWetLiOzgRq4xkYLuV/xP+lmum580" +
                        "MomZcwB06/C42FWIlkPqQF4NFTT1mXjHCzl5uY3OZN9+2KGPa5/QOS9ZI3ixp9TiS8oI" +
                        "Y7VskIk6tUJcnSF3pN6cI+EkS5zODV3Cs33S2Z3mskC3uBTZQxua75NUxycB5wvg4jbf" +
                        "GcKOaA9QhHmaloNDwXcw7v9hTwg/xe148mt+D5wABl8CAwEAAaNTMFEwHQYDVR0OBBYE" +
                        "FCM1tdnw9XYxBNieiNJ8liORKwlpMB8GA1UdIwQYMBaAFCM1tdnw9XYxBNieiNJ8liOR" +
                        "KwlpMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBALmgdhGrkMLsc/g" +
                        "n2BsaFx3S7fHaO3MEV0krghH9TMk+M1y0oghAjotm/bGqOmZ4x/Hv08YputTMLTK2qpa" +
                        "Xtf0Q75V7tOr29jpL10lFALuhNtjRt/Ha5mV4qYGDk+vT8+Rw7SzeVhhSr1pM/MmjN3c" +
                        "AKDZbI0RINIXarZCb2j963eCfguxXZJbxzW09S6kZ/bDEOwi4PLwE0kln9NqQW6JEBHY" +
                        "kDeYQonkKm1VrZklb1obq+g1UIJkTOAXQdJDyvfHWyKzKE8cUHGxYUvlxOL/YCyLkUGa" +
                        "eE/VmJs0niWtKlX4UURG0HAGjZIQ/pJejV+7GzknFMZmuiwJQe4yT4mw=",
                ),
            tsaUrl = "https://www.example.com",
            ocspUrls = mapOf("issuer" to "https://www.example.com"),
            ldapPersonUrl = "https://www.example.com",
            ldapCorpUrl = "https://www.example.com",
            midRestUrl = "https://www.example.com",
            midSkRestUrl = "https://www.example.com",
            sidV2RestUrl = "https://www.example.com",
            sidV2SkRestUrl = "https://www.example.com",
            certBundle =
                listOf(
                    "MIIDuzCCAqOgAwIBAgIUBkYXJdruP6EuH/+I4YoXxIQ3WcowDQYJKoZIhvcNAQELBQAw" +
                        "bTELMAkGA1UEBhMCRUUxDTALBgNVBAgMBFRlc3QxDTALBgNVBAcMBFRlc3QxDTALBgNV" +
                        "BAoMBFRlc3QxDTALBgNVBAsMBFRlc3QxDTALBgNVBAMMBFRlc3QxEzARBgkqhkiG9w0B" +
                        "CQEWBHRlc3QwHhcNMjQwNjEwMTI1OTA3WhcNMjUwNjEwMTI1OTA3WjBtMQswCQYDVQQG" +
                        "EwJFRTENMAsGA1UECAwEVGVzdDENMAsGA1UEBwwEVGVzdDENMAsGA1UECgwEVGVzdDEN" +
                        "MAsGA1UECwwEVGVzdDENMAsGA1UEAwwEVGVzdDETMBEGCSqGSIb3DQEJARYEdGVzdDCC" +
                        "ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANNQx56UkGcNvUrEsdzqhn94nHb3" +
                        "X8oa1+JUWLHE9KUe2ZiNaIMjMOEuMKtss3tKHHBwLig0by24cwySNozoL156i9a5J8VX" +
                        "zkuEr0dKlkGm13BnSBVY+gdRB47oh1ZocSewyyJmWetLiOzgRq4xkYLuV/xP+lmum580" +
                        "MomZcwB06/C42FWIlkPqQF4NFTT1mXjHCzl5uY3OZN9+2KGPa5/QOS9ZI3ixp9TiS8oI" +
                        "Y7VskIk6tUJcnSF3pN6cI+EkS5zODV3Cs33S2Z3mskC3uBTZQxua75NUxycB5wvg4jbf" +
                        "GcKOaA9QhHmaloNDwXcw7v9hTwg/xe148mt+D5wABl8CAwEAAaNTMFEwHQYDVR0OBBYE" +
                        "FCM1tdnw9XYxBNieiNJ8liORKwlpMB8GA1UdIwQYMBaAFCM1tdnw9XYxBNieiNJ8liOR" +
                        "KwlpMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBALmgdhGrkMLsc/g" +
                        "n2BsaFx3S7fHaO3MEV0krghH9TMk+M1y0oghAjotm/bGqOmZ4x/Hv08YputTMLTK2qpa" +
                        "Xtf0Q75V7tOr29jpL10lFALuhNtjRt/Ha5mV4qYGDk+vT8+Rw7SzeVhhSr1pM/MmjN3c" +
                        "AKDZbI0RINIXarZCb2j963eCfguxXZJbxzW09S6kZ/bDEOwi4PLwE0kln9NqQW6JEBHY" +
                        "kDeYQonkKm1VrZklb1obq+g1UIJkTOAXQdJDyvfHWyKzKE8cUHGxYUvlxOL/YCyLkUGa" +
                        "eE/VmJs0niWtKlX4UURG0HAGjZIQ/pJejV+7GzknFMZmuiwJQe4yT4mw=",
                ),
            configurationLastUpdateCheckDate = null,
            configurationUpdateDate = null,
        )
    }
}
