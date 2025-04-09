@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.cryptolib.CryptoContainer.Companion.openOrCreate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class CryptoContainerTest {
    companion object {
        private lateinit var configurationLoader: ConfigurationLoader
        private lateinit var configurationRepository: ConfigurationRepository

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
                } catch (_: Exception) {
                }
            }
        }
    }

    private lateinit var context: Context

    private lateinit var testFile: File
    private lateinit var dataFile1: File
    private lateinit var dataFile2: File
    private lateinit var dataFile3: File

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        testFile = File.createTempFile("testFile", ".txt", context.filesDir)
        dataFile1 = File.createTempFile("dataFile1", ".txt", context.filesDir)
        dataFile2 = File.createTempFile("dataFile2", ".txt", context.filesDir)
        dataFile3 = File.createTempFile("dataFile3", ".txt", context.filesDir)
    }

    @After
    fun tearDown() {
        testFile.delete()
        dataFile1.delete()
        dataFile2.delete()
        dataFile3.delete()
    }

    @Test
    fun cryptoContainer_rawContainer_success() =
        runTest {
            val dataFiles = listOf(testFile)

            val cdoc2Settings = CDOC2Settings(context)
            val cryptoContainer = openOrCreate(context, testFile, dataFiles, cdoc2Settings)

            val result = cryptoContainer.file

            assertNotNull(result)
        }
}
