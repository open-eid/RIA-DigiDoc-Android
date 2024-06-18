@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.task

import ee.ria.DigiDoc.common.extensions.removeWhitespaces
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.domain.model.ConfigurationData
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepository
import ee.ria.DigiDoc.configuration.utils.Constant.CENTRAL_CONFIGURATION_SERVICE_URL_PROPERTY
import ee.ria.DigiDoc.configuration.utils.Constant.CENTRAL_CONF_SERVICE_URL_NAME
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_DOWNLOAD_DATE_PROPERTY
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_UPDATE_INTERVAL_PROPERTY
import ee.ria.DigiDoc.configuration.utils.Constant.CONFIGURATION_VERSION_SERIAL_PROPERTY
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIGURATION_PROPERTIES_FILE_NAME
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_JSON
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_PUB
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_CONFIG_RSA
import ee.ria.DigiDoc.configuration.utils.Constant.DEFAULT_UPDATE_INTERVAL
import ee.ria.DigiDoc.configuration.utils.Constant.PROPERTIES_FILE_NAME
import ee.ria.DigiDoc.configuration.utils.Parser
import ee.ria.DigiDoc.network.configuration.interceptors.NetworkInterceptor
import ee.ria.DigiDoc.network.configuration.interceptors.UserAgentInterceptor
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.internal.tls.OkHostnameVerifier
import okhttp3.logging.HttpLoggingInterceptor
import org.bouncycastle.util.encoders.Base64
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Properties
import java.util.concurrent.TimeUnit

