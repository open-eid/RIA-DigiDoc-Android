@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.loader

import android.content.Context
import com.google.gson.Gson
import ee.ria.DigiDoc.common.extensions.removeWhitespaces
import ee.ria.DigiDoc.configuration.ConfigurationManager
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationProvider
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifier
import ee.ria.DigiDoc.configuration.cache.ConfigurationCache.cacheConfigurationFiles
import ee.ria.DigiDoc.configuration.cache.ConfigurationCache.getCachedFile
import ee.ria.DigiDoc.configuration.domain.model.ConfigurationData
import ee.ria.DigiDoc.configuration.properties.ConfigurationProperties.getConfigurationLastCheckDate
import ee.ria.DigiDoc.configuration.properties.ConfigurationProperties.getConfigurationUpdatedDate
import ee.ria.DigiDoc.configuration.properties.ConfigurationProperties.updateProperties
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepository
import ee.ria.DigiDoc.configuration.service.CentralConfigurationService
import ee.ria.DigiDoc.configuration.utils.ConfigurationUtil.isBase64
import ee.ria.DigiDoc.configuration.utils.ConfigurationUtil.isSerialNewerThanCached
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_PUB
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_RSA
import ee.ria.DigiDoc.configuration.utils.Constant.CACHE_CONFIG_FOLDER
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_PUB
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_RSA
import ee.ria.DigiDoc.configuration.utils.Constant.PROPERTIES_FILE_NAME
import ee.ria.DigiDoc.network.configuration.client.CentralConfigurationClient
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import org.bouncycastle.util.encoders.Base64
import java.io.File
import java.util.Date
import java.util.Properties

object ConfigurationLoader {
    private val gson = Gson()

    fun loadConfigurationProperty(context: Context): ConfigurationProperty {
        val properties = Properties()
        context.assets.open("config/${PROPERTIES_FILE_NAME}").use { inputStream ->
            properties.load(inputStream)
        }

        val mappedProperties =
            properties.entries.associate {
                it.key.toString() to it.value.toString()
            }
        return ConfigurationProperty.fromProperties(mappedProperties)
    }

    fun loadCachedConfiguration(context: Context): ConfigurationProvider? {
        val cacheDir = File(context.cacheDir, CACHE_CONFIG_FOLDER)

        val confFile = File(cacheDir, CACHED_CONFIG_JSON)
        val publicKeyFile = File(cacheDir, CACHED_CONFIG_PUB)
        val signatureFile = File(cacheDir, CACHED_CONFIG_RSA)

        return if (confFile.exists() && publicKeyFile.exists() && signatureFile.exists()) {
            val signatureBytes = signatureFile.readBytes()
            val signatureText = String(signatureBytes, Charsets.UTF_8)

            val signature =
                if (isBase64(signatureText)) {
                    Base64.decode(signatureText)
                } else {
                    signatureBytes
                }

            ConfigurationSignatureVerifier(publicKeyFile.readText())
                .verifyConfigurationSignature(confFile.readText(), signature)
            val configText = confFile.readText()
            val configurationProvider = gson.fromJson(configText, ConfigurationProvider::class.java)
            cacheConfigurationFiles(
                context,
                configText,
                publicKeyFile.readText(),
                signature,
            )
            updateProperties(
                context,
                configurationProvider.configurationLastUpdateCheckDate,
                configurationProvider.configurationUpdateDate,
                configurationProvider.metaInf.serial,
            )
            configurationProvider.configurationLastUpdateCheckDate =
                getConfigurationLastCheckDate(context)
            configurationProvider.configurationUpdateDate =
                getConfigurationUpdatedDate(context)
            configurationProvider
        } else {
            null
        }
    }

