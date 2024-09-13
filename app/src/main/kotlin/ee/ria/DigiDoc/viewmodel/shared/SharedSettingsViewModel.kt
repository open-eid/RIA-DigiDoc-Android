@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.DIR_SIVA_CERT
import ee.ria.DigiDoc.common.Constant.DIR_TSA_CERT
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.manager.ActivityManager
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxyConfig
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.proxy.ProxyUtil
import ee.ria.DigiDoc.network.siva.SivaSetting
import ee.ria.DigiDoc.network.utils.NetworkUtil.constructClientBuilder
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.DigiDoc.utilsLib.signing.CertificateUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
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
        @ApplicationContext private val context: Context,
        private val contentResolver: ContentResolver,
        val dataStore: DataStore,
        private val initialization: Initialization,
        private val configurationRepository: ConfigurationRepository,
        private val activityManager: ActivityManager,
    ) : ViewModel() {
        private val logTag = "SharedSettingsViewModel"

        private val _updatedConfiguration = MutableLiveData<ConfigurationProvider?>()
        val updatedConfiguration: LiveData<ConfigurationProvider?> = _updatedConfiguration

        private val _issuedTo = MutableLiveData<String?>()
        val issuedTo: LiveData<String?> = _issuedTo

        private val _validTo = MutableLiveData<String?>()
        val validTo: LiveData<String?> = _validTo

        private val _previousSivaUrl = MutableLiveData<String?>()
        val previousSivaUrl: LiveData<String?> = _previousSivaUrl

        private val _sivaCertificate = MutableLiveData<X509Certificate?>()
        val sivaCertificate: LiveData<X509Certificate?> = _sivaCertificate

        private val _errorState = MutableLiveData<Int?>(null)
        val errorState: LiveData<Int?> = _errorState

        init {
            CoroutineScope(Main).launch {
                configurationRepository.observeConfigurationUpdates { newConfig ->
                    _updatedConfiguration.value = newConfig
                }
            }
        }

        fun resetToDefaultSettings() {
            resetSigningSettings()
            resetRightsSettings()
            resetSivaSettings()
            resetProxySettings()
        }

        private fun resetProxySettings() {
            dataStore.setProxySetting(ProxySetting.NO_PROXY)
            clearProxySettings()
        }

        private fun clearProxySettings() {
            val manualProxySettings = ManualProxy("", 80, "", "")
            setManualProxySettings(manualProxySettings)
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

        private fun resetSivaSettings() {
            dataStore.setSivaSetting(SivaSetting.DEFAULT)
            dataStore.setSettingsSivaUrl("")
            dataStore.setSettingsSivaCertName(null)
            removeSivaCert()
        }

        private fun resetRightsSettings() {
            dataStore.setSettingsOpenAllFileTypes(true)
            dataStore.setSettingsAllowScreenshots(false)
        }

        private fun resetSigningSettings() {
            dataStore.setSettingsUUID("")
            dataStore.setSettingsTSAUrl("")
            dataStore.setSettingsAskRoleAndAddress(false)
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

        fun saveProxySettings(
            clearSettings: Boolean,
            manualProxySettings: ManualProxy,
        ) {
            val currentProxySetting: ProxySetting = dataStore.getProxySetting()
            if (currentProxySetting == ProxySetting.MANUAL_PROXY) {
                setManualProxySettings(manualProxySettings)
            } else if (currentProxySetting == ProxySetting.SYSTEM_PROXY) {
                val systemSettings: ProxyConfig = ProxyUtil.getProxy(currentProxySetting, ManualProxy("", 80, "", ""))
                val proxySettings: ManualProxy? = systemSettings.manualProxy()
                if (proxySettings != null) {
                    overrideLibdigidocppProxy(proxySettings)
                    return
                }
                if (clearSettings) {
                    clearProxySettings()
                }
            } else {
                if (clearSettings) {
                    clearProxySettings()
                }
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

        private fun getIssuer(certificateHolder: X509CertificateHolder): String {
            val organizationRDNs = certificateHolder.issuer.getRDNs(BCStyle.O)
            if (organizationRDNs.isNotEmpty()) {
                return organizationRDNs[0].first.value.toString()
            }
            val organizationUnitRDNs = certificateHolder.issuer.getRDNs(BCStyle.OU)
            if (organizationUnitRDNs.isNotEmpty()) {
                return organizationUnitRDNs[0].first.value.toString()
            }
            val commonNameRDNs = certificateHolder.issuer.getRDNs(BCStyle.CN)
            if (commonNameRDNs.isNotEmpty()) {
                return commonNameRDNs[0].first.value.toString()
            }

            return "-"
        }

        fun updateData(sivaServiceUrl: String) {
            _previousSivaUrl.postValue(sivaServiceUrl)

            val sivaCertName: String = dataStore.getSettingsSivaCertName()
            val sivaFile = FileUtil.getCertFile(context, sivaCertName, DIR_SIVA_CERT)

            if (sivaFile != null) {
                val fileContents: String = FileUtil.readFileContent(sivaFile.path)
                try {
                    val sivaCert = CertificateUtil.x509Certificate(fileContents)
                    _sivaCertificate.postValue(sivaCert)
                    val certificateHolder: X509CertificateHolder = JcaX509CertificateHolder(sivaCert)
                    val issuer: String = getIssuer(certificateHolder)
                    _issuedTo.postValue(issuer)
                    _validTo.postValue(getFormattedDateTime(certificateHolder.notAfter))
                } catch (e: CertificateException) {
                    errorLog(logTag, "Unable to get SiVa certificate", e)

                    // Remove invalid files
                    removeSivaCert()
                    resetCertificateInfo()
                }
            }
        }

        fun handleFile(uri: Uri) {
            try {
                val initialStream: InputStream? = contentResolver.openInputStream(uri)
                val documentFile = DocumentFile.fromSingleUri(context, uri)
                if (documentFile != null) {
                    val sivaCertFolder = File(context.filesDir, DIR_SIVA_CERT)
                    if (!sivaCertFolder.exists()) {
                        val isFolderCreated = sivaCertFolder.mkdirs()
                        debugLog(
                            logTag,
                            String.format("SiVa cert folder created: %s", isFolderCreated),
                        )
                    }

                    var fileName = documentFile.name
                    if (fileName.isNullOrEmpty()) {
                        fileName = "sivaCert"
                    }
                    val sivaFile = File(sivaCertFolder, fileName)

                    FileUtils.copyInputStreamToFile(initialStream, sivaFile)

                    dataStore.setSettingsSivaCertName(sivaFile.name)
                }
            } catch (e: Exception) {
                errorLog(logTag, "Unable to read SiVa certificate file data", e)
            }
        }

        private fun resetCertificateInfo() {
            _issuedTo.postValue(null)
            _validTo.postValue(null)
        }

        fun checkConnection(manualProxySettings: ManualProxy) {
            debugLog(logTag, "Checking connection")

            saveProxySettings(false, manualProxySettings)

            val request: Request =
                Request.Builder()
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

            CoroutineScope(IO).launch {
                val call = httpClient.newCall(request)
                try {
                    val response = call.execute()
                    if (response.code == 403) {
                        debugLog(logTag, "Forbidden error with proxy configuration")
                        _errorState.postValue(R.string.main_settings_proxy_check_username_and_password)
                    }

                    if (response.code != 200) {
                        debugLog(logTag, "No Internet connection detected")
                        _errorState.postValue(R.string.main_settings_proxy_check_connection_unsuccessful)
                    } else {
                        debugLog(logTag, "Internet connection detected successfully")
                        _errorState.postValue(R.string.main_settings_proxy_check_connection_success)
                    }
                } catch (e: IOException) {
                    val message = e.message
                    if (message != null && (
                            message.contains("CONNECT: 403") ||
                                message.contains("Failed to authenticate with proxy")
                        )
                    ) {
                        errorLog(
                            logTag,
                            "Received HTTP status 403 or failed to authenticate. " +
                                "Unable to connect with proxy configuration",
                        )
                        _errorState.postValue(R.string.main_settings_proxy_check_connection_unsuccessful)
                    }
                    errorLog(logTag, "Unable to check Internet connection", e)
                    _errorState.postValue(R.string.main_settings_proxy_check_connection_unsuccessful)
                }
            }
        }

        fun recreateActivity() {
            activityManager.setShouldRecreateActivity(true)
        }
    }