object FetchAndPackageDefaultConfigurationTask {
    @Suppress("PropertyName")
    private val LOG_TAG = javaClass.simpleName
    private var properties = Properties()
    private var buildVariant: String? = null
    private val defaultTimeout = 5L

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            loadResourcesProperties()
            loadAndStoreDefaultConfiguration(args)
        }
    }

    @Throws(Exception::class)
    private suspend fun loadAndStoreDefaultConfiguration(args: Array<String>) {
        buildVariant = "main"
        val configurationServiceUrl = determineCentralConfigurationServiceUrl(args)

        val okHttpClient =
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(UserAgentInterceptor("Jenkins"))
                .addInterceptor(NetworkInterceptor())
                .hostnameVerifier(OkHostnameVerifier)
                .connectTimeout(defaultTimeout, TimeUnit.SECONDS)
                .readTimeout(defaultTimeout, TimeUnit.SECONDS)
                .callTimeout(defaultTimeout, TimeUnit.SECONDS)
                .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
                .build()

        val retrofit =
            Retrofit.Builder()
                .baseUrl("$configurationServiceUrl/")
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val centralConfigurationRepository = retrofit.create(CentralConfigurationRepository::class.java)

        val configurationData =
            ConfigurationData(
                centralConfigurationRepository.fetchConfiguration(),
                centralConfigurationRepository.fetchPublicKey(),
                Base64.decode(centralConfigurationRepository.fetchSignature().trim().removeWhitespaces()),
            )

        loadAndAssertConfiguration(configurationData, configurationServiceUrl, args)
    }

    private fun loadAndAssertConfiguration(
        confData: ConfigurationData,
        confServiceUrl: String,
        args: Array<String>,
    ) {
        verifyConfigurationSignature(confData)
        assertConfigurationLoaded(confData)
        storeAsDefaultConfiguration(confData)
        val configurationParser = Parser(confData.configurationJson)
        val confVersionSerial: Int = configurationParser.parseIntValue("META-INF", "SERIAL")
        storeApplicationProperties(
            confServiceUrl,
            confVersionSerial,
            determineConfigurationUpdateInterval(args),
        )
    }

    private fun loadResourcesProperties() {
        properties = Properties()
        try {
            val userDir = System.getProperty("user.dir") ?: throw IllegalStateException("User directory is not set")
            FileInputStream(
                "$userDir/src/main/resources/$DEFAULT_CONFIGURATION_PROPERTIES_FILE_NAME",
            ).use {
                    propsInputStream ->
                properties.load(propsInputStream)
            }
        } catch (e: IOException) {
            throw IllegalStateException(
                "Failed to read '$DEFAULT_CONFIGURATION_PROPERTIES_FILE_NAME' file",
                e,
            )
        }
    }

    private fun determineCentralConfigurationServiceUrl(args: Array<String>): String {
        return if (args.isNotEmpty()) {
            args[0]
        } else {
            properties.getProperty(CENTRAL_CONF_SERVICE_URL_NAME)
        }
    }

    private fun determineConfigurationUpdateInterval(args: Array<String>): Int {
        return try {
            if (args.size > 1) {
                args[1].toInt()
            } else {
                properties.getProperty(CONFIGURATION_UPDATE_INTERVAL_PROPERTY)
                    .toInt()
            }
        } catch (nfe: NumberFormatException) {
            errorLog(LOG_TAG, "Unable to determine configuration update interval", nfe)
            DEFAULT_UPDATE_INTERVAL
        }
    }

    private fun determineConfigurationVersionSerial(args: Array<String>): Int {
        return try {
            if (args.size > 2) {
                args[2].toInt()
            } else {
                properties.getProperty(CONFIGURATION_VERSION_SERIAL_PROPERTY)
                    .toInt()
            }
        } catch (nfe: NumberFormatException) {
            errorLog(LOG_TAG, "Unable to determine configuration version serial", nfe)
            0
        }
    }

    private fun determineConfigurationDownloadDate(args: Array<String>): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

        return if (args.size > 3) {
            LocalDateTime.parse(args[3], formatter)
        } else {
            LocalDateTime.parse(properties.getProperty(CONFIGURATION_DOWNLOAD_DATE_PROPERTY), formatter)
        }
    }

    private fun assertConfigurationLoaded(confData: ConfigurationData) {
        check(
            !(
                confData.configurationJson == null ||
                    confData.configurationSignature == null ||
                    confData.configurationSignaturePublicKey == null
            ),
        ) {
            "Configuration loading has failed"
        }
    }

    private fun storeAsDefaultConfiguration(confData: ConfigurationData) {
        confData.configurationJson?.let { storeFile(DEFAULT_CONFIG_JSON, it) }
        confData.configurationSignature?.let { storeFile(DEFAULT_CONFIG_RSA, it) }
        confData.configurationSignaturePublicKey?.let { storeFile(DEFAULT_CONFIG_PUB, it) }
    }

    private fun storeApplicationProperties(
        configurationServiceUrl: String,
        confVersionSerial: Int,
        configurationUpdateInterval: Int,
    ) {
        val propertiesFileBuilder: StringBuilder =
            StringBuilder()
                .append(CENTRAL_CONFIGURATION_SERVICE_URL_PROPERTY)
                .append("=")
                .append(configurationServiceUrl)
                .append("\n")
                .append(CONFIGURATION_UPDATE_INTERVAL_PROPERTY)
                .append("=")
                .append(configurationUpdateInterval)
                .append("\n")
                .append(CONFIGURATION_VERSION_SERIAL_PROPERTY)
                .append("=")
                .append(confVersionSerial)
                .append("\n")
                .append(CONFIGURATION_DOWNLOAD_DATE_PROPERTY)
                .append("=")
                .append(DateUtil.dateFormat.format(Date()))
        storeFile(PROPERTIES_FILE_NAME, propertiesFileBuilder.toString())
    }

    private fun storeFile(
        filename: String,
        fileContent: String,
    ) {
        FileUtil.storeFile(configFileDir(filename), fileContent)
    }

    @Suppress("SameParameterValue")
    private fun storeFile(
        filename: String,
        fileContent: ByteArray,
    ) {
        FileUtil.storeFile(configFileDir(filename), fileContent)
    }

    private fun configFileDir(filename: String): String {
        val userDir = System.getProperty("user.dir") ?: throw IllegalStateException("User directory is not set")
        val configDir = File("$userDir/src/$buildVariant/assets/config/")
        configDir.mkdirs()
        return File(configDir, filename).path
    }

    private fun verifyConfigurationSignature(configurationData: ConfigurationData) {
        val configurationSignatureVerifier = ConfigurationSignatureVerifierImpl()

        if (configurationData.configurationJson == null ||
            configurationData.configurationSignaturePublicKey == null ||
            configurationData.configurationSignature == null
        ) {
            throw UninitializedPropertyAccessException(
                "Unable to verify signature. Needed configuration property is null",
            )
        } else {
            configurationData.configurationJson.let { conf ->
                configurationData.configurationSignaturePublicKey.let { publicKey ->
                    configurationData.configurationSignature.let { signature ->
                        configurationSignatureVerifier.verifyConfigurationSignature(
                            conf,
                            publicKey,
                            signature,
                        )
                    }
                }
            }
        }
    }
}
