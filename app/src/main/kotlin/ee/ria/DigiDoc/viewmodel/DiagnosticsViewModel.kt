@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.io.ByteStreams
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.BuildConfig
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.utils.TSLUtil
import ee.ria.DigiDoc.domain.model.settings.CDOCSetting
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.utils.Constant.Defaults.DEFAULT_UUID_VALUE
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.getAccessibilityEventType
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.NoSuchFileException
import java.util.Arrays
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DiagnosticsViewModel
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        val dataStore: DataStore,
        private val configurationLoader: ConfigurationLoader,
        private val configurationRepository: ConfigurationRepository,
        private val contentResolver: ContentResolver,
    ) : ViewModel() {
        private val logTag = "SettingsViewModel"
        private val logsFileName =
            "ria_digidoc_${getAppVersion()}.${getAppVersionCode()}_logs.log"
        private val diagnosticsFileName =
            "ria_digidoc_${getAppVersion()}.${getAppVersionCode()}_diagnostics.log"

        private val _updatedConfiguration = MutableLiveData<ConfigurationProvider?>()
        var updatedConfiguration: LiveData<ConfigurationProvider?> = _updatedConfiguration

        val isCdoc2Enabled = false

        init {
            observeConfigurationUpdates(context)
        }

        fun observeConfigurationUpdates(context: Context) {
            CoroutineScope(Main).launch {
                configurationRepository.observeConfigurationUpdates { newConfig ->
                    CoroutineScope(Main).launch {
                        if (updatedConfiguration.value != null) {
                            val messageResId =
                                if (newConfig.configurationUpdateDate !==
                                    updatedConfiguration.value?.configurationUpdateDate
                                ) {
                                    R.string.configuration_updated
                                } else {
                                    R.string.configuration_is_already_up_to_date
                                }
                            sendAccessibilityEvent(
                                context,
                                getAccessibilityEventType(),
                                context.getString(messageResId),
                            )
                        }
                        _updatedConfiguration.value = newConfig
                    }
                }
            }
        }

        fun getSivaUrl(): String {
            val settingsSivaUrl = dataStore.getSettingsSivaUrl()
            if (settingsSivaUrl.isNotEmpty()) {
                return settingsSivaUrl
            }
            val value = updatedConfiguration.value?.sivaUrl
            return value ?: ""
        }

        fun getTsaUrl(): String {
            val settingsTsaUrl = dataStore.getSettingsTSAUrl()
            if (settingsTsaUrl.isNotEmpty()) {
                return settingsTsaUrl
            }
            val value = updatedConfiguration.value?.tsaUrl
            return value ?: ""
        }

        fun getConfigurationDate(date: Date?): String {
            val dateFormatted = date?.let { DateUtil.dateTimeFormatWithDots.format(it) }

            return dateFormatted ?: ""
        }

        fun getRpUuid(): Int {
            val rpUuid: String = dataStore.getSettingsUUID()
            val uuid: Int =
                if (rpUuid.isEmpty() || rpUuid == DEFAULT_UUID_VALUE) {
                    R.string.main_diagnostics_rpuuid_default
                } else {
                    R.string.main_diagnostics_rpuuid_custom
                }
            return uuid
        }

        fun isCdoc2Selected(): Boolean = dataStore.getCdocSetting() == CDOCSetting.CDOC2

        fun isCdoc2KeyServerUsed(): Boolean {
            val isCdoc2Setting = isCdoc2Selected()
            val cdoc2UseKeyServer = updatedConfiguration.value?.cdoc2UseKeyServer ?: false
            return isCdoc2Setting && dataStore.getUseOnlineEncryption(cdoc2UseKeyServer)
        }

        fun getCdoc2KeyServerUUID(): String {
            val defaultKeyServer = updatedConfiguration.value?.cdoc2DefaultKeyServer ?: DEFAULT_UUID_VALUE
            return dataStore.getCDOC2UUID(defaultKeyServer)
        }

        fun getTslCacheData(context: Context): List<String> {
            val tslCacheList = ArrayList<String>()
            val tslCacheDir = File(context.cacheDir.absolutePath + "/schema")
            val tslFiles =
                tslCacheDir.listFiles { _: File?, fileName: String ->
                    fileName.endsWith(
                        ".xml",
                    )
                }

            if (tslFiles != null && tslFiles.isNotEmpty()) {
                Arrays.sort(
                    tslFiles,
                ) { f1: File, f2: File ->
                    f1.name.compareTo(f2.name, ignoreCase = true)
                }
                for (tslFile in tslFiles) {
                    try {
                        FileInputStream(tslFile).use { tslInputStream ->
                            val tslEntryText: String =
                                getTSLFileVersion(tslInputStream, tslFile.name)
                            tslCacheList.add(tslEntryText)
                        }
                    } catch (e: Exception) {
                        errorLog(
                            logTag,
                            "Error displaying TSL version for: ${tslFile.absolutePath}",
                            e,
                        )
                    }
                }
            }
            return tslCacheList
        }

        @Throws(Exception::class)
        suspend fun updateConfiguration(context: Context) {
            configurationLoader.loadCentralConfiguration(
                context,
                dataStore.getProxySetting(),
                dataStore.getManualProxySettings(),
            )
        }

        fun saveFile(
            documentFile: File,
            activityResult: ActivityResult,
        ) {
            FileInputStream(documentFile).use { inputStream ->
                activityResult.data?.data?.let {
                    contentResolver
                        .openOutputStream(it)
                        .use { outputStream ->
                            if (outputStream != null) {
                                ByteStreams.copy(inputStream, outputStream)
                            }
                        }
                }
            }
        }

        fun createLogFile(context: Context): File {
            if (FileUtil.logsExist(FileUtil.getLogsDirectory(context))) {
                return FileUtil.combineLogFiles(
                    FileUtil.getLogsDirectory(context),
                    logsFileName,
                )
            }
            throw FileNotFoundException("Unable to get directory with logs")
        }

        fun resetLogs(context: Context) {
            LoggingUtil.resetLogs(FileUtil.getLogsDirectory(context))
        }

        fun createDiagnosticsFile(context: Context): File {
            val diagnosticsFilePath: String = File(context.filesDir.path, "diagnostics").path
            val root = File(diagnosticsFilePath)
            if (!root.exists()) {
                val isDirectoryCreated = root.mkdirs()
                if (!isDirectoryCreated) {
                    errorLog(logTag, "Unable to create directory for diagnostics files")
                    throw NoSuchFileException(
                        root.absolutePath,
                        null,
                        "Unable to create directory for diagnostics files",
                    )
                }
            }

            val diagnosticsFileLocation = File(diagnosticsFilePath, diagnosticsFileName)
            try {
                FileOutputStream(diagnosticsFileLocation).use { fileStream ->
                    OutputStreamWriter(fileStream, StandardCharsets.UTF_8)
                        .use { writer ->
                            writer.append(formatDiagnosticsText(context))
                            writer.flush()
                            return diagnosticsFileLocation
                        }
                }
            } catch (ex: IOException) {
                errorLog(logTag, "Unable to get diagnostics file location", ex)
                throw ex
            }
        }

        fun getAppVersion(): String = BuildConfig.VERSION_NAME

        fun getAppVersionCode(): Int = BuildConfig.VERSION_CODE

        @Throws(XmlPullParserException::class, IOException::class)
        private fun getTSLFileVersion(
            tslInputStream: InputStream,
            tslFileName: String,
        ): String {
            val version: Int = TSLUtil.readSequenceNumber(tslInputStream)
            return FileUtil.normalizeText(tslFileName) + " (" + version + ")"
        }

        private fun formatDiagnosticsText(context: Context): String {
            val diagnosticsText =
                buildString {
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_application_version_title,
                        )} ${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_operating_system_title,
                        )} Android ${Build.VERSION.RELEASE}",
                    )

                    // Category
                    appendLine()
                    appendLine(context.getString(R.string.main_diagnostics_libraries_title))
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_libdigidocpp_title,
                        )} ${dataStore.getLibdigidocppVersion()}",
                    )

                    // Category
                    appendLine()
                    appendLine(context.getString(R.string.main_diagnostics_urls_title))
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_config_url_title,
                        )} ${updatedConfiguration.value?.metaInf?.url}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_tsl_url_title,
                        )} ${updatedConfiguration.value?.tslUrl}",
                    )
                    appendLine(
                        "${context.getString(R.string.main_diagnostics_siva_url_title)} ${getSivaUrl()}",
                    )
                    appendLine(
                        "${context.getString(R.string.main_diagnostics_tsa_url_title)} ${getTsaUrl()}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_ldap_person_url_title,
                        )} ${updatedConfiguration.value?.ldapPersonUrls?.joinToString(", ")}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_ldap_corp_url_title,
                        )} ${updatedConfiguration.value?.ldapCorpUrl}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_mid_proxy_url_title,
                        )} ${updatedConfiguration.value?.midRestUrl}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_mid_sk_url_title,
                        )} ${updatedConfiguration.value?.midSkRestUrl}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_sid_v2_proxy_url_title,
                        )} ${updatedConfiguration.value?.sidV2RestUrl}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_sid_v2_sk_url_title,
                        )} ${updatedConfiguration.value?.sidV2SkRestUrl}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_rpuuid_title,
                        )} ${context.getString(getRpUuid())}",
                    )

                    if (isCdoc2Enabled) {
                        // CDOC2
                        appendLine()
                        appendLine(context.getString(R.string.main_diagnostics_cdoc2_title))
                        appendLine(
                            "${
                                context.getString(
                                    R.string.main_diagnostics_cdoc2_default_title,
                                )
                            } ${isCdoc2Selected()}",
                        )
                        appendLine(
                            "${
                                context.getString(
                                    R.string.main_diagnostics_cdoc2_use_keyserver_title,
                                )
                            } ${isCdoc2KeyServerUsed()}",
                        )
                        appendLine(
                            "${
                                context.getString(
                                    R.string.main_diagnostics_cdoc2_default_keyserver_title,
                                )
                            } ${getCdoc2KeyServerUUID()}",
                        )
                    }

                    // Category
                    appendLine()
                    appendLine(context.getString(R.string.main_diagnostics_tsl_cache_title))
                    getTslCacheData(context).forEach { data ->
                        appendLine(data)
                    }

                    // Category
                    appendLine()
                    appendLine(context.getString(R.string.main_diagnostics_central_configuration_title))
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_date_title,
                        )} ${updatedConfiguration.value?.metaInf?.date}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_serial_title,
                        )} ${updatedConfiguration.value?.metaInf?.serial}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_url_title,
                        )} ${updatedConfiguration.value?.metaInf?.url}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_version_title,
                        )} ${updatedConfiguration.value?.metaInf?.version}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_configuration_update_date,
                        )} ${getConfigurationDate(updatedConfiguration.value?.configurationUpdateDate)}",
                    )
                    appendLine(
                        "${context.getString(
                            R.string.main_diagnostics_configuration_last_check_date,
                        )} ${getConfigurationDate(updatedConfiguration.value?.configurationLastUpdateCheckDate)}",
                    )
                }

            return diagnosticsText
        }
    }
