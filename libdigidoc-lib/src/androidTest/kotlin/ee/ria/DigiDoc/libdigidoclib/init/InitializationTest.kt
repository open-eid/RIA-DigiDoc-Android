@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.init

import android.content.Context
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.libdigidoclib.exceptions.AlreadyInitializedException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.lang.reflect.Field

@RunWith(MockitoJUnitRunner::class)
class InitializationTest {
    companion object {
        private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext

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
                tslCerts = listOf(),
                tsaUrl = "https://www.example.com",
                ocspUrls = mapOf("issuer" to "https://www.example.com"),
                ldapPersonUrl = "https://www.example.com",
                ldapCorpUrl = "https://www.example.com",
                midRestUrl = "https://www.example.com",
                midSkRestUrl = "https://www.example.com",
                sidV2RestUrl = "https://www.example.com",
                sidV2SkRestUrl = "https://www.example.com",
                certBundle = listOf(),
                configurationLastUpdateCheckDate = null,
                configurationUpdateDate = null,
            )

        @Mock
        private var configurationRepository: ConfigurationRepository = mock()

        private lateinit var initialization: Initialization

        // Reset using reflection.
        // Libdigidocpp is initialized as singleton and tests might fail after first initialization
        @Throws(
            SecurityException::class,
            NoSuchFieldException::class,
            java.lang.IllegalArgumentException::class,
            IllegalAccessException::class,
        )
        private fun resetInitialization() {
            val field: Field = Initialization::class.java.getDeclaredField("isInitialized")
            field.isAccessible = true
            field.setBoolean(initialization, false)
        }
    }

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        configurationRepository = mock(ConfigurationRepository::class.java)
        initialization = Initialization(configurationRepository)
        resetInitialization()
    }

    @Test
    fun initialization_init_success() {
        try {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            runTest {
                initialization.init(context)
            }
        } catch (e: Exception) {
            fail("No exceptions should be thrown")
        }
    }

    @Test
    fun initialization_init_throwsIllegalArgumentExceptionWithNullContext() {
        val mockContext = mock(Context::class.java)

        `when`(mockContext.resources).thenReturn(context.resources)
        `when`(mockContext.cacheDir).thenReturn(null)
        `when`(mockContext.filesDir).thenReturn(context.filesDir)

        assertThrows(IllegalArgumentException::class.java) {
            runTest {
                initialization.init(mockContext)
            }
        }
    }

    @Test
    fun initialization_init_throwsIllegalArgumentExceptionWithEmptySchemaDir() {
        val cacheDir = context.cacheDir
        val mockContext = mock(Context::class.java)
        `when`(mockContext.resources).thenReturn(Resources.getSystem())
        `when`(mockContext.cacheDir).thenReturn(cacheDir)
        `when`(mockContext.filesDir).thenReturn(context.filesDir)

        assertThrows(NotFoundException::class.java) {
            runTest {
                initialization.init(mockContext)
            }
        }
    }

    @Test
    fun initialization_init_throwsAlreadyInitializedExceptionWhenInitTwice() {
        assertThrows(AlreadyInitializedException::class.java) {
            runTest {
                initialization.init(context)
                initialization.init(context)
            }
        }
    }
}
