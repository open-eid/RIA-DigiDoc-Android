@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityEvent
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
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
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
        @ApplicationContext private val context: Context,
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
                            AccessibilityUtil.sendAccessibilityEvent(
                                context,
                                AccessibilityEvent.TYPE_ANNOUNCEMENT,
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
                if (rpUuid.isEmpty()) {
                    R.string.main_diagnostics_rpuuid_default
                } else {
                    R.string.main_diagnostics_rpuuid_custom
                }
            return uuid
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
                        .openOutputStream(it).use { outputStream ->
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

        fun getAppVersion(): String {
            return BuildConfig.VERSION_NAME
        }

        fun getAppVersionCode(): Int {
            return BuildConfig.VERSION_CODE
        }

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
                        )} ${updatedConfiguration.value?.ldapPersonUrl}",
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
