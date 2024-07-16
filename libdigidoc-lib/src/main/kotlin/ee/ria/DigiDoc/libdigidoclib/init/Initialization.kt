@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.init

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.system.ErrnoException
import android.system.Os
import android.util.Base64
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.libdigidoclib.BuildConfig
import ee.ria.DigiDoc.libdigidoclib.exceptions.AlreadyInitializedException
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.getSchemaDir
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.getSchemaPath
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.initSchema
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.libdigidocpp.Conf
import ee.ria.libdigidocpp.DigiDocConf
import ee.ria.libdigidocpp.StringMap
import ee.ria.libdigidocpp.digidoc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Initialization
    @Inject
    constructor(
        private val configurationRepository: ConfigurationRepository,
    ) {
        private var isInitialized = false
        private val libdigidocInitLogTag = "Libdigidoc-Initialization"
        private val libdigidocLogLevel = 4 // 4 - Debug messages

        /**
         * Initialize libdigidoc-lib.
         *
         * Unzips the schema, access certificate and initializes libdigidocpp.
         */
        @Throws(IOException::class, NotFoundException::class)
        suspend fun init(context: Context) {
            if (isInitialized) {
                throw AlreadyInitializedException("Libdigidocpp is already initialized")
            }
            coroutineScope {
                launch(IO) {
                    initHomeDir(context)
                    initNativeLibs()
                    try {
                        initSchema(context)
                    } catch (ioe: IOException) {
                        errorLog(libdigidocInitLogTag, "Init schema failed: ${ioe.message}")
                        throw ioe
                    } catch (nfe: NotFoundException) {
                        errorLog(libdigidocInitLogTag, "Init schema failed: ${nfe.message}")
                        throw nfe
                    } catch (erre: ErrnoException) {
                        errorLog(libdigidocInitLogTag, "Init schema failed: ${erre.message}")
                        throw erre
                    }

                    initLibDigiDocpp(
                        context,
                        getSchemaPath(context),
                    )
                }
            }
        }

        @Throws(ErrnoException::class)
        private fun initHomeDir(context: Context) {
            val path: String = getSchemaPath(context)
            try {
                Os.setenv("HOME", path, true)
            } catch (erre: ErrnoException) {
                errorLog(
                    libdigidocInitLogTag,
                    "Setting HOME environment variable failed: ${erre.message}",
                )
                throw erre
            }
        }

        private fun initNativeLibs() {
            System.loadLibrary("digidoc_java")
        }

        private suspend fun initLibDigiDocpp(
            context: Context,
            path: String,
        ) {
            initLibDigiDocConfiguration(
                context,
            )
            digidoc.initializeLib(UserAgentUtil.getUserAgent(context), path)
            isInitialized = true
        }

        private suspend fun initLibDigiDocConfiguration(context: Context) {
            val conf = DigiDocConf(getSchemaDir(context).absolutePath)
            Conf.init(conf.transfer())
            if (BuildConfig.BUILD_TYPE.contentEquals("debug")) {
                initLibDigiDocLogging(context)
            }

            forcePKCS12Certificate()

            loadConfiguration()
        }

        private fun overrideConfiguration(configurationProvider: ConfigurationProvider) {
            overrideTSLUrl(configurationProvider.tslUrl)
            overrideTSLCert(configurationProvider.tslCerts)
            overrideSivaUrl(configurationProvider.sivaUrl)
            overrideOCSPUrls(configurationProvider.ocspUrls)
            overrideTSCerts(configurationProvider.certBundle)
            overrideTSUrl(configurationProvider.tsaUrl)
            overrideVerifyServiceCert(configurationProvider.certBundle)
        }

        private fun forcePKCS12Certificate() {
            DigiDocConf.instance().setPKCS12Cert("798.p12")
        }

        private fun initLibDigiDocLogging(context: Context) {
            val logDirectory = File(context.filesDir, "/logs")
            if (!logDirectory.exists()) {
                val isDirCreated = logDirectory.mkdir()
                if (isDirCreated) {
                    debugLog(libdigidocInitLogTag, "Directories created for ${logDirectory.path}")
                }
            }
            DigiDocConf.instance().setLogLevel(libdigidocLogLevel)
            DigiDocConf.instance()
                .setLogFile(File(logDirectory, "libdigidocpp.log").absolutePath)
        }

        fun overrideProxy(
            host: String?,
            port: Int,
            username: String?,
            password: String?,
        ) {
            if (Optional.ofNullable(host).isPresent) {
                DigiDocConf.instance().setProxyHost(host)
            }
            DigiDocConf.instance().setProxyPort(if (port != 0) port.toString() else "80")
            if (Optional.ofNullable(username).isPresent) {
                DigiDocConf.instance().setProxyUser(username)
            }
            if (Optional.ofNullable(password).isPresent) {
                DigiDocConf.instance().setProxyPass(password)
            }
        }

        private fun overrideTSUrl(tsUrl: String) {
            DigiDocConf.instance().setTSUrl(tsUrl)
        }

        private fun overrideSivaUrl(sivaUrl: String) {
            DigiDocConf.instance().setVerifyServiceUri(sivaUrl)
        }

        private fun overrideTSCerts(certBundle: List<String>) {
            DigiDocConf.instance().setTSCert(ByteArray(0)) // Clear existing TS certificates list
            for (tsCert in certBundle) {
                DigiDocConf.instance().addTSCert(Base64.decode(tsCert, Base64.DEFAULT))
            }
        }

        private fun overrideTSLUrl(tslUrl: String) {
            DigiDocConf.instance().setTSLUrl(tslUrl)
        }

        private fun overrideTSLCert(tslCerts: List<String>) {
            DigiDocConf.instance().setTSLCert(ByteArray(0)) // Clear existing TSL certificates list
            for (tslCert in tslCerts) {
                DigiDocConf.instance().addTSLCert(Base64.decode(tslCert, Base64.DEFAULT))
            }
        }

        private fun overrideVerifyServiceCert(certBundle: List<String>) {
            DigiDocConf.instance().setVerifyServiceCert(ByteArray(0))
            for (cert in certBundle) {
                DigiDocConf.instance().addVerifyServiceCert(Base64.decode(cert, Base64.DEFAULT))
            }
        }

        private fun overrideOCSPUrls(ocspUrls: Map<String, String>) {
            val stringMap = StringMap()
            for ((key, value) in ocspUrls) {
                stringMap[key] = value
            }
            DigiDocConf.instance().setOCSPUrls(stringMap)
        }

        private suspend fun loadConfiguration() {
            configurationRepository.getConfiguration()?.let { overrideConfiguration(it) }
            CoroutineScope(IO).launch {
                configurationRepository.observeConfigurationUpdates { newConfig ->
                    overrideConfiguration(newConfig)
                }
            }
        }
    }