    fun loadDefaultConfiguration(context: Context): ConfigurationProvider? {
        val assets = context.assets

        val confData = assets.open("config/${DEFAULT_CONFIG_JSON}").bufferedReader().use { it.readText() }
        val publicKey = assets.open("config/${DEFAULT_CONFIG_PUB}").bufferedReader().use { it.readText() }
        val signatureBytes = assets.open("config/${DEFAULT_CONFIG_RSA}").readBytes()

        val signatureText = String(signatureBytes, Charsets.UTF_8).removeWhitespaces()

        val signature =
            if (isBase64(signatureText)) {
                Base64.decode(signatureText)
            } else {
                signatureBytes
            }

        ConfigurationSignatureVerifier(publicKey).verifyConfigurationSignature(confData, signature)

        cacheConfigurationFiles(
            context,
            confData,
            publicKey,
            signature,
        )
        val configurationProvider = gson.fromJson(confData, ConfigurationProvider::class.java)
        updateProperties(
            context,
            Date(),
            Date(),
            configurationProvider.metaInf.serial,
        )
        configurationProvider.configurationLastUpdateCheckDate =
            getConfigurationLastCheckDate(context)
        configurationProvider.configurationUpdateDate =
            getConfigurationUpdatedDate(context)
        return configurationProvider
    }

    @Throws(Exception::class)
    suspend fun loadCentralConfigurationData(
        configurationServiceUrl: String,
        userAgent: String,
    ): ConfigurationData {
        val apiService: CentralConfigurationService =
            CentralConfigurationClient(
                configurationServiceUrl,
                userAgent,
            ).retrofit.create(CentralConfigurationService::class.java)
        val centralConfigurationRepository = CentralConfigurationRepository(apiService)
        val centralSignature =
            Base64.decode(centralConfigurationRepository.fetchSignature().trim().removeWhitespaces())
        val centralConfig = centralConfigurationRepository.fetchConfiguration()
        val centralPublicKey = centralConfigurationRepository.fetchPublicKey()

        ConfigurationSignatureVerifier(
            centralPublicKey,
        ).verifyConfigurationSignature(centralConfig, centralSignature)

        return ConfigurationData(centralConfig, centralPublicKey, centralSignature)
    }

    @Throws(Exception::class)
    suspend fun loadCentralConfiguration(context: Context) {
        val configurationProperty = loadConfigurationProperty(context)
        val apiService: CentralConfigurationService =
            CentralConfigurationClient(
                configurationProperty.centralConfigurationServiceUrl,
                UserAgentUtil.getUserAgent(context),
            ).retrofit.create(CentralConfigurationService::class.java)
        val centralConfigurationRepository = CentralConfigurationRepository(apiService)

        val cachedSignature =
            getCachedFile(context, CACHED_CONFIG_RSA)

        val currentSignature = cachedSignature.readBytes()

        val centralSignature = Base64.decode(centralConfigurationRepository.fetchSignature().trim().removeWhitespaces())

        if (currentSignature.contentEquals(centralSignature)) {
            val centralConfig = centralConfigurationRepository.fetchConfiguration()
            val centralPublicKey = centralConfigurationRepository.fetchPublicKey()

            val centralConfigurationProvider = gson.fromJson(centralConfig, ConfigurationProvider::class.java)

            ConfigurationSignatureVerifier(
                centralPublicKey,
            ).verifyConfigurationSignature(centralConfig, centralSignature)

            if (isSerialNewerThanCached(
                    ConfigurationManager.getConfigurationFlow().value?.metaInf?.serial ?: 0,
                    centralConfigurationProvider.metaInf.serial,
                )
            ) {
                cacheConfigurationFiles(
                    context,
                    centralConfig,
                    centralPublicKey,
                    centralSignature,
                )
                updateProperties(
                    context,
                    Date(),
                    Date(),
                    centralConfigurationProvider.metaInf.serial,
                )
                centralConfigurationProvider.configurationLastUpdateCheckDate =
                    getConfigurationLastCheckDate(context)
                centralConfigurationProvider.configurationUpdateDate =
                    getConfigurationUpdatedDate(context)
                ConfigurationManager.setConfigurationFlow(centralConfigurationProvider)
            } else {
                val cachedConfig = loadCachedConfiguration(context)
                updateProperties(
                    context,
                    cachedConfig?.configurationLastUpdateCheckDate,
                    cachedConfig?.configurationUpdateDate,
                    cachedConfig?.metaInf?.serial,
                )
                cachedConfig?.configurationLastUpdateCheckDate =
                    getConfigurationLastCheckDate(context)
                ConfigurationManager.setConfigurationFlow(cachedConfig)
            }
        } else {
            val cachedConfig = loadCachedConfiguration(context)
            ConfigurationManager.setConfigurationFlow(cachedConfig)
        }
    }
}
