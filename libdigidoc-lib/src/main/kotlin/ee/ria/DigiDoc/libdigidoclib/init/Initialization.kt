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
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Resources.NotFoundException
import android.system.ErrnoException
import android.text.TextUtils
import android.widget.Toast
import androidx.preference.PreferenceManager
import ee.ria.DigiDoc.common.Constant.DIR_SIVA_CERT
import ee.ria.DigiDoc.common.Constant.DIR_TSA_CERT
import ee.ria.DigiDoc.common.preferences.EncryptedPreferences
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.libdigidoclib.R
import ee.ria.DigiDoc.libdigidoclib.exceptions.AlreadyInitializedException
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.getSchemaDir
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.getSchemaPath
import ee.ria.DigiDoc.libdigidoclib.utils.FileUtils.initSchema
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxyConfig
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.utils.ProxyUtil
import ee.ria.DigiDoc.network.utils.ProxyUtil.getManualProxySettings
import ee.ria.DigiDoc.network.utils.ProxyUtil.getProxySetting
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import ee.ria.DigiDoc.utilsLib.extensions.removeWhitespaces
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil.getCertFile
import ee.ria.DigiDoc.utilsLib.file.FileUtil.readFileContent
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.libdigidocpp.Conf
import ee.ria.libdigidocpp.DigiDocConf
import ee.ria.libdigidocpp.digidoc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Base64
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

        private var tsaUrlChangeListener: OnSharedPreferenceChangeListener? = null
        private var tsCertChangeListener: OnSharedPreferenceChangeListener? = null
        private var sivaUrlChangeListener: OnSharedPreferenceChangeListener? = null
        private var sivaCertChangeListener: OnSharedPreferenceChangeListener? = null

        /**
         * Initialize libdigidoc-lib.
         *
         * Unzips the schema, access certificate and initializes libdigidocpp.
         */
        @Throws(IOException::class, NotFoundException::class)
        suspend fun init(
            context: Context,
            isLoggingEnabled: Boolean = false,
        ) {
            if (isInitialized) {
                setLibdigidocppLogLevel(isLoggingEnabled)
                throw AlreadyInitializedException("Libdigidocpp is already initialized")
            }

            withContext(IO) {
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
                    isLoggingEnabled,
                )
            }
        }

        private fun initLibDigiDocpp(
            context: Context,
            path: String,
            isLoggingEnabled: Boolean,
        ) {
            initLibDigiDocConfiguration(
                context,
                isLoggingEnabled,
            )
            digidoc.initializeLib(UserAgentUtil.getAppInfo(context), path)
            UserAgentUtil.setLibdigidocppVersion(digidoc.version())
            isInitialized = true
        }

        private fun initLibDigiDocConfiguration(
            context: Context,
            isLoggingEnabled: Boolean,
        ) {
            val conf = DigiDocConf(getSchemaDir(context).absolutePath)
            Conf.init(conf.transfer())
            initLibDigiDocLogging(context, isLoggingEnabled)

            forcePKCS12Certificate()

            val proxySetting: ProxySetting? = getProxySetting(context)
            val manualProxy: ManualProxy = getManualProxySettings(context)
            val proxyConfig: ProxyConfig = ProxyUtil.getProxy(proxySetting, manualProxy)
            val proxySettings = proxyConfig.manualProxy()

            val proxyHostPreferenceKey =
                context.resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_host_key)
            val proxyPortPreferenceKey =
                context.resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_port_key)
            val proxyUsernamePreferenceKey =
                context.resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_username_key)
            val proxyPasswordPreferenceKey =
                context.resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_password_key)

            if (proxySetting == ProxySetting.SYSTEM_PROXY) {
                if (proxySettings != null) {
                    overrideProxy(
                        proxySettings.host,
                        proxySettings.port,
                        proxySettings.username,
                        proxySettings.password,
                    )
                } else {
                    overrideProxy("", 80, "", "")
                }
            } else {
                initProxy(
                    context,
                    proxyHostPreferenceKey,
                    "",
                    proxyPortPreferenceKey,
                    80,
                    proxyUsernamePreferenceKey,
                    "",
                    proxyPasswordPreferenceKey,
                    "",
                )
            }

            loadConfiguration(context)
        }

        private fun overrideConfiguration(
            context: Context,
            configurationProvider: ConfigurationProvider,
        ) {
            overrideTSLUrl(configurationProvider.tslUrl)
            overrideTSLCert(configurationProvider.tslCerts)
            overrideSivaUrl(configurationProvider.sivaUrl)
            overrideTSCerts(configurationProvider.certBundle)
            overrideTSUrl(configurationProvider.tsaUrl)
            overrideVerifyServiceCert(configurationProvider.certBundle)

            certBundle = configurationProvider.certBundle
            val tsaUrlPreferenceKey =
                context.resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_url_key)
            val tsaCertPreferenceKey =
                context.resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_cert_key)
            val sivaUrlPreferenceKey =
                context.resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_siva_url_key)
            val sivaCertPreferenceKey =
                context.resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_siva_cert_key)

            initTsaUrl(
                context,
                tsaUrlPreferenceKey,
                configurationProvider.tsaUrl,
            )
            initTsCert(
                context,
                tsaCertPreferenceKey,
                "",
                tsaUrlPreferenceKey,
                configurationProvider.tsaUrl,
            )
            initSivaUrl(
                context,
                sivaUrlPreferenceKey,
                configurationProvider.sivaUrl,
            )
            initSivaCert(
                context,
                sivaCertPreferenceKey,
                configurationProvider.certBundle,
            )
        }

        private fun forcePKCS12Certificate() {
            DigiDocConf.instance().setPKCS12Cert("798.p12")
        }

        private fun initLibDigiDocLogging(
            context: Context,
            isLoggingEnabled: Boolean,
        ) {
            val logDirectory = FileUtil.getLogsDirectory(context)
            if (!logDirectory.exists()) {
                val isDirCreated = logDirectory.mkdir()
                if (isDirCreated) {
                    debugLog(libdigidocInitLogTag, "Directories created or already exist for ${logDirectory.path}")
                }
            }
            setLibdigidocppLogLevel(isLoggingEnabled)
            DigiDocConf
                .instance()
                .setLogFile(File(logDirectory, "libdigidocpp.log").absolutePath)
        }

        private fun setLibdigidocppLogLevel(isLoggingEnabled: Boolean) {
            DigiDocConf.instance().setLogLevel(if (isLoggingEnabled) libdigidocLogLevel else 0)
        }

        @Suppress("SameParameterValue")
        private fun initProxy(
            context: Context,
            hostPreferenceKey: String,
            hostDefaultValue: String,
            portPreferenceKey: String,
            portDefaultValue: Int,
            usernamePreferenceKey: String,
            usernameDefaultValue: String,
            passwordPreferenceKey: String,
            passwordDefaultValue: String,
        ) {
            try {
                val sharedPreferences: SharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context)
                overrideProxy(
                    sharedPreferences.getString(hostPreferenceKey, hostDefaultValue),
                    sharedPreferences.getInt(portPreferenceKey, portDefaultValue),
                    sharedPreferences.getString(usernamePreferenceKey, usernameDefaultValue),
                    EncryptedPreferences.getString(context, passwordPreferenceKey, passwordDefaultValue),
                )
            } catch (e: IllegalStateException) {
                errorLog(libdigidocInitLogTag, "Error initializing proxy", e)
                throw RuntimeException(e)
            }
        }

        private fun initTsaUrl(
            context: Context,
            preferenceKey: String,
            defaultValue: String,
        ) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (tsaUrlChangeListener != null) {
                preferences.unregisterOnSharedPreferenceChangeListener(tsaUrlChangeListener)
            }
            tsaUrlChangeListener =
                TsaUrlChangeListener(preferenceKey, defaultValue)
            preferences.registerOnSharedPreferenceChangeListener(tsaUrlChangeListener)
            (tsaUrlChangeListener as TsaUrlChangeListener).onSharedPreferenceChanged(
                preferences,
                preferenceKey,
            )
        }

        @Suppress("SameParameterValue")
        private fun initTsCert(
            context: Context,
            preferenceKey: String,
            defaultValue: String,
            tsaUrlPreferenceKey: String,
            defaultTsaUrl: String,
        ) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)

            if (tsCertChangeListener != null) {
                preferences.unregisterOnSharedPreferenceChangeListener(tsCertChangeListener)
            }

            tsCertChangeListener =
                TsCertChangeListener(
                    context,
                    preferenceKey,
                    defaultValue,
                    tsaUrlPreferenceKey,
                    defaultTsaUrl,
                )
            preferences.registerOnSharedPreferenceChangeListener(tsCertChangeListener)
            (tsCertChangeListener as TsCertChangeListener).onSharedPreferenceChanged(
                preferences,
                preferenceKey,
            )
        }

        private fun initSivaUrl(
            context: Context,
            preferenceKey: String,
            defaultValue: String,
        ) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (sivaUrlChangeListener != null) {
                preferences.unregisterOnSharedPreferenceChangeListener(sivaUrlChangeListener)
            }
            sivaUrlChangeListener =
                SivaUrlChangeListener(preferenceKey, defaultValue)
            preferences.registerOnSharedPreferenceChangeListener(sivaUrlChangeListener)
            (sivaUrlChangeListener as SivaUrlChangeListener).onSharedPreferenceChanged(
                preferences,
                preferenceKey,
            )
        }

        private fun initSivaCert(
            context: Context,
            preferenceKey: String,
            certBundle: List<String>,
        ) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            if (sivaCertChangeListener != null) {
                preferences.unregisterOnSharedPreferenceChangeListener(sivaCertChangeListener)
            }

            sivaCertChangeListener =
                SivaCertChangeListener(context, preferenceKey, certBundle)
            preferences.registerOnSharedPreferenceChangeListener(sivaCertChangeListener)
            (sivaCertChangeListener as SivaCertChangeListener).onSharedPreferenceChanged(
                preferences,
                preferenceKey,
            )
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
                DigiDocConf.instance().addTSCert(Base64.getDecoder().decode(tsCert.removeWhitespaces().trim()))
            }
        }

        private fun overrideTSLUrl(tslUrl: String) {
            DigiDocConf.instance().setTSLUrl(tslUrl)
        }

        private fun overrideTSLCert(tslCerts: List<String>) {
            DigiDocConf.instance().setTSLCert(ByteArray(0)) // Clear existing TSL certificates list
            for (tslCert in tslCerts) {
                DigiDocConf.instance().addTSLCert(Base64.getDecoder().decode(tslCert.removeWhitespaces().trim()))
            }
        }

        private fun overrideVerifyServiceCert(certBundle: List<String>) {
            DigiDocConf.instance().setVerifyServiceCert(ByteArray(0))
            for (cert in certBundle) {
                DigiDocConf.instance().addVerifyServiceCert(Base64.getDecoder().decode(cert.removeWhitespaces().trim()))
            }
        }

        private fun loadConfiguration(context: Context) {
            configurationRepository.getConfiguration()?.let { overrideConfiguration(context, it) }
            CoroutineScope(Main).launch {
                configurationRepository.observeConfigurationUpdates { newConfig ->
                    overrideConfiguration(context, newConfig)
                }
            }
        }

        private class TsaUrlChangeListener(
            private val preferenceKey: String,
            private val defaultValue: String,
        ) : OnSharedPreferenceChangeListener {
            override fun onSharedPreferenceChanged(
                sharedPreferences: SharedPreferences,
                key: String?,
            ) {
                if (TextUtils.equals(key, preferenceKey)) {
                    val value = sharedPreferences.getString(key, defaultValue)
                    if (value != null) {
                        DigiDocConf.instance().setTSUrl(value.ifEmpty { defaultValue })
                    }
                }
            }
        }

        private class TsCertChangeListener(
            private val context: Context,
            private val preferenceKey: String,
            private val defaultValue: String,
            private val tsaUrlPreferenceKey: String,
            private val defaultTsaUrl: String,
        ) : OnSharedPreferenceChangeListener {
            override fun onSharedPreferenceChanged(
                sharedPreferences: SharedPreferences,
                key: String?,
            ) {
                if (TextUtils.equals(key, preferenceKey)) {
                    if (sharedPreferences.getString(
                            tsaUrlPreferenceKey,
                            defaultTsaUrl,
                        ) == defaultTsaUrl
                    ) {
                        overrideTSCerts(certBundle, null)
                    } else {
                        overrideTSCerts(
                            certBundle,
                            sharedPreferences.getString(key, defaultValue)?.let {
                                getCustomCertFile(
                                    context,
                                    it,
                                    DIR_TSA_CERT,
                                )
                            },
                        )
                    }
                }
            }
        }

        private class SivaUrlChangeListener(
            private val preferenceKey: String,
            private val defaultValue: String,
        ) : OnSharedPreferenceChangeListener {
            override fun onSharedPreferenceChanged(
                sharedPreferences: SharedPreferences,
                key: String?,
            ) {
                if (TextUtils.equals(key, preferenceKey)) {
                    val value = sharedPreferences.getString(key, defaultValue)
                    if (value != null) {
                        DigiDocConf
                            .instance()
                            .setVerifyServiceUri(value.ifEmpty { defaultValue })
                    }
                }
            }
        }

        private class SivaCertChangeListener(
            private val context: Context,
            private val preferenceKey: String,
            private val defaultValues: List<String>,
        ) : OnSharedPreferenceChangeListener {
            override fun onSharedPreferenceChanged(
                sharedPreferences: SharedPreferences,
                key: String?,
            ) {
                if (TextUtils.equals(key, preferenceKey)) {
                    overrideVerifyServiceCert(
                        context,
                        defaultValues,
                        sharedPreferences.getString(key, "")?.let {
                            getCustomCertFile(
                                context,
                                it,
                                DIR_SIVA_CERT,
                            )
                        },
                    )
                }
            }
        }

        companion object {
            private var certBundle: List<String> = ArrayList()

            private fun getCustomCertFile(
                context: Context,
                fileName: String,
                certFolder: String,
            ): String? {
                val certFile: File? = getCertFile(context, fileName, certFolder)
                if (certFile != null) {
                    return try {
                        readFileContent(certFile.path)
                            .replace("-----BEGIN CERTIFICATE-----", "")
                            .replace("-----END CERTIFICATE-----", "")
                            .replace("\\s".toRegex(), "")
                    } catch (_: IllegalStateException) {
                        null
                    }
                }
                return null
            }

            private fun overrideTSCerts(
                certBundle: List<String>,
                customTsCert: String?,
            ) {
                DigiDocConf.instance().setTSCert(ByteArray(0)) // Clear existing TS certificates list
                for (tsCert in certBundle) {
                    DigiDocConf.instance().addTSCert(
                        Base64.getDecoder().decode(tsCert.removeWhitespaces().trim()),
                    )
                }

                if (customTsCert != null) {
                    DigiDocConf.instance().addTSCert(
                        Base64.getDecoder().decode(customTsCert.removeWhitespaces().trim()),
                    )
                }
            }

            private fun overrideVerifyServiceCert(
                context: Context,
                certBundle: List<String>,
                customSivaCert: String?,
            ) {
                try {
                    DigiDocConf.instance().setVerifyServiceCert(ByteArray(0))
                    if (!customSivaCert.isNullOrEmpty()) {
                        DigiDocConf.instance().addVerifyServiceCert(
                            Base64.getDecoder().decode(customSivaCert.removeWhitespaces().trim()),
                        )
                    }
                    for (cert in certBundle) {
                        DigiDocConf.instance().addVerifyServiceCert(
                            Base64.getDecoder().decode(cert.removeWhitespaces().trim()),
                        )
                    }
                } catch (e: Exception) {
                    errorLog("Libdigidoc-Initialization", "Error adding custom SiVa certificate", e)
                    CoroutineScope(Main).launch {
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.libdigidocpp_custom_siva_cert_error),
                                Toast.LENGTH_LONG,
                            ).show()
                    }
                }
            }
        }
    }
