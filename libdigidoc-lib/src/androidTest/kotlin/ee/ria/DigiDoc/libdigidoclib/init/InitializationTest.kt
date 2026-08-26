/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.init

import android.content.Context
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.Constant.Defaults.DEFAULT_UUID_VALUE
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile.Companion.getResourceFileAsFile
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer.Companion.openOrCreate
import ee.ria.DigiDoc.libdigidoclib.exceptions.AlreadyInitializedException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
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
import java.util.UUID

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
                ldapPersonUrl = "https://www.example.com",
                ldapPersonUrls = listOf("https://www.example.com"),
                ldapCorpUrl = "https://www.example.com",
                midRestUrl = "https://www.example.com",
                midSkRestUrl = "https://www.example.com",
                sidV2RestUrl = "https://www.example.com",
                sidV2SkRestUrl = "https://www.example.com",
                certBundle = listOf(),
                configurationLastUpdateCheckDate = null,
                configurationUpdateDate = null,
                cdoc2Conf =
                    mapOf(
                        DEFAULT_UUID_VALUE to
                            ConfigurationProvider.CDOC2Conf(
                                uuid = UUID.randomUUID(),
                                name = "RIA",
                                post = "https://cdoc2.id.ee:8443",
                                fetch = "https://cdoc2.id.ee:8444",
                            ),
                    ),
                cdoc2Default = false,
                cdoc2UseKeyServer = false,
                cdoc2DefaultKeyServer = DEFAULT_UUID_VALUE,
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

        @Throws(
            SecurityException::class,
            NoSuchFieldException::class,
            java.lang.IllegalArgumentException::class,
            IllegalAccessException::class,
        )
        private fun isInitializedFlag(): Boolean {
            val field: Field = Initialization::class.java.getDeclaredField("isInitialized")
            field.isAccessible = true
            return field.getBoolean(initialization)
        }
    }

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        configurationRepository = mock(ConfigurationRepository::class.java)
        LibdigidocLibraryLoader().init(context)
        initialization = Initialization(configurationRepository)
        resetInitialization()
    }

    @Test
    fun initialization_init_doesNotThrowWhenTslUnreachable() {
        try {
            val configuration = configurationProvider.copy(tslUrl = "http://127.0.0.1:1/eu-lotl.xml")
            `when`(configurationRepository.getConfiguration()).thenReturn(configuration)
            runTest {
                initialization.init(context)
            }
        } catch (e: Exception) {
            fail("Failing to download the list must not be fatal, but init threw: $e")
        }

        assertTrue(
            "The library should still be marked as initialized when the list cannot be downloaded",
            isInitializedFlag(),
        )
    }

    @Test
    fun initialization_init_containerRemainsUsableWhenTslUnreachable() {
        val configuration = configurationProvider.copy(tslUrl = "http://127.0.0.1:1/eu-lotl.xml")
        `when`(configurationRepository.getConfiguration()).thenReturn(configuration)

        runTest {
            initialization.init(context)

            val containerFile =
                getResourceFileAsFile(context, "example.asice", ee.ria.DigiDoc.common.R.raw.example)
            val signedContainer = openOrCreate(context, containerFile, listOf(containerFile), false)

            assertEquals(2, signedContainer.getSignatures().size)
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
        `when`(configurationRepository.getConfiguration())
            .thenReturn(configurationProvider.copy(tslUrl = "http://127.0.0.1:1/eu-lotl.xml"))
        assertThrows(AlreadyInitializedException::class.java) {
            runTest {
                initialization.init(context)
                initialization.init(context)
            }
        }
    }
}
