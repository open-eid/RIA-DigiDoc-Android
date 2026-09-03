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

package ee.ria.DigiDoc.configuration.loader

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifier
import ee.ria.DigiDoc.configuration.exception.PublicKeyNotFoundException
import ee.ria.DigiDoc.configuration.properties.ConfigurationProperties
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepository
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Date

class ConfigurationLoaderImplTest {
    private lateinit var context: Context
    private lateinit var assets: AssetManager
    private lateinit var centralConfigurationRepository: CentralConfigurationRepository
    private lateinit var configurationSignatureVerifier: ConfigurationSignatureVerifier
    private lateinit var configurationProperties: ConfigurationProperties
    private lateinit var configurationLoader: ConfigurationLoader

    @Before
    fun setUp() {
        assets = mock(AssetManager::class.java)
        context = mock(Context::class.java)
        `when`(context.assets).thenReturn(assets)
        `when`(assets.open("config/default-config.json"))
            .thenReturn(ByteArrayInputStream("{}".toByteArray()))

        centralConfigurationRepository = mock(CentralConfigurationRepository::class.java)
        configurationSignatureVerifier = mock(ConfigurationSignatureVerifier::class.java)
        configurationProperties = mock(ConfigurationProperties::class.java)

        configurationLoader =
            ConfigurationLoaderImpl(
                Gson(),
                centralConfigurationRepository,
                ConfigurationProperty(),
                configurationProperties,
                configurationSignatureVerifier,
            )
    }

    @Test
    fun configurationLoader_loadDefaultConfiguration_decodesBase64SignatureCarryingTrailingNewline() {
        val bundledKey = "-----BEGIN PUBLIC KEY-----\nBUNDLED\n-----END PUBLIC KEY-----\n"
        `when`(assets.open("config/default-config.ecpub"))
            .thenReturn(ByteArrayInputStream(bundledKey.toByteArray()))
        `when`(assets.open("config/default-config.ecc"))
            .thenReturn(ByteArrayInputStream("dGVzdA==\n".toByteArray()))

        try {
            runBlocking { configurationLoader.loadDefaultConfiguration(context) }
        } catch (_: Exception) {
        }

        verify(configurationSignatureVerifier).verifyConfigurationSignature(
            any(),
            any(),
            check { assertArrayEquals("test".toByteArray(), it) },
        )
    }

    @Test
    fun configurationLoader_shouldCheckForUpdates_honoursTheConfiguredUpdateInterval() {
        val loader =
            ConfigurationLoaderImpl(
                Gson(),
                centralConfigurationRepository,
                ConfigurationProperty(updateInterval = 7),
                configurationProperties,
                configurationSignatureVerifier,
            )
        `when`(configurationProperties.getConfigurationLastCheckDate(context))
            .thenReturn(Date.from(Instant.now().minus(5, ChronoUnit.DAYS)))

        assertFalse(runBlocking { loader.shouldCheckForUpdates(context) })
    }

    @Test
    fun configurationLoader_shouldCheckForUpdates_afterTheConfiguredUpdateInterval() {
        val loader =
            ConfigurationLoaderImpl(
                Gson(),
                centralConfigurationRepository,
                ConfigurationProperty(updateInterval = 7),
                configurationProperties,
                configurationSignatureVerifier,
            )
        `when`(configurationProperties.getConfigurationLastCheckDate(context))
            .thenReturn(Date.from(Instant.now().minus(8, ChronoUnit.DAYS)))

        assertTrue(runBlocking { loader.shouldCheckForUpdates(context) })
    }

    @Test
    fun configurationLoader_loadDefaultConfiguration_clearsLastCheckDateSoAnUpdateIsNotDeferred() {
        val bundledKey = "-----BEGIN PUBLIC KEY-----\nBUNDLED\n-----END PUBLIC KEY-----\n"
        `when`(assets.open("config/default-config.ecpub"))
            .thenReturn(ByteArrayInputStream(bundledKey.toByteArray()))
        `when`(assets.open("config/default-config.ecc"))
            .thenReturn(ByteArrayInputStream("dGVzdA==\n".toByteArray()))
        `when`(assets.open("config/default-config.json"))
            .thenReturn(
                ByteArrayInputStream(
                    """{"META-INF":{"VER":1,"SERIAL":205,"URL":"https://id.eesti.ee/config.json","DATE":"20260101000000Z"}}"""
                        .toByteArray(),
                ),
            )
        `when`(context.cacheDir).thenReturn(Files.createTempDirectory("loaderDefaultTest").toFile())

        runBlocking { configurationLoader.loadDefaultConfiguration(context) }

        verify(configurationProperties).clearConfigurationLastCheckDate(context)
    }

