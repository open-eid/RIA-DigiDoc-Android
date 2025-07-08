@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Base64
import androidx.annotation.RawRes
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.Constant.CDOC1_EXTENSION
import ee.ria.DigiDoc.common.Constant.CDOC2_EXTENSION
import ee.ria.DigiDoc.common.Constant.CONTAINER_MIME_TYPE
import ee.ria.DigiDoc.common.Constant.DIR_CRYPTO_CERT
import ee.ria.DigiDoc.common.certificate.CertificateServiceImpl
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.cryptolib.CryptoContainer.Companion.decrypt
import ee.ria.DigiDoc.cryptolib.CryptoContainer.Companion.encrypt
import ee.ria.DigiDoc.cryptolib.CryptoContainer.Companion.openOrCreate
import ee.ria.DigiDoc.cryptolib.exception.ContainerDataFilesEmptyException
import ee.ria.DigiDoc.cryptolib.exception.CryptoException
import ee.ria.DigiDoc.cryptolib.exception.DataFilesEmptyException
import ee.ria.DigiDoc.cryptolib.exception.RecipientsEmptyException
import ee.ria.DigiDoc.cryptolib.init.CryptoInitialization
import ee.ria.DigiDoc.cryptolib.repository.RecipientRepository
import ee.ria.DigiDoc.cryptolib.repository.RecipientRepositoryImpl
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.cdoc.CDocException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files

