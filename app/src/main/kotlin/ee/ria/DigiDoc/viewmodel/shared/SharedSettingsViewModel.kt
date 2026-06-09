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

package ee.ria.DigiDoc.viewmodel.shared

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.DIR_CRYPTO_CERT
import ee.ria.DigiDoc.common.Constant.DIR_SIVA_CERT
import ee.ria.DigiDoc.common.Constant.DIR_TSA_CERT
import ee.ria.DigiDoc.common.Constant.Defaults.DEFAULT_UUID_VALUE
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.domain.model.settings.CDOCSetting
import ee.ria.DigiDoc.domain.model.settings.TSASetting
import ee.ria.DigiDoc.domain.model.settings.UUIDSetting
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.manager.ActivityManager
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxyConfig
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.siva.SivaSetting
import ee.ria.DigiDoc.network.utils.NetworkUtil.constructClientBuilder
import ee.ria.DigiDoc.network.utils.ProxyUtil
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.signing.CertificateUtil
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.FileUtils
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SharedSettingsViewModel
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val contentResolver: ContentResolver,
        val dataStore: DataStore,
        private val initialization: Initialization,
        private val configurationRepository: ConfigurationRepository,
        private val activityManager: ActivityManager,
    ) : ViewModel() {
        private val logTag = "SharedSettingsViewModel"

        private val _updatedConfiguration = MutableLiveData<ConfigurationProvider?>()
        val updatedConfiguration: LiveData<ConfigurationProvider?> = _updatedConfiguration

        private val _sivaIssuedTo = MutableStateFlow<String?>(null)
        val sivaIssuedTo: StateFlow<String?> = _sivaIssuedTo

        private val _sivaValidTo = MutableStateFlow<String?>(null)
        val sivaValidTo: StateFlow<String?> = _sivaValidTo

        private val _tsaIssuedTo = MutableStateFlow<String?>(null)
        val tsaIssuedTo: StateFlow<String?> = _tsaIssuedTo

        private val _tsaValidTo = MutableStateFlow<String?>(null)
        val tsaValidTo: StateFlow<String?> = _tsaValidTo

        private val _cryptoCertIssuedTo = MutableStateFlow<String?>(null)
        val cryptoCertIssuedTo: StateFlow<String?> = _cryptoCertIssuedTo

        private val _cryptoCertValidTo = MutableStateFlow<String?>(null)
        val cryptoCertValidTo: StateFlow<String?> = _cryptoCertValidTo

        private val _previousSivaUrl = MutableStateFlow<String?>(null)
        val previousSivaUrl: StateFlow<String?> = _previousSivaUrl

        private val _previousTsaUrl = MutableStateFlow<String?>(null)
        val previousTsaUrl: StateFlow<String?> = _previousTsaUrl

        private val _sivaCertificate = MutableStateFlow<X509Certificate?>(null)
        val sivaCertificate: StateFlow<X509Certificate?> = _sivaCertificate

        private val _tsaCertificate = MutableStateFlow<X509Certificate?>(null)
        val tsaCertificate: StateFlow<X509Certificate?> = _tsaCertificate

        private val _cryptoCertificate = MutableStateFlow<X509Certificate?>(null)
        val cryptoCertificate: StateFlow<X509Certificate?> = _cryptoCertificate

        private val _errorState = MutableStateFlow<Int?>(null)
        val errorState: StateFlow<Int?> = _errorState

        private val defaultManualProxySettings = ManualProxy("", 80, "", "")

        private val _allowScreenshots = MutableStateFlow(dataStore.getSettingsAllowScreenshots())
        val allowScreenshots = _allowScreenshots.asStateFlow()

        private val _enableRoleAsking = MutableStateFlow(dataStore.getSettingsAskRoleAndAddress())
        val enableRoleAsking = _enableRoleAsking.asStateFlow()

        private val _enableOpenAllFileTypes = MutableStateFlow(dataStore.getSettingsOpenAllFileTypes())
        val enableOpenAllFileTypes = _enableOpenAllFileTypes.asStateFlow()

        init {
            viewModelScope.launch(Main) {
                configurationRepository.observeConfigurationUpdates { newConfig ->
                    _updatedConfiguration.value = newConfig
                }
            }
        }

        fun setSettingsAskRoleAndAddress(value: Boolean) {
            dataStore.setSettingsAskRoleAndAddress(value)
            _enableRoleAsking.value = value
        }

        fun setAllowScreenshots(value: Boolean) {
            dataStore.setSettingsAllowScreenshots(value)
            _allowScreenshots.value = value
        }

        fun setSettingsOpenAllFileTypes(value: Boolean) {
            dataStore.setSettingsOpenAllFileTypes(value)
            _enableOpenAllFileTypes.value = value
        }

        fun resetToDefaultSettings() {
            resetSigningSettings()
            resetRightsSettings()
            resetSivaSettings()
            resetProxySettings()
            resetCryptoSettings()

            resetCertificateInfo()
            resetErrorState()

            resetShowingWrongCanNumberDialog()
        }

        private fun resetProxySettings() {
            dataStore.setProxySetting(ProxySetting.NO_PROXY)
            clearProxySettings()
        }

        private fun clearProxySettings() {
            setManualProxySettings(defaultManualProxySettings)
        }

        private fun resetShowingWrongCanNumberDialog() {
            dataStore.setDoNotShowWrongCanDialog(false)
        }

        private fun setManualProxySettings(manualProxy: ManualProxy) {
            dataStore.setProxyHost(manualProxy.host)
            dataStore.setProxyPort(manualProxy.port)
            dataStore.setProxyUsername(manualProxy.username)
            dataStore.setProxyPassword(manualProxy.password)
            overrideLibdigidocppProxy(manualProxy)
        }

        private fun overrideLibdigidocppProxy(manualProxy: ManualProxy) {
            initialization.overrideProxy(
                manualProxy.host,
                manualProxy.port,
                manualProxy.username,
                manualProxy.password,
            )
        }

        private fun resetCryptoSettings() {
            dataStore.setCdocSetting(CDOCSetting.CDOC1)
            dataStore.setUseOnlineEncryption(false)
            dataStore.setCDOC2SelectedService(DEFAULT_UUID_VALUE)
            dataStore.setCDOC2UUID("00000000-0000-0000-0000-000000000002")
            dataStore.setCDOC2FetchURL("https://cdoc2-keyserver-get")
            dataStore.setCDOC2PostURL("https://cdoc2-keyserver-post")
            removeCryptoCert()
        }

        private fun resetSivaSettings() {
            dataStore.setSivaSetting(SivaSetting.DEFAULT)
            dataStore.setSettingsSivaUrl("")
            dataStore.setSettingsSivaCertName(null)
            removeSivaCert()
        }

        private fun resetRightsSettings() {
            setSettingsOpenAllFileTypes(true)
            setAllowScreenshots(false)
        }

        private fun resetSigningSettings() {
            dataStore.setUuidSetting(UUIDSetting.DEFAULT)
            dataStore.setTsaSetting(TSASetting.DEFAULT)
            dataStore.setSettingsUUID(DEFAULT_UUID_VALUE)
            dataStore.setSettingsTSAUrl(updatedConfiguration.value?.tsaUrl ?: "")
            setSettingsAskRoleAndAddress(false)
            dataStore.setSettingsDefaultLTA(false)
            dataStore.setIsTsaCertificateViewVisible(false)
            val certFile =
                FileUtil.getCertFile(context, dataStore.getTSACertName(), DIR_TSA_CERT)
            removeCertificate(certFile)
        }

        private fun removeCertificate(tsaFile: File?) {
            if (tsaFile != null) {
                FileUtil.removeFile(tsaFile.path)
            }
            dataStore.setTSACertName(null)
        }

        private fun removeSivaCert() {
            val sivaCertName = dataStore.getSettingsSivaCertName()
            val sivaFile = FileUtil.getCertFile(context, sivaCertName, DIR_SIVA_CERT)

            if (sivaFile != null) {
                FileUtil.removeFile(sivaFile.path)
            }
            dataStore.setSettingsSivaCertName(null)
        }

        private fun removeTsaCert() {
            val tsaCertName = dataStore.getTSACertName()
            val tsaFile = FileUtil.getCertFile(context, tsaCertName, DIR_TSA_CERT)

            if (tsaFile != null) {
                FileUtil.removeFile(tsaFile.path)
            }
            dataStore.setTSACertName(null)
        }

        private fun removeCryptoCert() {
            val cryptoCertName = dataStore.getCryptoCertName()
            val cryptoCertFile = FileUtil.getCertFile(context, cryptoCertName, DIR_CRYPTO_CERT)

            if (cryptoCertFile != null) {
                FileUtil.removeFile(cryptoCertFile.path)
            }
            dataStore.setCryptoCertName(null)
        }

        fun saveProxySettings(manualProxySettings: ManualProxy = defaultManualProxySettings) {
            when (dataStore.getProxySetting()) {
                ProxySetting.MANUAL_PROXY -> setManualProxySettings(manualProxySettings)
                ProxySetting.SYSTEM_PROXY -> {
                    val systemSettings: ProxyConfig =
                        ProxyUtil.getProxy(ProxySetting.SYSTEM_PROXY, defaultManualProxySettings)
                    overrideLibdigidocppProxy(systemSettings.manualProxy() ?: defaultManualProxySettings)
                }
                ProxySetting.NO_PROXY -> overrideLibdigidocppProxy(defaultManualProxySettings)
            }
        }

        private fun getFormattedDateTime(date: Date?): String {
            try {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                if (date != null) {
                    return dateFormat.format(date)
                }
            } catch (e: java.lang.IllegalStateException) {
                debugLog(logTag, "Unable to format date", e)
            }
            return "-"
        }

        private fun getSubject(certificateHolder: X509CertificateHolder): String {
            val organizationRDNs = certificateHolder.subject.getRDNs(BCStyle.O)
            val organizationUnitRDNs = certificateHolder.subject.getRDNs(BCStyle.OU)
            val commonNameRDNs = certificateHolder.subject.getRDNs(BCStyle.CN)

            if (commonNameRDNs.isNotEmpty()) {
                return commonNameRDNs[0].first.value.toString()
            }
            if (organizationRDNs.isNotEmpty()) {
                return organizationRDNs[0].first.value.toString()
            }
            if (organizationUnitRDNs.isNotEmpty()) {
                return organizationUnitRDNs[0].first.value.toString()
            }

            return "-"
        }

        fun updateSivaData(
            sivaServiceUrl: String,
            context: Context,
        ) {
            _previousSivaUrl.value = sivaServiceUrl

            val sivaCertName: String = dataStore.getSettingsSivaCertName()
            val sivaFile = FileUtil.getCertFile(context, sivaCertName, DIR_SIVA_CERT)

            if (sivaFile != null) {
                val fileContents: String = FileUtil.readFileContent(sivaFile.path)
                try {
                    val sivaCert = CertificateUtil.x509Certificate(fileContents)
                    _sivaCertificate.value = sivaCert
                    val certificateHolder: X509CertificateHolder = JcaX509CertificateHolder(sivaCert)
                    val issuer: String = getSubject(certificateHolder)
                    _sivaIssuedTo.value = issuer
                    val notAfter: Date = certificateHolder.notAfter
                    if (notAfter.before(Date())) {
                        val expiredText = context.getString(R.string.main_settings_siva_certificate_expired)
                        _sivaValidTo.value = "${getFormattedDateTime(notAfter)} ($expiredText)"
                    } else {
                        _sivaValidTo.value = getFormattedDateTime(notAfter)
                    }
                } catch (e: CertificateException) {
                    errorLog(logTag, "Unable to get SiVa certificate", e)

                    // Remove invalid files
                    removeSivaCert()
                    resetCertificateInfo()
                }
            } else {
                _sivaIssuedTo.value = null
                _sivaValidTo.value = null
                _sivaCertificate.value = null
            }
        }

        fun updateTsaData(
            tsaServiceUrl: String,
            context: Context,
        ) {
            _previousTsaUrl.value = tsaServiceUrl

            val tsaCertName: String = dataStore.getTSACertName()
            val tsaFile = FileUtil.getCertFile(context, tsaCertName, DIR_TSA_CERT)

            if (tsaFile != null) {
                val fileContents: String = FileUtil.readFileContent(tsaFile.path)
                try {
                    val tsaCert = CertificateUtil.x509Certificate(fileContents)
                    _tsaCertificate.value = tsaCert
                    val certificateHolder: X509CertificateHolder = JcaX509CertificateHolder(tsaCert)
                    val issuer: String = getSubject(certificateHolder)
                    _tsaIssuedTo.value = issuer
                    val notAfter: Date = certificateHolder.notAfter
                    if (notAfter.before(Date())) {
                        val expiredText = context.getString(R.string.main_settings_siva_certificate_expired)
                        _tsaValidTo.value = "${getFormattedDateTime(notAfter)} ($expiredText)"
                    } else {
                        _tsaValidTo.value = getFormattedDateTime(notAfter)
                    }
                } catch (e: CertificateException) {
                    errorLog(logTag, "Unable to get TSA certificate", e)

                    // Remove invalid files
                    removeTsaCert()
                    resetCertificateInfo()
                }
            } else {
                _tsaIssuedTo.value = null
                _tsaValidTo.value = null
                _tsaCertificate.value = null
            }
        }

        fun updateCryptoCertData(context: Context) {
            val cryptoCertName: String = dataStore.getCryptoCertName()
            val cryptoCertFile = FileUtil.getCertFile(context, cryptoCertName, DIR_CRYPTO_CERT)

            if (cryptoCertFile != null) {
                val fileContents: String = FileUtil.readFileContent(cryptoCertFile.path)
                try {
                    val cryptoCert = CertificateUtil.x509Certificate(fileContents)
                    _cryptoCertificate.value = cryptoCert
                    val certificateHolder: X509CertificateHolder = JcaX509CertificateHolder(cryptoCert)
                    val issuer: String = getSubject(certificateHolder)
                    _cryptoCertIssuedTo.value = issuer
                    val notAfter: Date = certificateHolder.notAfter
                    if (notAfter.before(Date())) {
                        val expiredText = context.getString(R.string.main_settings_siva_certificate_expired)
                        _cryptoCertValidTo.value = "${getFormattedDateTime(notAfter)} ($expiredText)"
                    } else {
                        _cryptoCertValidTo.value = getFormattedDateTime(notAfter)
                    }
                } catch (e: CertificateException) {
                    errorLog(logTag, "Unable to get Crypto certificate", e)

                    // Remove invalid files
                    removeCryptoCert()
                    resetCertificateInfo()
                }
            } else {
                _cryptoCertIssuedTo.value = null
                _cryptoCertValidTo.value = null
                _cryptoCertificate.value = null
            }
        }

        fun handleSivaFile(uri: Uri) {
            try {
                val initialStream: InputStream? =
                    contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Unable to open input stream for SiVa certificate URI")
                val sivaCertFolder = File(context.filesDir, DIR_SIVA_CERT)
                if (!sivaCertFolder.exists()) {
                    val isFolderCreated = sivaCertFolder.mkdirs()
                    debugLog(logTag, String.format("SiVa cert folder created: %s", isFolderCreated))
                }
                val fileName =
                    DocumentFile
                        .fromSingleUri(context, uri)
                        ?.name
                        .takeUnless { it.isNullOrEmpty() } ?: "sivaCert"
                val sivaFile = File(sivaCertFolder, fileName)
                FileUtils.copyInputStreamToFile(initialStream, sivaFile)
                dataStore.setSettingsSivaCertName(sivaFile.name)
            } catch (e: Exception) {
                errorLog(logTag, "Unable to read SiVa certificate file data", e)
            }
        }

        fun handleTsaFile(uri: Uri) {
            try {
                val initialStream: InputStream? =
                    contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Unable to open input stream for TSA certificate URI")
                val tsaCertFolder = File(context.filesDir, DIR_TSA_CERT)
                if (!tsaCertFolder.exists()) {
                    val isFolderCreated = tsaCertFolder.mkdirs()
                    debugLog(logTag, String.format("TSA cert folder created: %s", isFolderCreated))
                }
                val fileName =
                    DocumentFile
                        .fromSingleUri(context, uri)
                        ?.name
                        .takeUnless { it.isNullOrEmpty() } ?: "tsaCert"
                val tsaFile = File(tsaCertFolder, fileName)
                FileUtils.copyInputStreamToFile(initialStream, tsaFile)
                dataStore.setTSACertName(tsaFile.name)
            } catch (e: Exception) {
                errorLog(logTag, "Unable to read TSA certificate file data", e)
            }
        }

        fun handleCryptoCertFile(uri: Uri) {
            try {
                val initialStream: InputStream? =
                    contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Unable to open input stream for crypto certificate URI")
                val cryptoCertFolder = File(context.filesDir, DIR_CRYPTO_CERT)
                if (!cryptoCertFolder.exists()) {
                    val isFolderCreated = cryptoCertFolder.mkdirs()
                    debugLog(logTag, String.format("Crypto cert folder created: %s", isFolderCreated))
                }
                val fileName =
                    DocumentFile
                        .fromSingleUri(context, uri)
                        ?.name
                        .takeUnless { it.isNullOrEmpty() } ?: "cryptoCert"
                val cryptoCertFile = File(cryptoCertFolder, fileName)
                FileUtils.copyInputStreamToFile(initialStream, cryptoCertFile)
                dataStore.setCryptoCertName(cryptoCertFile.name)
            } catch (e: Exception) {
                errorLog(logTag, "Unable to read Crypto certificate file data", e)
            }
        }

        private fun resetCertificateInfo() {
            _sivaIssuedTo.value = null
            _sivaValidTo.value = null
            _sivaCertificate.value = null
            _tsaIssuedTo.value = null
            _tsaValidTo.value = null
            _tsaCertificate.value = null
            _cryptoCertIssuedTo.value = null
            _cryptoCertValidTo.value = null
            _cryptoCertificate.value = null
        }

        fun resetErrorState() {
            _errorState.value = null
        }

        fun checkConnection(manualProxySettings: ManualProxy) {
            debugLog(logTag, "Checking connection")

            saveProxySettings(manualProxySettings)

            val request: Request =
                Request
                    .Builder()
                    .url("https://id.eesti.ee/config.json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", UserAgentUtil.getUserAgent(context))
                    .build()

            val httpClient: OkHttpClient
            try {
                httpClient = constructClientBuilder(context).build()
            } catch (e: Exception) {
                throw IllegalStateException("Failed to construct HTTP client", e)
            }

            viewModelScope.launch(IO) {
                val call = httpClient.newCall(request)
                try {
                    val response = call.execute()
                    val isProxyInUse = dataStore.getProxySetting() != ProxySetting.NO_PROXY
                    if (isProxyInUse && (response.code == 403 || response.code == 407)) {
                        debugLog(logTag, "Forbidden error with proxy configuration")
                        _errorState.value =
                            R.string.main_settings_proxy_check_username_and_password
                    } else if (response.code != 200) {
                        debugLog(logTag, "No Internet connection detected")
                        _errorState.value =
                            R.string.main_settings_proxy_check_connection_unsuccessful
                    } else {
                        debugLog(logTag, "Internet connection detected successfully")
                        _errorState.value =
                            R.string.main_settings_proxy_check_connection_success
                    }
                } catch (e: IOException) {
                    val message = e.message
                    val isProxyAuthenticationFailure =
                        message != null &&
                            (
                                message.contains("CONNECT: 403") ||
                                    message.contains("CONNECT: 407") ||
                                    message.contains("Failed to authenticate with proxy")
                            )
                    errorLog(logTag, "Unable to check Internet connection", e)
                    _errorState.value =
                        if (isProxyAuthenticationFailure) {
                            R.string.main_settings_proxy_check_username_and_password
                        } else {
                            R.string.main_settings_proxy_check_connection_unsuccessful
                        }
                }
            }
        }

        fun recreateActivity(shouldResetLogging: Boolean = false) {
            activityManager.setShouldResetLogging(shouldResetLogging = shouldResetLogging)
            activityManager.setShouldRecreateActivity(true)
        }
    }
