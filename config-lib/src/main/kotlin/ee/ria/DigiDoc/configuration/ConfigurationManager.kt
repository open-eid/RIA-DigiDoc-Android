@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import android.content.Context
import android.content.res.AssetManager
import androidx.annotation.StringRes
import ee.ria.DigiDoc.configuration.ConfigurationProvider.MetaInf
import ee.ria.DigiDoc.configuration.loader.CachedConfigurationHandler
import ee.ria.DigiDoc.configuration.loader.CachedConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.CentralConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.DefaultConfigurationLoader
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.CACHED_CONFIG_RSA
import ee.ria.DigiDoc.configuration.utils.Parser
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.infoLog
import java.io.FileNotFoundException
import java.io.IOException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Date
import java.util.Locale

class ConfigurationManager(
    context: Context,
    private val configurationProperties: ConfigurationProperties,
    private val cachedConfigurationHandler: CachedConfigurationHandler,
    userAgent: String,
) {
    @Suppress("PropertyName")
    private val LOG_TAG = javaClass.simpleName
    private val centralConfigurationServiceUrl: String = configurationProperties.centralConfigurationServiceUrl
    private val centralConfigurationLoader: CentralConfigurationLoader
    private val defaultConfigurationLoader: DefaultConfigurationLoader
    private val cachedConfigurationLoader: CachedConfigurationLoader
    private lateinit var confSignatureVerifier: ConfigurationSignatureVerifier
    private val context: Context

    init {
        centralConfigurationLoader =
            CentralConfigurationLoader(
                context, centralConfigurationServiceUrl,
                userAgent,
            )
        defaultConfigurationLoader = DefaultConfigurationLoader(context.assets)
        cachedConfigurationLoader = CachedConfigurationLoader(cachedConfigurationHandler)
        this.context = context
    }

    val configuration: ConfigurationProvider
        get() =
            if (shouldUpdateConfiguration()) {
                loadCentralConfiguration()
            } else {
                loadCachedConfiguration()
            }

    fun forceLoadCachedConfiguration(): ConfigurationProvider {
        return loadCachedConfiguration()
    }

    fun forceLoadDefaultConfiguration(): ConfigurationProvider {
        return loadDefaultConfiguration()
    }

    fun forceLoadCentralConfiguration(): ConfigurationProvider {
        return loadCentralConfiguration()
    }

    private fun shouldUpdateConfiguration(): Boolean {
        return !cachedConfigurationHandler.doesCachedConfigurationExist() || isConfigurationOutdated
    }

    private val isConfigurationOutdated: Boolean
        get() {
            val currentDate = Date()
            val confLastUpdateDate =
                cachedConfigurationHandler.confLastUpdateCheckDate ?: return true
            val diffTime = currentDate.time - confLastUpdateDate.time
            val differenceInDays = diffTime / (1000 * 60 * 60 * 24)
            return differenceInDays > configurationProperties.configurationUpdateInterval
        }

    private fun loadCentralConfiguration(): ConfigurationProvider {
        return try {
            infoLog(
                LOG_TAG,
                "Attempting to load configuration from central configuration service <$centralConfigurationServiceUrl>",
            )
            val centralConfigurationSignature =
                centralConfigurationLoader.loadConfigurationSignature()
            val currentDate = Date()
            cachedConfigurationHandler.updateConfigurationLastCheckDate(currentDate)
            if (cachedConfigurationHandler.doesCachedConfigurationExist() &&
                isCachedConfUpToDate(
                    centralConfigurationSignature,
                )
            ) {
                infoLog(
                    LOG_TAG,
                    "Cached configuration signature matches with central configuration signature. " +
                        "Not updating and using cached configuration",
                )
                cachedConfigurationHandler.updateProperties()
                return loadCachedConfiguration()
            }
            centralConfigurationLoader.loadConfigurationJson()
            verifyConfigurationSignature(centralConfigurationLoader)
            cachedConfigurationHandler.updateConfigurationUpdatedDate(currentDate)
            infoLog(
                LOG_TAG,
                "Configuration successfully loaded from central configuration service",
            )
            parseAndCacheConfiguration(centralConfigurationLoader)
        } catch (e: Exception) {
            errorLog(LOG_TAG, "Failed to load configuration from central configuration service", e)
            loadCachedConfiguration()
        }
    }

    private fun loadCachedConfiguration(): ConfigurationProvider {
        return try {
            infoLog(LOG_TAG, "Attempting to load cached configuration")
            cachedConfigurationLoader.load()
            verifyConfigurationSignature(cachedConfigurationLoader)
            infoLog(LOG_TAG, "Cached configuration signature verified")
            parseConfigurationProvider(cachedConfigurationLoader.configurationJson)
        } catch (e: Exception) {
            errorLog(LOG_TAG, "Failed to load cached configuration", e)
            loadDefaultConfiguration()
        }
    }

    private fun loadDefaultConfiguration(): ConfigurationProvider {
        return try {
            infoLog(LOG_TAG, "Attempting to load default configuration")
            defaultConfigurationLoader.load()
            verifyConfigurationSignature(defaultConfigurationLoader)
            infoLog(LOG_TAG, "Default configuration signature verified")
            overrideConfUpdateDateWithDefaultConfigurationInitDownloadDate()
            parseAndCacheConfiguration(defaultConfigurationLoader)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load default configuration", e)
        }
    }

    private fun verifyConfigurationSignature(configurationLoader: ConfigurationLoader) {
        var publicKey = defaultConfigurationLoader.configurationSignaturePublicKey
        if (publicKey == null) {
            publicKey = defaultConfigurationLoader.loadConfigurationSignaturePublicKey()
        }
        confSignatureVerifier = ConfigurationSignatureVerifier(publicKey)
        configurationLoader.configurationJson?.let { json ->
            configurationLoader.configurationSignature?.let { signature ->
                confSignatureVerifier.verifyConfigurationSignature(
                    json,
                    signature,
                )
            }
        }
    }

    private fun isCachedConfUpToDate(centralConfigurationSignature: ByteArray?): Boolean {
        val cachedConfSignature =
            cachedConfigurationHandler.readFileContentBytes(CACHED_CONFIG_RSA)
        return cachedConfSignature.contentEquals(centralConfigurationSignature)
    }

    private fun overrideConfUpdateDateWithDefaultConfigurationInitDownloadDate() {
        val defaultConfInitDownloadDate =
            configurationProperties.packagedConfigurationInitialDownloadDate
        val cachedConfUpdateDate = cachedConfigurationHandler.confUpdateDate
        if (cachedConfUpdateDate == null || defaultConfInitDownloadDate.after(cachedConfUpdateDate)) {
            cachedConfigurationHandler.updateConfigurationUpdatedDate(defaultConfInitDownloadDate)
        }
    }

    private fun parseAndCacheConfiguration(configurationLoader: ConfigurationLoader): ConfigurationProvider {
        val configurationProvider =
            parseConfigurationProvider(configurationLoader.configurationJson)
        cacheConfiguration(configurationLoader, configurationProvider)
        infoLog(LOG_TAG, "Configuration successfully cached")
        cachedConfigurationHandler.updateProperties()
        return configurationProvider
    }

    private fun parseConfigurationProvider(configurationJson: String?): ConfigurationProvider {
        val configurationParser = Parser(configurationJson)
        return parseAndConstructConfigurationProvider(configurationParser)
    }

    private fun cacheConfiguration(
        configurationLoader: ConfigurationLoader,
        configurationProvider: ConfigurationProvider,
    ) {
        val configurationJson = configurationLoader.configurationJson
        cachedConfigurationHandler.cacheFile(
            CACHED_CONFIG_JSON,
            configurationJson,
        )
        cachedConfigurationHandler.cacheFile(
            CACHED_CONFIG_RSA,
            configurationLoader.configurationSignature,
        )
        cachedConfigurationHandler.updateConfigurationVersionSerial(
            configurationProvider.metaInf.serial,
        )
    }

    private fun parseAndConstructConfigurationProvider(configurationParser: Parser): ConfigurationProvider {
        val metaInf =
            MetaInf(
                url = configurationParser.parseStringValue("META-INF", "URL"),
                date = configurationParser.parseStringValue("META-INF", "DATE"),
                serial = configurationParser.parseIntValue("META-INF", "SERIAL"),
                version = configurationParser.parseIntValue("META-INF", "VER"),
            )
        return ConfigurationProvider(
            metaInf = metaInf,
            configUrl = centralConfigurationServiceUrl,
            sivaUrl = configurationParser.parseStringValue("SIVA-URL"),
            tslUrl = configurationParser.parseStringValue("TSL-URL"),
            tslCerts = configurationParser.parseStringValues("TSL-CERTS"),
            tsaUrl = configurationParser.parseStringValue("TSA-URL"),
            ldapPersonUrl = configurationParser.parseStringValue("LDAP-PERSON-URL"),
            ldapCorpUrl = configurationParser.parseStringValue("LDAP-CORP-URL"),
            midRestUrl = configurationParser.parseStringValue("MID-PROXY-URL"),
            midSkRestUrl = configurationParser.parseStringValue("MID-SK-URL"),
            sidV2RestUrl = configurationParser.parseStringValue("SIDV2-PROXY-URL"),
            sidV2SkRestUrl = configurationParser.parseStringValue("SIDV2-SK-URL"),
            ocspUrls = configurationParser.parseStringValuesToMap("OCSP-URL-ISSUER"),
            certBundle = configurationParser.parseStringValues("CERT-BUNDLE"),
            configurationLastUpdateCheckDate = cachedConfigurationHandler.confLastUpdateCheckDate,
            configurationUpdateDate = cachedConfigurationHandler.confUpdateDate,
        )
    }

    private fun loadCentralConfServiceSSlCertIfPresent(assetManager: AssetManager): X509Certificate? {
        return try {
            val certStream = assetManager.open("certs/test-ca.cer")
            val cf = CertificateFactory.getInstance("X.509")
            cf.generateCertificate(certStream) as X509Certificate
        } catch (e: FileNotFoundException) {
            // No explicit SSL certificate found in assets, using java default cacerts
            null
        } catch (e: IOException) {
            errorLog(LOG_TAG, "Failed to load SSL certificate", e)
            throw IllegalStateException("Failed to load SSL certificate", e)
        } catch (e: CertificateException) {
            errorLog(LOG_TAG, "Failed to load SSL certificate", e)
            throw IllegalStateException("Failed to load SSL certificate", e)
        }
    }

    private fun getLocalizedMessage(
        context: Context,
        @StringRes message: Int,
    ): String {
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(Locale.getDefault())
        return context.createConfigurationContext(configuration)
            .getText(message)
            .toString()
    }
}
