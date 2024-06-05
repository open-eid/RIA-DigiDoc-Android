@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import android.content.Context
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ConfigurationRepositoryTest {
    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var configurationLoader: ConfigurationLoader

    private lateinit var configurationRepository: ConfigurationRepositoryImpl

    private val testDispatcher = UnconfinedTestDispatcher()

    private val configurationFlow = MutableStateFlow<ConfigurationProvider?>(null)

    @Before
    fun setUp() {
        `when`(configurationLoader.getConfigurationFlow()).thenReturn(configurationFlow)
        configurationRepository = ConfigurationRepositoryImpl(context, configurationLoader)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun configurationRepository_getConfiguration_success() {
        val configurationProvider = mockConfigurationProvider()
        configurationFlow.value = configurationProvider

        val result = configurationRepository.getConfiguration()
        assertNotNull(result)
        assertEquals(configurationProvider, result)
    }

    @Test
    fun configurationRepository_getConfiguration_nullWhenNoConfiguration() {
        configurationFlow.value = null

        val result = configurationRepository.getConfiguration()
        assertNull(result)
    }

    @Test
    fun configurationRepository_getCentralConfiguration_success() =
        runBlocking {
            val configurationProvider = mockConfigurationProvider()
            configurationFlow.value = configurationProvider

            val result = configurationRepository.getCentralConfiguration()
            assertNotNull(result)
            assertEquals(configurationProvider, result)
            verify(configurationLoader).loadCentralConfiguration(context)
        }

    @Test
    fun configurationRepository_observeConfigurationUpdates_success() =
        runBlocking {
            val configurationProvider = mockConfigurationProvider()
            var updatedConfig: ConfigurationProvider? = null

            val job =
                launch {
                    configurationRepository.observeConfigurationUpdates {
                        updatedConfig = it
                    }
                }

            configurationFlow.value = configurationProvider

            yield()

            assertEquals(configurationProvider, updatedConfig)
            job.cancel()
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
            configUrl = "https://www.example.com",
            sivaUrl = "https://www.example.com",
            tslUrl = "https://www.example.com",
            tslCerts =
                listOf(
                    "MIIDuzCCAqOgAwIBAgIUBkYXJdruP6EuH/+I4YoXxIQ3WcowDQYJKoZIhvcNAQELBQAw\n" +
                        "bTELMAkGA1UEBhMCRUUxDTALBgNVBAgMBFRlc3QxDTALBgNVBAcMBFRlc3QxDTALBgNV\n" +
                        "BAoMBFRlc3QxDTALBgNVBAsMBFRlc3QxDTALBgNVBAMMBFRlc3QxEzARBgkqhkiG9w0B\n" +
                        "CQEWBHRlc3QwHhcNMjQwNjEwMTI1OTA3WhcNMjUwNjEwMTI1OTA3WjBtMQswCQYDVQQG\n" +
                        "EwJFRTENMAsGA1UECAwEVGVzdDENMAsGA1UEBwwEVGVzdDENMAsGA1UECgwEVGVzdDEN\n" +
                        "MAsGA1UECwwEVGVzdDENMAsGA1UEAwwEVGVzdDETMBEGCSqGSIb3DQEJARYEdGVzdDCC\n" +
                        "ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANNQx56UkGcNvUrEsdzqhn94nHb3\n" +
                        "X8oa1+JUWLHE9KUe2ZiNaIMjMOEuMKtss3tKHHBwLig0by24cwySNozoL156i9a5J8VX\n" +
                        "zkuEr0dKlkGm13BnSBVY+gdRB47oh1ZocSewyyJmWetLiOzgRq4xkYLuV/xP+lmum580\n" +
                        "MomZcwB06/C42FWIlkPqQF4NFTT1mXjHCzl5uY3OZN9+2KGPa5/QOS9ZI3ixp9TiS8oI\n" +
                        "Y7VskIk6tUJcnSF3pN6cI+EkS5zODV3Cs33S2Z3mskC3uBTZQxua75NUxycB5wvg4jbf\n" +
                        "GcKOaA9QhHmaloNDwXcw7v9hTwg/xe148mt+D5wABl8CAwEAAaNTMFEwHQYDVR0OBBYE\n" +
                        "FCM1tdnw9XYxBNieiNJ8liORKwlpMB8GA1UdIwQYMBaAFCM1tdnw9XYxBNieiNJ8liOR\n" +
                        "KwlpMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBALmgdhGrkMLsc/g\n" +
                        "n2BsaFx3S7fHaO3MEV0krghH9TMk+M1y0oghAjotm/bGqOmZ4x/Hv08YputTMLTK2qpa\n" +
                        "Xtf0Q75V7tOr29jpL10lFALuhNtjRt/Ha5mV4qYGDk+vT8+Rw7SzeVhhSr1pM/MmjN3c\n" +
                        "AKDZbI0RINIXarZCb2j963eCfguxXZJbxzW09S6kZ/bDEOwi4PLwE0kln9NqQW6JEBHY\n" +
                        "kDeYQonkKm1VrZklb1obq+g1UIJkTOAXQdJDyvfHWyKzKE8cUHGxYUvlxOL/YCyLkUGa\n" +
                        "eE/VmJs0niWtKlX4UURG0HAGjZIQ/pJejV+7GzknFMZmuiwJQe4yT4mw=\n",
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
                    "MIIDuzCCAqOgAwIBAgIUBkYXJdruP6EuH/+I4YoXxIQ3WcowDQYJKoZIhvcNAQELBQAw\n" +
                        "bTELMAkGA1UEBhMCRUUxDTALBgNVBAgMBFRlc3QxDTALBgNVBAcMBFRlc3QxDTALBgNV\n" +
                        "BAoMBFRlc3QxDTALBgNVBAsMBFRlc3QxDTALBgNVBAMMBFRlc3QxEzARBgkqhkiG9w0B\n" +
                        "CQEWBHRlc3QwHhcNMjQwNjEwMTI1OTA3WhcNMjUwNjEwMTI1OTA3WjBtMQswCQYDVQQG\n" +
                        "EwJFRTENMAsGA1UECAwEVGVzdDENMAsGA1UEBwwEVGVzdDENMAsGA1UECgwEVGVzdDEN\n" +
                        "MAsGA1UECwwEVGVzdDENMAsGA1UEAwwEVGVzdDETMBEGCSqGSIb3DQEJARYEdGVzdDCC\n" +
                        "ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANNQx56UkGcNvUrEsdzqhn94nHb3\n" +
                        "X8oa1+JUWLHE9KUe2ZiNaIMjMOEuMKtss3tKHHBwLig0by24cwySNozoL156i9a5J8VX\n" +
                        "zkuEr0dKlkGm13BnSBVY+gdRB47oh1ZocSewyyJmWetLiOzgRq4xkYLuV/xP+lmum580\n" +
                        "MomZcwB06/C42FWIlkPqQF4NFTT1mXjHCzl5uY3OZN9+2KGPa5/QOS9ZI3ixp9TiS8oI\n" +
                        "Y7VskIk6tUJcnSF3pN6cI+EkS5zODV3Cs33S2Z3mskC3uBTZQxua75NUxycB5wvg4jbf\n" +
                        "GcKOaA9QhHmaloNDwXcw7v9hTwg/xe148mt+D5wABl8CAwEAAaNTMFEwHQYDVR0OBBYE\n" +
                        "FCM1tdnw9XYxBNieiNJ8liORKwlpMB8GA1UdIwQYMBaAFCM1tdnw9XYxBNieiNJ8liOR\n" +
                        "KwlpMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBALmgdhGrkMLsc/g\n" +
                        "n2BsaFx3S7fHaO3MEV0krghH9TMk+M1y0oghAjotm/bGqOmZ4x/Hv08YputTMLTK2qpa\n" +
                        "Xtf0Q75V7tOr29jpL10lFALuhNtjRt/Ha5mV4qYGDk+vT8+Rw7SzeVhhSr1pM/MmjN3c\n" +
                        "AKDZbI0RINIXarZCb2j963eCfguxXZJbxzW09S6kZ/bDEOwi4PLwE0kln9NqQW6JEBHY\n" +
                        "kDeYQonkKm1VrZklb1obq+g1UIJkTOAXQdJDyvfHWyKzKE8cUHGxYUvlxOL/YCyLkUGa\n" +
                        "eE/VmJs0niWtKlX4UURG0HAGjZIQ/pJejV+7GzknFMZmuiwJQe4yT4mw=\n",
                ),
            configurationLastUpdateCheckDate = null,
            configurationUpdateDate = null,
        )
    }
}
