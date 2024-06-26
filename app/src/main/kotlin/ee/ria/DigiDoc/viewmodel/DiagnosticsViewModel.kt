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
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.utils.TSLUtil
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
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
        private val diagnosticsFileName =
            "ria_digidoc_" + getAppVersion() + "_diagnostics.txt"
        private val diagnosticsFilePath: String = (
            context.filesDir.path +
                File.separator + "diagnostics" + File.separator
        )
        private val diagnosticsLogsFilePath =
            "ria_digidoc_" + getAppVersion() + "_logs.txt"

        private val _getConfigurationLastUpdateCheckDate = MutableLiveData<String?>()
        val getConfigurationLastUpdateCheckDate: LiveData<String?> = _getConfigurationLastUpdateCheckDate

        fun refreshConfigurationVariables() {
            _getConfigurationLastUpdateCheckDate.postValue(getConfigurationLastUpdateCheckDate())
        }

        private fun getAppVersion(): String {
            return BuildConfig.VERSION_NAME
        }

        fun getConfigUrl(): String {
            val value = configurationRepository.getConfiguration()?.configUrl
            return value ?: ""
        }

        fun getTslUrl(): String {
            val value = configurationRepository.getConfiguration()?.tslUrl
            return value ?: ""
        }

        fun getSivaUrl(): String {
            val settingsSivaUrl = dataStore.getSettingsSivaUrl()
            if (settingsSivaUrl.isNotEmpty()) {
                return settingsSivaUrl
            }
            val value = configurationRepository.getConfiguration()?.sivaUrl
            return value ?: ""
        }

        fun getTsaUrl(): String {
            val settingsTsaUrl = dataStore.getSettingsTSAUrl()
            if (settingsTsaUrl.isNotEmpty()) {
                return settingsTsaUrl
            }
            val value = configurationRepository.getConfiguration()?.tsaUrl
            return value ?: ""
        }

        fun getLdapPersonUrl(): String {
            val value = configurationRepository.getConfiguration()?.ldapPersonUrl
            return value ?: ""
        }

        fun getLdapCorpUrl(): String {
            val value = configurationRepository.getConfiguration()?.ldapCorpUrl
            return value ?: ""
        }

        fun getMidRestUrl(): String {
            val value = configurationRepository.getConfiguration()?.midRestUrl
            return value ?: ""
        }

        fun getMidSkRestUrl(): String {
            val value = configurationRepository.getConfiguration()?.midSkRestUrl
            return value ?: ""
        }

        fun getSidV2RestUrl(): String {
            val value = configurationRepository.getConfiguration()?.sidV2RestUrl
            return value ?: ""
        }

        fun getSidV2SkRestUrl(): String {
            val value = configurationRepository.getConfiguration()?.sidV2SkRestUrl
            return value ?: ""
        }

        fun getMetaInfGetDate(): String {
            val value = configurationRepository.getConfiguration()?.metaInf?.date
            return value ?: ""
        }

        fun getMetaInfGetSerial(): String {
            val value = configurationRepository.getConfiguration()?.metaInf?.serial.toString()
            return value
        }

        fun getMetaInfGetUrl(): String {
            val value = configurationRepository.getConfiguration()?.metaInf?.url
            return value ?: ""
        }

        fun getMetaInfGetVersion(): String {
            val value = configurationRepository.getConfiguration()?.metaInf?.version.toString()
            return value
        }

        fun getConfigurationUpdateDate(): String {
            val value = configurationRepository.getConfiguration()?.configurationUpdateDate
            val dateFormatted = value?.let { DateUtil.dateFormat.format(it) }

            return dateFormatted ?: ""
        }

        fun getConfigurationLastUpdateCheckDate(): String {
            val value = configurationRepository.getConfiguration()?.configurationLastUpdateCheckDate
            val dateFormatted = value?.let { DateUtil.dateFormat.format(it) }

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

        fun getTslCacheData(): List<String> {
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

        @Throws(XmlPullParserException::class, IOException::class)
        private fun getTSLFileVersion(
            tslInputStream: InputStream,
            tslFileName: String,
        ): String {
            val version: Int = TSLUtil.readSequenceNumber(tslInputStream)
            return FileUtil.normalizeText(tslFileName) + " (" + version + ")"
        }

        suspend fun updateConfiguration() {
            configurationLoader.initConfiguration(context)
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

        fun createLogFile(): File {
            if (FileUtil.logsExist(FileUtil.getLogsDirectory(context))) {
                return FileUtil.combineLogFiles(
                    FileUtil.getLogsDirectory(context),
                    diagnosticsLogsFilePath,
                )
            }
            throw FileNotFoundException("Unable to get directory with logs")
        }

        fun createDiagnosticsFile(): File {
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

            val diagnosticsFileLocation = File(diagnosticsFilePath + diagnosticsFileName)
            try {
                FileOutputStream(diagnosticsFileLocation).use { fileStream ->
                    OutputStreamWriter(fileStream, StandardCharsets.UTF_8)
                        .use { writer ->
                            writer.append(formatDiagnosticsText())
                            writer.flush()
                            return diagnosticsFileLocation
                        }
                }
            } catch (ex: IOException) {
                errorLog(logTag, "Unable to get diagnostics file location", ex)
                throw ex
            }
        }

        private fun formatDiagnosticsText(): String {
            val diagnosticsText = StringBuilder()

            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_application_version_title) +
                    BuildConfig.VERSION_NAME,
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_operating_system_title) +
                    "Android " + Build.VERSION.RELEASE,
            ).append("\n")

            // Category
            diagnosticsText.append("\n\n").append(
                context.getString(R.string.main_diagnostics_libraries_title),
            ).append("\n")

            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_libdigidocpp_title) +
                    dataStore.getLibdigidocppVersion(),
            ).append("\n")

            // Category
            diagnosticsText.append("\n\n").append(
                context.getString(R.string.main_diagnostics_urls_title),
            ).append("\n")

            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_config_url_title) +
                    getConfigUrl(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_tsl_url_title) +
                    getTslUrl(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_siva_url_title) +
                    getSivaUrl(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_tsa_url_title) +
                    getTsaUrl(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_ldap_person_url_title) +
                    getLdapPersonUrl(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_ldap_corp_url_title) +
                    getLdapCorpUrl(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_mid_proxy_url_title) +
                    getMidRestUrl(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_mid_sk_url_title) +
                    getMidSkRestUrl(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_sid_v2_proxy_url_title) +
                    getSidV2RestUrl(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_sid_v2_sk_url_title) +
                    getSidV2SkRestUrl(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_rpuuid_title) +
                    context.getString(getRpUuid()),
            ).append("\n")

            // Category
            diagnosticsText.append("\n\n").append(
                context.getString(R.string.main_diagnostics_tsl_cache_title),
            ).append("\n")

            getTslCacheData().forEach { data ->
                diagnosticsText.append(data).append("\n")
            }

            // Category
            diagnosticsText.append("\n\n").append(
                context.getString(R.string.main_diagnostics_central_configuration_title),
            ).append("\n")

            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_date_title) +
                    getMetaInfGetDate(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_serial_title) +
                    getMetaInfGetSerial(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_url_title) +
                    getMetaInfGetUrl(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_version_title) +
                    getMetaInfGetVersion(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_configuration_update_date) +
                    getConfigurationUpdateDate(),
            ).append("\n")
            diagnosticsText.append(
                context.getString(R.string.main_diagnostics_configuration_last_check_date) +
                    getConfigurationLastUpdateCheckDate(),
            ).append("\n")

            return diagnosticsText.toString()
        }
    }