@RunWith(MockitoJUnitRunner::class)
class CryptoContainerTest {
    companion object {
        private lateinit var configurationLoader: ConfigurationLoader
        private lateinit var configurationRepository: ConfigurationRepository

        private val cert =
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
                "eE/VmJs0niWtKlX4UURG0HAGjZIQ/pJejV+7GzknFMZmuiwJQe4yT4mw="

        private val configurationProvider =
            ConfigurationProvider(
                metaInf =
                    ConfigurationProvider.MetaInf(
                        url = "https://www.example.com",
                        date = "2021-09-01",
                        serial = 1,
                        version = 1,
                    ),
                sivaUrl = "https://www.example.com",
                tslUrl = "https://www.example.com",
                tslCerts = listOf("CER00001", "CER00002"),
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
                        cert,
                    ),
                configurationLastUpdateCheckDate = null,
                configurationUpdateDate = null,
                cdoc2Conf =
                    mapOf(
                        "00000000-0000-0000-0000-000000000000" to
                            ConfigurationProvider.CDOC2Conf(
                                name = "RIA",
                                post = "https://cdoc2.id.ee:8443",
                                fetch = "https://cdoc2.id.ee:8444",
                            ),
                    ),
                cdoc2UseKeyServer = false,
                cdoc2DefaultKeyServer = "00000000-0000-0000-0000-000000000000",
            )

        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val context = InstrumentationRegistry.getInstrumentation().targetContext
                    configurationLoader =
                        ConfigurationLoaderImpl(
                            Gson(),
                            CentralConfigurationRepositoryImpl(
                                CentralConfigurationServiceImpl("Tests", ConfigurationProperty()),
                            ),
                            ConfigurationProperty(),
                            ConfigurationPropertiesImpl(),
                            ConfigurationSignatureVerifierImpl(),
                        )
                    configurationRepository = ConfigurationRepositoryImpl(context, configurationLoader)
                    CryptoInitialization().init(
                        isLoggingEnabled = true,
                    )
                } catch (_: Exception) {
                }
            }
        }
    }

    @Mock
    private var mockConfigurationRepository: ConfigurationRepository = mock()

    private lateinit var context: Context

    private lateinit var recipientRepository: RecipientRepository

    private lateinit var testFile: File
    private lateinit var dataFile1: File
    private lateinit var dataFile2: File
    private lateinit var dataFile3: File

    private lateinit var containerCDOC1: File
    private lateinit var containerCDOC2: File
    private lateinit var containerCDOC2Network: File

    private lateinit var containerRIACDOC1: File
    private lateinit var containerRIACDOC2: File

    private lateinit var preferences: SharedPreferences
    private lateinit var resources: Resources

    private val authCert =
        "MIID4zCCA0SgAwIBAgIQENO9RJxIOHNfcdqN/UiQFzAKBggqhkjOPQQDBDBYMQswCQYDV" +
            "QQGEwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRhDA5OVFJFRS0xMDc0NzAxMzETM" +
            "BEGA1UEAwwKRVNURUlEMjAxODAeFw0yMDA5MjgxMjQzNTdaFw0yNTA5MjcyMTU5NTlaMHMxCzAJBgNVBAYTA" +
            "kVFMSQwIgYDVQQDDBtNRUxJS0pBTixCT1JJU1MsMzgyMDcyNTM3MTgxETAPBgNVBAQMCE1FTElLSkFOMQ8wD" +
            "QYDVQQqDAZCT1JJU1MxGjAYBgNVBAUTEVBOT0VFLTM4MjA3MjUzNzE4MHYwEAYHKoZIzj0CAQYFK4EEACIDY" +
            "gAEKglUc4tuRrcPFSGqQA//Ke7KqfYYcXfYZgQfZG21n/o+NwM1n+QqR0rsPlMLnQIxr2n6K10mimm874WUx" +
            "TgFts9VP51BVsSHJ4cI5aXcVtzoGBvRVLxHY7bOosCKw5Uvo4IBtjCCAbIwCQYDVR0TBAIwADAOBgNVHQ8BA" +
            "f8EBAMCA4gwRwYDVR0gBEAwPjAyBgsrBgEEAYORIQEBATAjMCEGCCsGAQUFBwIBFhVodHRwczovL3d3dy5za" +
            "y5lZS9DUFMwCAYGBACPegECMB8GA1UdEQQYMBaBFDM4MjA3MjUzNzE4QGVlc3RpLmVlMB0GA1UdDgQWBBTF5" +
            "xt4aCCUhRcUE/97xbVj1pynqDBhBggrBgEFBQcBAwRVMFMwUQYGBACORgEFMEcwRRY/aHR0cHM6Ly9zay5lZ" +
            "S9lbi9yZXBvc2l0b3J5L2NvbmRpdGlvbnMtZm9yLXVzZS1vZi1jZXJ0aWZpY2F0ZXMvEwJFTjAgBgNVHSUBA" +
            "f8EFjAUBggrBgEFBQcDAgYIKwYBBQUHAwQwHwYDVR0jBBgwFoAU2axw219+vpT4oOS+R6LQNK2aKhIwZgYIK" +
            "wYBBQUHAQEEWjBYMCcGCCsGAQUFBzABhhtodHRwOi8vYWlhLnNrLmVlL2VzdGVpZDIwMTgwLQYIKwYBBQUHM" +
            "AKGIWh0dHA6Ly9jLnNrLmVlL2VzdGVpZDIwMTguZGVyLmNydDAKBggqhkjOPQQDBAOBjAAwgYgCQgH6rAceM" +
            "rC+G2sC54rkiUlL1eqvtx9KDk/zMKba79ZleKBheJjIFLzTQODfRWrb+i49Kg63vHVHiI/HKD6GXE9vrAJCA" +
            "VTDhBzHHIpQJh1uTD6ZgMwSYJj0jw3g+8ig43IdAw4HZu6Eyv8AeVzEHRpgcNDjol+tMMm8DVkR4TId6LaHc" +
            "kX/"

    private val riaCertData =
        "MIIFvzCCA6egAwIBAgIQKnebbBPzuXNjX63ZX6/GVTANBgkqhkiG9w0BAQsF" +
            "ADCBhjELMAkGA1UEBhMCRUUxIjAgBgNVBAoMGUFTIFNlcnRpZml0c2VlcmltaXNrZXNrdXMxITAfBgNVBAsM" +
            "GFNlcnRpZml0c2VlcmltaXN0ZWVudXNlZDEXMBUGA1UEYQwOTlRSRUUtMTA3NDcwMTMxFzAVBgNVBAMMDktM" +
            "QVNTMy1TSyAyMDE2MB4XDTIyMTAzMTExMTMwOVoXDTI1MTEyOTExMTMwOVowfDERMA8GA1UEBRMINzAwMDYz" +
            "MTcxETAPBgNVBAgMCEhhcmp1bWFhMRAwDgYDVQQHDAdUYWxsaW5uMQswCQYDVQQGEwJFRTEhMB8GA1UECgwY" +
            "UmlpZ2kgSW5mb3PDvHN0ZWVtaSBBbWV0MRIwEAYDVQQDDAlSSUE6Q1lCRVIwggEiMA0GCSqGSIb3DQEBAQUA" +
            "A4IBDwAwggEKAoIBAQDfXHESB8OIGEk3gZGVSNzgKYdtwDOL7nEdtdmujgm2SPGSQpdJUEMT0+tRelzY0QBz" +
            "AqJxayfe7OUqsvKtqNUhfw6bQXeEFhDHGFQnOciPq88RNnqo0MmbpWFqM4ukyDrxrT+44D8ZCi0qWQeG50Am" +
            "/GhXu/gfZVsqYldGxOcI6oTwzLTAjsaguSlnqwQc8zBegvHBCvSfiF587Ki7BFN4v11aSykX78flmcec4GjK" +
            "UesfZCqn8Fxjjdz8Ljzl1tvK9WFkV6rYDaVVV8mwpj2fyFcOmC2CpOQ7lbMO4Vq5jm05tRAJ6e7wXB5wXaB0" +
            "MI/0m5Ly2OZ2wHjf9IQIx0aJAgMBAAGjggEwMIIBLDAJBgNVHRMEAjAAMFIGA1UdIARLMEkwMAYJKwYBBAHO" +
            "HwcDMCMwIQYIKwYBBQUHAgEWFWh0dHBzOi8vd3d3LnNrLmVlL2NwczAIBgYEAI96AQEwCwYJKwYBBAHOHwkE" +
            "MB8GA1UdIwQYMBaAFK5eWPXy8tnBjtnvTgfbdcpQ4ocAMA4GA1UdDwEB/wQEAwIEsDAdBgNVHQ4EFgQUKsOs" +
            "u8K5k8Pr1HljG5HAGG01oigwewYIKwYBBQUHAQEEbzBtMCgGCCsGAQUFBzABhhxodHRwOi8vYWlhLnNrLmVl" +
            "L2tsYXNzMy0yMDE2MEEGCCsGAQUFBzAChjVodHRwczovL2Muc2suZWUvS0xBU1MzLVNLXzIwMTZfRUVDQ1JD" +
            "QV9TSEEzODQuZGVyLmNydDANBgkqhkiG9w0BAQsFAAOCAgEAfzSy0Ya8FKL4YIBhpKE9gBgt2wtjOOBCFbue" +
            "0aa/IiN8fvewJe359K76suzPFWypqkQn7y0jkuzFFshyMExxI6hvCYud+Ji3oiguXKERTGN/69T0S/e40NQ3" +
            "V0hL4DdFAo5iVjc7IO/bC+r6WMAk6EEI9/hBW8LsyXvpCKdu3311d1ewTX29squN4yP3mak8NuiTjIXaYnd7" +
            "Cto2QbFhddwG48D5dHCuycc5N8hhFTTSqiOYuEqyyiMQFV5TxbawGnPYyn7c9uK3CX0IxiweNE4n0xsDfNKE" +
            "ktmPwbrV5mItNIupVE73ZpdhdGl6qVPYFGxfwRZDUDHpZaRt9DrzpkYGRUDAgrvnkhSasAu88HeTHz/J/+lO" +
            "lf0ZPnRBxyiaERws/bakQVUZcWucMJ8hOt/SSHrJvMwSsP+ZUsiwAsmxS67BOqRpXncn9rADDvDqrUMZLQIj" +
            "BMjHT0uUx+GNIIxTTKnuI3epwb7CMiII42s3OFr0ltv0ARHQ01MuSiYAxPmXBUBiOrEAgnkc/SQdUtuStEVh" +
            "toz18FucYfeaVUrUUlAAJZKwi+ITfAd9YYnVq3nq5nCBZN1qQUyWccBKNnopOPnGuzQhd5l9+EdAnrVQSCTE" +
            "67D4FYOcPvhAq2zd3DniK6UJtNz73lJtoJ3crBkRu7gfubDeaTvEm9aDCm0="

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().clear().commit()
        resources = context.resources
        recipientRepository =
            RecipientRepositoryImpl(
                configurationRepository,
                CertificateServiceImpl(),
            )
        testFile = createTempFileWithStringContent("testFile", "test content")
        dataFile1 = File.createTempFile("dataFile1", ".txt", context.filesDir)
        dataFile2 = File.createTempFile("dataFile2", ".txt", context.filesDir)
        dataFile3 = File.createTempFile("dataFile3", ".txt", context.filesDir)

        containerCDOC1 =
            getResourceFileAsFile(
                context, "example_cdoc1.cdoc", ee.ria.DigiDoc.common.R.raw.example_cdoc1,
            )
        containerCDOC2 =
            getResourceFileAsFile(
                context, "example_cdoc2.cdoc2", ee.ria.DigiDoc.common.R.raw.example_cdoc2,
            )
        containerCDOC2Network =
            getResourceFileAsFile(
                context, "example_network.cdoc2", ee.ria.DigiDoc.common.R.raw.example_network,
            )
        containerRIACDOC1 =
            getResourceFileAsFile(
                context, "example_ria_cdoc1.cdoc", ee.ria.DigiDoc.common.R.raw.example_ria_cdoc1,
            )
        containerRIACDOC2 =
            getResourceFileAsFile(
                context, "example_ria_cdoc2.cdoc2", ee.ria.DigiDoc.common.R.raw.example_ria_cdoc2,
            )
    }

    @After
    fun tearDown() {
        testFile.delete()
        dataFile1.delete()
        dataFile2.delete()
        dataFile3.delete()
    }

    @Test
    fun cryptoContainer_openOrCreate_createSuccess() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    false,
                )
                .apply()

            val dataFiles = listOf(testFile)

            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, testFile, dataFiles, cdoc2Settings)

            val result = cryptoContainer.file

            assertNotNull(result)
            assertEquals(CDOC1_EXTENSION, result?.extension)
        }

    @Test
    fun cryptoContainer_openOrCreate_multipleFilesCreateSuccess() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    false,
                )
                .apply()

            val dataFiles = listOf(dataFile1, dataFile2, dataFile3)

            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, dataFile1, dataFiles, cdoc2Settings)

            val result = cryptoContainer.file

            assertNotNull(result)
            assertEquals(CDOC1_EXTENSION, result?.extension)
        }

    @Test
    fun cryptoContainer_openOrCreate_forceCreateSuccess() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    false,
                )
                .apply()

            val dataFiles = listOf(containerCDOC1)

            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerCDOC1, dataFiles, cdoc2Settings, true)

            val result = cryptoContainer.file

            assertNotNull(result)
            assertEquals(containerCDOC1.name, result?.name)
            assertEquals(containerCDOC1.name, cryptoContainer.getDataFiles().first().name)
            assertEquals(CDOC1_EXTENSION, result?.extension)
        }

    @Test
    fun cryptoContainer_openOrCreate_useCDOC2EncryptionTrueCreateSuccess() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val dataFiles = listOf(testFile)

            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, testFile, dataFiles, cdoc2Settings)

            val result = cryptoContainer.file

            assertNotNull(result)
            assertEquals(CDOC2_EXTENSION, result?.extension)
        }

    @Test
    fun cryptoContainer_openOrCreate_openCDOC1Success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    false,
                )
                .apply()

            val dataFiles = listOf(containerCDOC1)

            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerCDOC1, dataFiles, cdoc2Settings)

            val result = cryptoContainer.file

            assertNotNull(result)
            assertEquals(containerCDOC1.name, result?.name)
        }

    @Test
    fun cryptoContainer_openOrCreate_openCDOC2Success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val dataFiles = listOf(containerCDOC2)

            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)

            val result = cryptoContainer.file

            assertNotNull(result)
            assertEquals(containerCDOC2.name, result?.name)
        }

    @Test
    fun cryptoContainer_openOrCreate_openCDOC1WithEnterpriseAddresseeSuccess() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    false,
                )
                .apply()

            val dataFiles = listOf(containerRIACDOC1)

            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerRIACDOC1, dataFiles, cdoc2Settings)

            val result = cryptoContainer.file

            assertNotNull(result)
            assertEquals(containerRIACDOC1.name, result?.name)
        }

    @Test
    fun cryptoContainer_openOrCreate_openCDOC2WithEnterpriseAddresseeSuccess() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val dataFiles = listOf(containerRIACDOC2)

            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerRIACDOC2, dataFiles, cdoc2Settings)

            val result = cryptoContainer.file

            assertNotNull(result)
            assertEquals(containerRIACDOC2.name, result?.name)
        }

    @Test(expected = CryptoException::class)
    fun cryptoContainer_openOrCreate_openCDOC2Exception() =
        runTest {
            val containerCDOC2 =
                getResourceFileAsFile(context, "example_cdoc2.cdoc", ee.ria.DigiDoc.common.R.raw.example_cdoc2)

            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    false,
                )
                .apply()

            val dataFiles = listOf(containerCDOC2)

            val cdoc2Settings = CDOC2Settings(context)
            openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)
        }

    @Test(expected = DataFilesEmptyException::class)
    fun cryptoContainer_openOrCreate_dataFilesNullExpectException() =
        runTest {
            val cdoc2Settings = CDOC2Settings(context)
            openOrCreate(context, testFile, null, cdoc2Settings)
        }

    @Test(expected = DataFilesEmptyException::class)
    fun cryptoContainer_openOrCreate_dataFilesEmptyExpectException() =
        runTest {
            val cdoc2Settings = CDOC2Settings(context)
            openOrCreate(context, testFile, listOf(), cdoc2Settings)
        }

    @Test(expected = CryptoException::class)
    fun cryptoContainer_decrypt_authCertIsNullThrowsException() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val dataFiles = listOf(containerCDOC2)
            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)

            val token = mock(Token::class.java)

            decrypt(
                context,
                containerCDOC2,
                cryptoContainer.getRecipients(),
                null,
                "1234".toByteArray(),
                token,
                cdoc2Settings,
                configurationRepository,
            )
        }

    @Test(expected = CryptoException::class)
    fun cryptoContainer_decrypt_authCertIsEmptyThrowsException() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val dataFiles = listOf(containerCDOC2)
            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)

            val token = mock(Token::class.java)

            decrypt(
                context,
                containerCDOC2,
                cryptoContainer.getRecipients(),
                byteArrayOf(),
                "1234".toByteArray(),
                token,
                cdoc2Settings,
                configurationRepository,
            )
        }

    @Test(expected = CDocException::class)
    fun cryptoContainer_decrypt_exception() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val dataFiles = listOf(containerCDOC2)
            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)

            val token = mock(Token::class.java)

            decrypt(
                context,
                containerCDOC2,
                cryptoContainer.getRecipients(),
                "012345678".toByteArray(),
                "1234".toByteArray(),
                token,
                cdoc2Settings,
                configurationRepository,
            )
        }

    @Test()
    fun cryptoContainer_decrypt_offlineSuccess() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val dataFiles = listOf(containerCDOC2)
            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)
            val data = "+puuS4wUR3wz+5D3DknZmebWhrP0X7ngQlXiBuPBipow0mDXgixJ7OCwhxx/zkYg"
            val token = mock(Token::class.java)
            `when`(token.decrypt(any(), any(), eq(true))).thenReturn(Base64.decode(data, Base64.DEFAULT))

            val result =
                decrypt(
                    context,
                    containerCDOC2,
                    cryptoContainer.getRecipients(),
                    Base64.decode(authCert, Base64.DEFAULT),
                    "1234".toByteArray(),
                    token,
                    cdoc2Settings,
                    configurationRepository,
                )

            assertTrue(result.decrypted)
            assertEquals("ko.png", result.getDataFiles().first().name)
        }

    @Test(expected = CryptoException::class)
    fun cryptoContainer_decrypt_onlineFMKException() =
        runTest {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_setting_key),
                    ProxySetting.MANUAL_PROXY.name,
                )
            }

            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_online_encryption),
                    true,
                )
                .apply()

            preferences
                .edit()
                .putString(
                    resources.getString(R.string.crypto_settings_use_cdoc2_fetch_url),
                    "https://cdoc2.id.ee:8444",
                )
                .apply()

            preferences
                .edit()
                .putString(
                    resources.getString(R.string.crypto_settings_use_cdoc2_fetch_url),
                    "https://cdoc2.id.ee:8443",
                )
                .apply()

            val dataFiles = listOf(containerCDOC2Network)
            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerCDOC2Network, dataFiles, cdoc2Settings)
            val data = "zumNg86dm4q97avNnIuTGSGvMyKTSPRi0QBG2HSri3HwTm9jVEJKWiGsCUZ1Nudz"
            val authData =
                "nrNDvHxP8jjqK1Uk83BxYB63UHQxAPAPBLlLlrQUddlY887Z0lS+E1FvXhv8XIpr0wNUP" +
                    "tIiAgMQ26zLsBXQ+8w9Y9WootXHhhHYq8OqFDG3zgJwtmwCt6WzbGpsxZSg"
            val token = mock(Token::class.java)
            `when`(token.decrypt(any(), any(), eq(true))).thenReturn(Base64.decode(data, Base64.DEFAULT))
            `when`(token.authenticate(any(), any())).thenReturn(Base64.decode(authData, Base64.DEFAULT))
            `when`(mockConfigurationRepository.getConfiguration()).thenReturn(configurationProvider)
            val result =
                decrypt(
                    context,
                    containerCDOC2Network,
                    cryptoContainer.getRecipients(),
                    Base64.decode(authCert, Base64.DEFAULT),
                    "1234".toByteArray(),
                    token,
                    cdoc2Settings,
                    mockConfigurationRepository,
                )

            assertTrue(result.decrypted)
            assertEquals("ko.png", result.getDataFiles().first().name)
        }

    @Test(expected = CryptoException::class)
    fun cryptoContainer_decrypt_onlineWithNetworkSettingsException() =
        runTest {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_setting_key),
                    ProxySetting.NO_PROXY.name,
                )
            }

            val cryptoCertName = "cert.pem"
            val cryptoCertFolder = File(context.filesDir, DIR_CRYPTO_CERT)
            if (!cryptoCertFolder.exists()) {
                cryptoCertFolder.mkdirs()
            }

            val cryptoCertFile = File(cryptoCertFolder, cryptoCertName)
            val certInputStream = ByteArrayInputStream(cert.toByteArray(Charsets.UTF_8))
            FileUtils.copyInputStreamToFile(certInputStream, cryptoCertFile)

            preferences
                .edit()
                .putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_crypto_cert_key),
                    cryptoCertName,
                )
                .apply()

            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_online_encryption),
                    true,
                )
                .apply()

            preferences
                .edit()
                .putString(
                    resources.getString(R.string.crypto_settings_use_cdoc2_fetch_url),
                    "https://cdoc2.id.ee:8444",
                )
                .apply()

            preferences
                .edit()
                .putString(
                    resources.getString(R.string.crypto_settings_use_cdoc2_fetch_url),
                    "https://cdoc2.id.ee:8443",
                )
                .apply()

            val dataFiles = listOf(containerCDOC2Network)
            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerCDOC2Network, dataFiles, cdoc2Settings)
            val data = "zumNg86dm4q97avNnIuTGSGvMyKTSPRi0QBG2HSri3HwTm9jVEJKWiGsCUZ1Nudz"
            val authData =
                "nrNDvHxP8jjqK1Uk83BxYB63UHQxAPAPBLlLlrQUddlY887Z0lS+E1FvXhv8XIpr0wNUP" +
                    "tIiAgMQ26zLsBXQ+8w9Y9WootXHhhHYq8OqFDG3zgJwtmwCt6WzbGpsxZSg"
            val token = mock(Token::class.java)
            `when`(token.decrypt(any(), any(), eq(true))).thenReturn(Base64.decode(data, Base64.DEFAULT))
            `when`(token.authenticate(any(), any())).thenReturn(Base64.decode(authData, Base64.DEFAULT))
            `when`(mockConfigurationRepository.getConfiguration()).thenReturn(configurationProvider)
            val result =
                decrypt(
                    context,
                    containerCDOC2Network,
                    cryptoContainer.getRecipients(),
                    Base64.decode(authCert, Base64.DEFAULT),
                    "1234".toByteArray(),
                    token,
                    cdoc2Settings,
                    mockConfigurationRepository,
                )

            assertTrue(result.decrypted)
            assertEquals("ko.png", result.getDataFiles().first().name)
        }

    @Test(expected = NullPointerException::class)
    fun cryptoContainer_decrypt_deriveECDH1Exception() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val dataFiles = listOf(containerCDOC2)
            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)
            val token = mock(Token::class.java)

            decrypt(
                context,
                containerCDOC2,
                cryptoContainer.getRecipients(),
                Base64.decode(authCert, Base64.DEFAULT),
                "1234".toByteArray(),
                token,
                cdoc2Settings,
                configurationRepository,
            )
        }

    @Test(expected = CDocException::class)
    fun cryptoContainer_decrypt_CDOC1RSAException() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    false,
                )
                .apply()

            val dataFiles = listOf(containerRIACDOC1)
            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerRIACDOC1, dataFiles, cdoc2Settings)
            val data = "+puuS4wUR3wz+5D3DknZmebWhrP0X7ngQlXiBuPBipow0mDXgixJ7OCwhxx/zkYg"
            val token = mock(Token::class.java)
            `when`(token.decrypt(any(), any(), eq(true))).thenReturn(Base64.decode(data, Base64.DEFAULT))

            decrypt(
                context,
                containerRIACDOC1,
                cryptoContainer.getRecipients(),
                Base64.decode(riaCertData, Base64.DEFAULT),
                "1234".toByteArray(),
                token,
                cdoc2Settings,
                configurationRepository,
            )
        }

    @Test(expected = NullPointerException::class)
    fun cryptoContainer_decrypt_decryptRSAException() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    false,
                )
                .apply()

            val dataFiles = listOf(containerRIACDOC1)
            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, containerRIACDOC1, dataFiles, cdoc2Settings)
            val token = mock(Token::class.java)
            decrypt(
                context,
                containerRIACDOC1,
                cryptoContainer.getRecipients(),
                Base64.decode(riaCertData, Base64.DEFAULT),
                "1234".toByteArray(),
                token,
                cdoc2Settings,
                configurationRepository,
            )
        }

    @Test
    fun cryptoContainer_encrypt_CDOC2OfflineSuccess() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_online_encryption),
                    false,
                )
                .apply()

            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val recipient = Addressee(Base64.decode(authCert, Base64.DEFAULT))

            val testFiles = listOf(testFile)
            val container = openOrCreate(context, testFile, testFiles, cdoc2Settings)

            val result =
                encrypt(context, container.file, testFiles, listOf(recipient), cdoc2Settings, configurationRepository)

            assertTrue(result.encrypted)
            assertEquals(container.file?.name, result.file?.name)
            assertEquals(1, result.getRecipients().size)
        }

    @Test(expected = CryptoException::class)
    fun cryptoContainer_encrypt_CDOC2OnlineException() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_online_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val recipient = Addressee(Base64.decode(authCert, Base64.DEFAULT))

            val testFiles = listOf(testFile)
            val container = openOrCreate(context, testFile, testFiles, cdoc2Settings)

            encrypt(context, container.file, testFiles, listOf(recipient), cdoc2Settings, configurationRepository)
        }

    @Test(expected = CryptoException::class)
    fun cryptoContainer_encrypt_CDOC2OnlineSuccess() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_online_encryption),
                    true,
                )
                .apply()

            preferences
                .edit()
                .putString(
                    resources.getString(R.string.crypto_settings_use_cdoc2_fetch_url),
                    "https://cdoc2.id.ee:8444",
                )
                .apply()

            preferences
                .edit()
                .putString(
                    resources.getString(R.string.crypto_settings_use_cdoc2_fetch_url),
                    "https://cdoc2.id.ee:8443",
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val recipient = Addressee(Base64.decode(authCert, Base64.DEFAULT))

            val testFiles = listOf(testFile)
            val container = openOrCreate(context, testFile, testFiles, cdoc2Settings)

            val result =
                encrypt(context, container.file, testFiles, listOf(recipient), cdoc2Settings, configurationRepository)

            assertTrue(result.encrypted)
            assertEquals(container.file?.name, result.file?.name)
            assertEquals(1, result.getRecipients().size)
        }

    @Test
    fun cryptoContainer_encrypt_CDOC1Success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    false,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val recipient = Addressee(Base64.decode(authCert, Base64.DEFAULT))

            val testFiles = listOf(testFile)
            val container = openOrCreate(context, testFile, testFiles, cdoc2Settings)

            val result =
                encrypt(context, container.file, testFiles, listOf(recipient), cdoc2Settings, configurationRepository)

            assertTrue(result.encrypted)
            assertEquals(container.file?.name, result.file?.name)
            assertEquals(1, result.getRecipients().size)
            assertEquals(1, result.getDataFiles().size)
        }

    @Test(expected = DataFilesEmptyException::class)
    fun cryptoContainer_encrypt_dataFilesNullException() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val recipient = Addressee(Base64.decode(authCert, Base64.DEFAULT))

            val testFiles = listOf(testFile)
            val container = openOrCreate(context, testFile, testFiles, cdoc2Settings)
            encrypt(context, container.file, null, listOf(recipient), cdoc2Settings, configurationRepository)
        }

    @Test(expected = DataFilesEmptyException::class)
    fun cryptoContainer_encrypt_dataFilesEmptyException() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val recipient = Addressee(Base64.decode(authCert, Base64.DEFAULT))

            val testFiles = listOf(testFile)
            val container = openOrCreate(context, testFile, testFiles, cdoc2Settings)
            encrypt(context, container.file, listOf(), listOf(recipient), cdoc2Settings, configurationRepository)
        }

    @Test(expected = RecipientsEmptyException::class)
    fun cryptoContainer_encrypt_recipientsNullException() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)

            val testFiles = listOf(testFile)
            val container = openOrCreate(context, testFile, testFiles, cdoc2Settings)
            encrypt(context, container.file, testFiles, null, cdoc2Settings, configurationRepository)
        }

    @Test(expected = RecipientsEmptyException::class)
    fun cryptoContainer_encrypt_recipientsEmptyException() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)

            val testFiles = listOf(testFile)
            val container = openOrCreate(context, testFile, testFiles, cdoc2Settings)
            encrypt(context, container.file, testFiles, listOf(), cdoc2Settings, configurationRepository)
        }

    @Test(expected = CryptoException::class)
    fun cryptoContainer_encrypt_CDOC2OfflineException() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)

            val testFiles = listOf(testFile)
            val container = openOrCreate(context, testFile, testFiles, cdoc2Settings)

            val dataFiles = listOf(containerCDOC2)
            val cryptoContainer = openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)

            val result =
                encrypt(
                    context,
                    container.file,
                    testFiles,
                    cryptoContainer.getRecipients(),
                    cdoc2Settings,
                    configurationRepository,
                )

            assertTrue(result.encrypted)
            assertEquals(container.file?.name, result.file?.name)
            assertEquals(1, result.getRecipients().size)
        }

    @Test()
    fun cryptoContainer_containerMimetype_success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)

            val dataFiles = listOf(containerCDOC2)
            val cryptoContainer = openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)
            assertEquals(CONTAINER_MIME_TYPE, cryptoContainer.containerMimetype())
        }

    @Test()
    fun cryptoContainer_getName_success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)

            val dataFiles = listOf(containerCDOC2)
            val cryptoContainer = openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)
            assertEquals("example_cdoc2.cdoc2", cryptoContainer.getName())
        }

    @Test()
    fun cryptoContainer_setName_success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)

            val dataFiles = listOf(containerCDOC2)
            val cryptoContainer = openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)
            cryptoContainer.setName("test_cdoc2.cdoc2")
            assertEquals("test_cdoc2.cdoc2", cryptoContainer.getName())
        }

    @Test()
    fun cryptoContainer_hasRecipients_success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)

            val dataFiles = listOf(containerCDOC2)
            val cryptoContainer = openOrCreate(context, containerCDOC2, dataFiles, cdoc2Settings)
            assertTrue(cryptoContainer.hasRecipients())
        }

    @Test()
    fun cryptoContainer_getDataFile_success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    false,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)

            val dataFiles = listOf(testFile)
            val cryptoContainer = openOrCreate(context, testFile, dataFiles, cdoc2Settings)
            val tempResourcesDir = Files.createTempDirectory("test_resources").toFile()
            val dataFile =
                cryptoContainer.getDataFile(
                    cryptoContainer.getDataFiles().first(),
                    tempResourcesDir,
                )
            assertEquals("txt", dataFile?.extension)
            assertTrue(dataFile?.exists() == true)
        }

    @Test(expected = IllegalArgumentException::class)
    fun cryptoContainer_getDataFile_exception() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    false,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)

            val dataFiles = listOf(testFile)
            val cryptoContainer = openOrCreate(context, testFile, dataFiles, cdoc2Settings)
            val tempResourcesDir = Files.createTempDirectory("test_resources").toFile()
            cryptoContainer.getDataFile(
                dataFile1,
                tempResourcesDir,
            )
        }

    @Test()
    fun cryptoContainer_addDataFiles_success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val dataFiles = listOf(testFile)
            val cryptoContainer = openOrCreate(context, testFile, dataFiles, cdoc2Settings)
            cryptoContainer.addDataFiles(listOf(dataFile1, dataFile2, dataFile3))
            assertEquals(4, cryptoContainer.getDataFiles().size)
        }

    @Test()
    fun cryptoContainer_addRecipients_success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val dataFiles = listOf(testFile)
            val cryptoContainer = openOrCreate(context, testFile, dataFiles, cdoc2Settings)
            cryptoContainer.addRecipients(listOf(Addressee(Base64.decode(authCert, Base64.DEFAULT))))
            assertEquals(1, cryptoContainer.getRecipients().size)
        }

    @Test()
    fun cryptoContainer_removeDataFile_success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val dataFiles = listOf(testFile)
            val cryptoContainer = openOrCreate(context, testFile, dataFiles, cdoc2Settings)
            cryptoContainer.addDataFiles(listOf(dataFile1, dataFile2, dataFile3))
            cryptoContainer.removeDataFile(dataFile1)
            assertEquals(3, cryptoContainer.getDataFiles().size)
        }

    @Test(expected = ContainerDataFilesEmptyException::class)
    fun cryptoContainer_removeDataFile_exception() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val dataFiles = listOf(testFile)
            val cryptoContainer = openOrCreate(context, testFile, dataFiles, cdoc2Settings)
            cryptoContainer.removeDataFile(testFile)
        }

    @Test()
    fun cryptoContainer_removeRecipient_success() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val dataFiles = listOf(testFile)
            val cryptoContainer = openOrCreate(context, testFile, dataFiles, cdoc2Settings)
            val recipient = Addressee(Base64.decode(authCert, Base64.DEFAULT))
            cryptoContainer.addRecipients(listOf(recipient))
            cryptoContainer.removeRecipient(recipient)
            assertEquals(0, cryptoContainer.getRecipients().size)
        }

    @Test()
    fun cryptoContainer_removeRecipient_removeFromEmptyRecipients() =
        runTest {
            preferences
                .edit()
                .putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    true,
                )
                .apply()

            val cdoc2Settings = CDOC2Settings(context)
            val dataFiles = listOf(testFile)
            val cryptoContainer = openOrCreate(context, testFile, dataFiles, cdoc2Settings)
            val recipient = Addressee(Base64.decode(authCert, Base64.DEFAULT))
            cryptoContainer.removeRecipient(recipient)
            assertEquals(0, cryptoContainer.getRecipients().size)
        }

    fun getResourceFileAsFile(
        context: Context,
        fileName: String,
        @RawRes resourceId: Int,
    ): File {
        val tempResourcesDir = Files.createTempDirectory("test_resources").toFile()
        val file = File(tempResourcesDir, fileName)

        return context.resources.openRawResource(resourceId).use { inputStream ->
            file.parentFile?.mkdirs()
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        }
    }

    @Suppress("SameParameterValue")
    private fun createTempFileWithStringContent(
        filename: String,
        content: String,
    ): File {
        val tempFile = File.createTempFile(filename, ".txt", context.filesDir)
        Files.write(tempFile.toPath(), content.toByteArray(Charset.defaultCharset()))
        return tempFile
    }
}
