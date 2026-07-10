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
import com.google.gson.Gson
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifier
import ee.ria.DigiDoc.configuration.cache.ConfigurationCache
import ee.ria.DigiDoc.configuration.domain.model.ConfigurationData
import ee.ria.DigiDoc.configuration.properties.ConfigurationProperties
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepository
import ee.ria.DigiDoc.configuration.utils.ConfigurationUtil
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_PUB
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_RSA
import ee.ria.DigiDoc.configuration.utils.Constant.CACHE_CONFIG_FOLDER
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_PUB
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_RSA
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.extensions.removeWhitespaces
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurationLoaderImpl
    @Inject
    constructor(
        private val gson: Gson,
        private val centralConfigurationRepository: CentralConfigurationRepository,
        private val configurationProperty: ConfigurationProperty,
        private val configurationProperties: ConfigurationProperties,
        private val configurationSignatureVerifier: ConfigurationSignatureVerifier,
    ) : ConfigurationLoader {
        private val logTag = "ConfigurationLoader"
        private val configurationFlow = MutableStateFlow<ConfigurationProvider?>(null)

        private val loadMutex = Mutex()

        @Throws(Exception::class)
        override suspend fun initConfiguration(
            context: Context,
            proxySetting: ProxySetting?,
            manualProxy: ManualProxy,
        ) = loadMutex.withLock {
            val cacheDir = getConfigCacheDir(context)
            if (!cacheDir.exists()) {
                cacheDir.mkdir()
            }

            loadLocalConfiguration(context)

            loadConfigurationProperty(context)

            if (shouldCheckForUpdates(context)) {
                try {
                    loadCentralConfigurationCore(context, proxySetting, manualProxy)
                } catch (e: Exception) {
                    errorLog(logTag, "Unable to check Central configuration. Using current configuration", e)
                }
            }
        }

        // Load default configuration if cached configuration does not succeed
        private suspend fun loadLocalConfiguration(context: Context) {
            try {
                loadCachedConfiguration(context, false)
            } catch (e: Exception) {
                errorLog(logTag, "Unable to load cached configuration. Using default configuration", e)
                loadDefaultConfiguration(context)
            }
        }

        private fun getConfigCacheDir(context: Context): File = File(context.cacheDir, CACHE_CONFIG_FOLDER)

        private fun decodeSignature(signatureBytes: ByteArray): ByteArray {
            val signatureText = String(signatureBytes, Charsets.UTF_8)
            return if (ConfigurationUtil.isBase64(signatureText)) {
                Base64.getDecoder().decode(signatureText)
            } else {
                signatureBytes
            }
        }

        override fun getConfigurationFlow(): StateFlow<ConfigurationProvider?> = configurationFlow

        override suspend fun loadConfigurationProperty(context: Context): ConfigurationProperty {
            val properties = configurationProperties.getConfigurationProperties(context)
            configurationProperty.centralConfigurationServiceUrl = properties.centralConfigurationServiceUrl
            configurationProperty.updateInterval = properties.updateInterval
            configurationProperty.versionSerial = properties.versionSerial
            configurationProperty.downloadDate = properties.downloadDate
            return configurationProperties.getConfigurationProperties(context)
        }

        override suspend fun loadCachedConfiguration(
            context: Context,
            afterCentralCheck: Boolean,
        ) {
            val cacheDir = getConfigCacheDir(context)
            val confFile = File(cacheDir, CACHED_CONFIG_JSON)
            val publicKeyFile = File(cacheDir, CACHED_CONFIG_PUB)
            val signatureFile = File(cacheDir, CACHED_CONFIG_RSA)

            val isCacheComplete = confFile.exists() && publicKeyFile.exists() && signatureFile.exists()
            if (!isCacheComplete) {
                loadDefaultConfiguration(context)
                return
            }

            val configText = confFile.readText()
            val publicKey = publicKeyFile.readText()
            val signature = decodeSignature(signatureFile.readBytes())

            configurationSignatureVerifier.verifyConfigurationSignature(configText, publicKey, signature)
            val configurationProvider = gson.fromJson(configText, ConfigurationProvider::class.java)
            ConfigurationCache.cacheConfigurationFiles(context, configText, publicKey, signature)

            if (!afterCentralCheck) {
                configurationProperties.updateProperties(
                    context,
                    configurationProvider.configurationLastUpdateCheckDate,
                    configurationProvider.configurationUpdateDate,
                    configurationProvider.metaInf.serial,
                )
                configurationProvider.configurationLastUpdateCheckDate =
                    configurationProperties.getConfigurationLastCheckDate(context)
                configurationProvider.configurationUpdateDate =
                    configurationProperties.getConfigurationUpdatedDate(context)
                configurationFlow.value = configurationProvider
            } else {
                val currentDate = Date()
                configurationProvider.configurationUpdateDate = configurationProvider.configurationUpdateDate
                    ?: configurationFlow.value?.configurationUpdateDate
                configurationProvider.configurationLastUpdateCheckDate = currentDate
                configurationProperties.setConfigurationLastCheckDate(context, currentDate)
                configurationFlow.value = configurationProvider
            }
        }

        override suspend fun loadDefaultConfiguration(context: Context) {
            val assets = context.assets

            val confData = assets.open("config/${DEFAULT_CONFIG_JSON}").bufferedReader().use { it.readText() }
            val publicKey = assets.open("config/${DEFAULT_CONFIG_PUB}").bufferedReader().use { it.readText() }
            val signature = decodeSignature(assets.open("config/${DEFAULT_CONFIG_RSA}").readBytes())

            configurationSignatureVerifier.verifyConfigurationSignature(confData, publicKey, signature)

            ConfigurationCache.cacheConfigurationFiles(
                context,
                confData,
                publicKey,
                signature,
            )
            val configurationProvider = gson.fromJson(confData, ConfigurationProvider::class.java)
            configurationProperties.updateProperties(
                context,
                DateUtil.getConfigurationDate(configurationProvider.metaInf.date),
                DateUtil.getConfigurationDate(configurationProvider.metaInf.date),
                configurationProvider.metaInf.serial,
            )
            configurationProvider.configurationLastUpdateCheckDate =
                configurationProperties.getConfigurationLastCheckDate(context)
            configurationProvider.configurationUpdateDate =
                configurationProperties.getConfigurationUpdatedDate(context)
            configurationFlow.value = configurationProvider
        }

        @Throws(Exception::class)
        override suspend fun loadCentralConfigurationData(
            configurationServiceUrl: String,
            userAgent: String,
        ): ConfigurationData {
            val centralSignature =
                Base64.getDecoder().decode(centralConfigurationRepository.fetchSignature().trim())
            val centralConfig = centralConfigurationRepository.fetchConfiguration()
            val centralPublicKey = centralConfigurationRepository.fetchPublicKey()

            configurationSignatureVerifier.verifyConfigurationSignature(
                centralConfig,
                centralPublicKey,
                centralSignature,
            )

            return ConfigurationData(centralConfig, centralPublicKey, centralSignature)
        }

        @Throws(Exception::class)
        override suspend fun loadCentralConfiguration(
            context: Context,
            proxySetting: ProxySetting?,
            proxy: ManualProxy,
        ) = loadMutex.withLock {
            loadCentralConfigurationCore(context, proxySetting, proxy)
        }

        @Throws(Exception::class)
        private suspend fun loadCentralConfigurationCore(
            context: Context,
            proxySetting: ProxySetting?,
            proxy: ManualProxy,
        ) {
            val cachedSignature =
                ConfigurationCache.getCachedFile(context, CACHED_CONFIG_RSA)

            val currentSignature = cachedSignature.readBytes()

            loadConfigurationProperty(context)

            centralConfigurationRepository.setupProxy(proxySetting, proxy)

            val centralSignature =
                Base64.getDecoder().decode(
                    centralConfigurationRepository.fetchSignature().removeWhitespaces().trim(),
                )

            if (!currentSignature.contentEquals(centralSignature)) {
                val centralConfig = centralConfigurationRepository.fetchConfiguration()
                val centralPublicKey = centralConfigurationRepository.fetchPublicKey()

                val centralConfigurationProvider =
                    gson.fromJson(centralConfig, ConfigurationProvider::class.java)

                configurationSignatureVerifier.verifyConfigurationSignature(
                    centralConfig,
                    centralPublicKey,
                    centralSignature,
                )

                if (ConfigurationUtil.isSerialNewerThanCached(
                        configurationFlow.value?.metaInf?.serial ?: 0,
                        centralConfigurationProvider.metaInf.serial,
                    )
                ) {
                    ConfigurationCache.cacheConfigurationFiles(
                        context,
                        centralConfig,
                        centralPublicKey,
                        centralSignature,
                    )
                    configurationProperties.updateProperties(
                        context,
                        Date(),
                        Date(),
                        centralConfigurationProvider.metaInf.serial,
                    )

                    val currentDate = Date()
                    centralConfigurationProvider.configurationLastUpdateCheckDate = currentDate
                    centralConfigurationProvider.configurationUpdateDate = currentDate
                    configurationFlow.value = centralConfigurationProvider
                } else {
                    loadCachedConfiguration(context, true)
                    configurationProperties.updateProperties(
                        context,
                        configurationFlow.value?.configurationLastUpdateCheckDate,
                        configurationFlow.value?.configurationUpdateDate,
                        configurationFlow.value?.metaInf?.serial,
                    )
                    configurationFlow.value?.configurationLastUpdateCheckDate =
                        configurationProperties.getConfigurationLastCheckDate(context)
                }
            } else {
                loadCachedConfiguration(context, true)
            }
        }

        override suspend fun shouldCheckForUpdates(context: Context): Boolean {
            val lastExecutionDate =
                configurationProperties
                    .getConfigurationLastCheckDate(context)
                    ?.toInstant()
                    ?.atZone(ZoneId.systemDefault())
                    ?.toLocalDateTime()

            if (lastExecutionDate == null) {
                return true
            }

            val currentDate = LocalDateTime.now()

            val daysSinceLastUpdateCheck = ChronoUnit.DAYS.between(lastExecutionDate, currentDate)

            return daysSinceLastUpdateCheck >= 4
        }
    }
