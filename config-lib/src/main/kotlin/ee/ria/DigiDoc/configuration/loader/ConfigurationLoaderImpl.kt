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
import ee.ria.DigiDoc.configuration.exception.ConfigurationSignatureValidationException
import ee.ria.DigiDoc.configuration.exception.PublicKeyNotFoundException
import ee.ria.DigiDoc.configuration.properties.ConfigurationProperties
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepository
import ee.ria.DigiDoc.configuration.utils.ConfigurationUtil
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_ECC
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.CACHE_CONFIG_FOLDER
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_ECC
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_ECPUB
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_JSON
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.extensions.removeWhitespaces
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.IOException
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
                } catch (e: ConfigurationSignatureValidationException) {
                    errorLog(logTag, "Central configuration signature validation failed", e)
                    restoreLocalConfiguration(context)
                    throw e
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Exception) {
                    errorLog(logTag, "Unable to check Central configuration. Using current configuration", e)
                    restoreLocalConfiguration(context)
                }
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
            val signatureFile = File(cacheDir, CACHED_CONFIG_ECC)

            val isCacheComplete = confFile.exists() && signatureFile.exists()
            if (!isCacheComplete) {
                loadDefaultConfiguration(context)
                return
            }

            val configText = confFile.readText()
            val publicKey = bundledPublicKey(context)
            val storedSignature = signatureFile.readBytes()
            val signature = decodeSignature(storedSignature)

            configurationSignatureVerifier.verifyConfigurationSignature(configText, publicKey, signature)
            val configurationProvider = gson.fromJson(configText, ConfigurationProvider::class.java)

            if (!storedSignature.contentEquals(signature)) {
                debugLog(logTag, "Normalizing the cached configuration signature to its decoded form")
                cacheConfiguration(context, configText, signature)
            }

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

        // Load default configuration if cached configuration does not succeed
        override suspend fun loadLocalConfiguration(context: Context) {
            try {
                loadCachedConfiguration(context, false)
            } catch (e: Exception) {
                errorLog(logTag, "Unable to load cached configuration. Using default configuration", e)
                loadDefaultConfiguration(context)
            }
        }

        override suspend fun loadDefaultConfiguration(context: Context) {
            val assets = context.assets

            val confData = assets.open("config/${DEFAULT_CONFIG_JSON}").bufferedReader().use { it.readText() }
            val publicKey = bundledPublicKey(context)
            val signature =
                decodeSignature(assets.open("config/${DEFAULT_CONFIG_ECC}").use { it.readBytes() })

            configurationSignatureVerifier.verifyConfigurationSignature(confData, publicKey, signature)

            cacheConfiguration(context, confData, signature)
            val configurationProvider = gson.fromJson(confData, ConfigurationProvider::class.java)
            configurationProperties.updateProperties(
                context,
                null,
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
        override suspend fun loadCentralConfiguration(
            context: Context,
            proxySetting: ProxySetting?,
            proxy: ManualProxy,
        ) = loadMutex.withLock {
            loadCentralConfigurationCore(context, proxySetting, proxy)
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

        private suspend fun restoreLocalConfiguration(context: Context) {
            try {
                loadLocalConfiguration(context)
            } catch (recoveryFailure: Exception) {
                errorLog(logTag, "Unable to restore local configuration", recoveryFailure)
            }
        }

        private fun getConfigCacheDir(context: Context): File = File(context.cacheDir, CACHE_CONFIG_FOLDER)

        private fun cacheConfiguration(
            context: Context,
            confData: String,
            signature: ByteArray,
        ) {
            try {
                ConfigurationCache.cacheConfigurationFiles(context, confData, signature)
            } catch (e: Exception) {
                errorLog(logTag, "Unable to cache the configuration, continuing with the loaded one", e)
            }
        }

        private fun bundledPublicKey(context: Context): String {
            val publicKey =
                try {
                    context.assets
                        .open("config/${DEFAULT_CONFIG_ECPUB}")
                        .bufferedReader()
                        .use { it.readText() }
                } catch (e: IOException) {
                    throw PublicKeyNotFoundException("Bundled ${DEFAULT_CONFIG_ECPUB} is missing", e)
                }

            return publicKey.ifBlank {
                throw PublicKeyNotFoundException("Bundled ${DEFAULT_CONFIG_ECPUB} is empty")
            }
        }

        private fun decodeSignature(signatureBytes: ByteArray): ByteArray {
            val signatureText = String(signatureBytes, Charsets.UTF_8).removeWhitespaces().trim()
            if (signatureText.isEmpty()) {
                return signatureBytes
            }

            return if (ConfigurationUtil.isBase64(signatureText)) {
                Base64.getDecoder().decode(signatureText)
            } else {
                signatureBytes
            }
        }

        @Throws(Exception::class)
        private suspend fun loadCentralConfigurationCore(
            context: Context,
            proxySetting: ProxySetting?,
            proxy: ManualProxy,
        ) {
            val cachedSignature =
                ConfigurationCache.getCachedFile(context, CACHED_CONFIG_ECC)

            val currentSignature = cachedSignature.readBytes()

            loadConfigurationProperty(context)

            centralConfigurationRepository.setupProxy(proxySetting, proxy)

            val centralSignature =
                try {
                    Base64.getDecoder().decode(
                        centralConfigurationRepository.fetchSignature().removeWhitespaces().trim(),
                    )
                } catch (e: IllegalArgumentException) {
                    throw ConfigurationSignatureValidationException(e)
                }

            if (!currentSignature.contentEquals(centralSignature)) {
                val centralConfig = centralConfigurationRepository.fetchConfiguration()
                val centralPublicKey = bundledPublicKey(context)

                configurationSignatureVerifier.verifyConfigurationSignature(
                    centralConfig,
                    centralPublicKey,
                    centralSignature,
                )

                val centralConfigurationProvider =
                    gson.fromJson(centralConfig, ConfigurationProvider::class.java)

                if (ConfigurationUtil.isSerialNewerThanCached(
                        configurationFlow.value?.metaInf?.serial ?: 0,
                        centralConfigurationProvider.metaInf.serial,
                    )
                ) {
                    cacheConfiguration(context, centralConfig, centralSignature)
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
    }