    @Test
    fun configurationLoader_loadCentralConfiguration_recordsTheAppVersionSoAnUpdateForcesTheNextCheck() {
        val bundledKey = "-----BEGIN PUBLIC KEY-----\nBUNDLED\n-----END PUBLIC KEY-----\n"
        val cacheDir = Files.createTempDirectory("loaderVersionTest").toFile()
        File(cacheDir, "config").mkdirs()
        File(cacheDir, "config/active-config.ecc").writeBytes("test".toByteArray())
        File(cacheDir, "config/active-config.json").writeText(
            """{"META-INF":{"VER":1,"SERIAL":205,"URL":"https://id.eesti.ee/config.json","DATE":"20260101000000Z"}}""",
        )

        `when`(context.cacheDir).thenReturn(cacheDir)
        `when`(assets.open("config/default-config.ecpub"))
            .thenReturn(ByteArrayInputStream(bundledKey.toByteArray()))
        `when`(configurationProperties.getConfigurationProperties(context))
            .thenReturn(ConfigurationProperty("https://example.org", 4, 1, LocalDateTime.now()))

        runBlocking {
            `when`(centralConfigurationRepository.fetchSignature()).thenReturn("dGVzdA==")

            configurationLoader.loadCentralConfiguration(
                context,
                ProxySetting.NO_PROXY,
                ManualProxy("", 80, "", ""),
            )
        }

        verify(configurationProperties).setLastCheckedAppVersion(eq(context), any())
    }

    @Test
    fun configurationLoader_loadCentralConfiguration_verifiesWithBundledKeyNotAServerSuppliedOne() {
        val bundledKey = "-----BEGIN PUBLIC KEY-----\nBUNDLED\n-----END PUBLIC KEY-----\n"
        val cacheDir = Files.createTempDirectory("loaderCentralTest").toFile()
        File(cacheDir, "config").mkdirs()
        File(cacheDir, "config/active-config.ecc").writeBytes("stale".toByteArray())

        `when`(context.cacheDir).thenReturn(cacheDir)
        `when`(assets.open("config/default-config.ecpub"))
            .thenReturn(ByteArrayInputStream(bundledKey.toByteArray()))
        `when`(configurationProperties.getConfigurationProperties(context))
            .thenReturn(ConfigurationProperty("https://example.org", 4, 1, LocalDateTime.now()))

        try {
            runBlocking {
                `when`(centralConfigurationRepository.fetchSignature()).thenReturn("dGVzdA==")
                `when`(centralConfigurationRepository.fetchConfiguration()).thenReturn("{}")

                configurationLoader.loadCentralConfiguration(
                    context,
                    ProxySetting.NO_PROXY,
                    ManualProxy("", 80, "", ""),
                )
            }
        } catch (_: Exception) {
        }

        verify(configurationSignatureVerifier).verifyConfigurationSignature(any(), eq(bundledKey), any())
    }

    @Test
    fun configurationLoader_loadDefaultConfiguration_throwsWhenBundledPublicKeyIsEmpty() {
        `when`(assets.open("config/default-config.ecpub"))
            .thenReturn(ByteArrayInputStream(ByteArray(0)))

        assertThrows(PublicKeyNotFoundException::class.java) {
            runBlocking { configurationLoader.loadDefaultConfiguration(context) }
        }
    }

    @Test
    fun configurationLoader_loadDefaultConfiguration_throwsWhenBundledPublicKeyIsBlank() {
        `when`(assets.open("config/default-config.ecpub"))
            .thenReturn(ByteArrayInputStream("   \n".toByteArray()))

        assertThrows(PublicKeyNotFoundException::class.java) {
            runBlocking { configurationLoader.loadDefaultConfiguration(context) }
        }
    }

    @Test
    fun configurationLoader_loadDefaultConfiguration_throwsWhenBundledPublicKeyIsMissing() {
        `when`(assets.open("config/default-config.ecpub"))
            .thenThrow(FileNotFoundException("config/default-config.ecpub"))

        assertThrows(PublicKeyNotFoundException::class.java) {
            runBlocking { configurationLoader.loadDefaultConfiguration(context) }
        }
    }

    @Test
    fun configurationLoader_loadDefaultConfiguration_verifiesWithBundledKeyAndNeverTheServerKey() {
        val bundledKey = "-----BEGIN PUBLIC KEY-----\nBUNDLED\n-----END PUBLIC KEY-----\n"
        `when`(assets.open("config/default-config.ecpub"))
            .thenReturn(ByteArrayInputStream(bundledKey.toByteArray()))
        `when`(assets.open("config/default-config.ecc"))
            .thenReturn(ByteArrayInputStream(byteArrayOf(1, 2, 3)))

        try {
            runBlocking { configurationLoader.loadDefaultConfiguration(context) }
        } catch (_: Exception) {
        }

        verify(configurationSignatureVerifier).verifyConfigurationSignature(any(), eq(bundledKey), any())
    }
}
