@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.preferences

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.IS_CRASH_SENDING_ALWAYS_ENABLED
import ee.ria.DigiDoc.common.Constant.KEY_LOCALE
import ee.ria.DigiDoc.common.preferences.EncryptedPreferences
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.siva.SivaSetting
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.DigiDoc.utilsLib.toast.ToastUtil
import ee.ria.libdigidocpp.digidoc
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.Locale
import javax.inject.Inject

class DataStore
    @Inject
    constructor(private var context: Context) {
        private val logTag = javaClass.simpleName

        private var preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        private var resources: Resources = context.resources

        fun getSignatureAddMethod(): String {
            val signatureAddMethod =
                preferences.getString(
                    resources.getString(R.string.main_settings_signature_add_method_key),
                    Route.MobileId.route,
                ) ?: Route.MobileId.route
            val signatureAddMethods =
                arrayOf(
                    Route.MobileId.route,
                    Route.SmartId.route,
                    Route.IdCard.route,
                    Route.NFC.route,
                )
            if (!listOf(*signatureAddMethods).contains(signatureAddMethod)) {
                return Route.MobileId.route
            }
            return signatureAddMethod
        }

        fun setSignatureAddMethod(method: String) {
            val editor = preferences.edit()
            editor.putString(resources.getString(R.string.main_settings_signature_add_method_key), method)
            editor.apply()
        }

        fun getCanNumber(): String {
            val encryptedPreferences: SharedPreferences? = getEncryptedPreferences(context)
            if (encryptedPreferences != null) {
                return encryptedPreferences.getString(
                    resources.getString(R.string.main_settings_can_key),
                    "",
                ) ?: ""
            }
            errorLog(
                logTag,
                "Un" +
                    "able to read CAN",
            )
            return ""
        }

        fun setCanNumber(can: String) {
            val encryptedPreferences: SharedPreferences? = getEncryptedPreferences(context)
            if (encryptedPreferences != null) {
                val editor: SharedPreferences.Editor = encryptedPreferences.edit()
                editor.putString(resources.getString(R.string.main_settings_can_key), can)
                editor.apply()
                return
            }
            errorLog(logTag, "Unable to save CAN")
        }

        fun getPhoneNo(): String {
            return preferences.getString(
                resources.getString(R.string.main_settings_phone_no_key),
                "372",
            ) ?: "372"
        }

        fun setPhoneNo(phoneNo: String) {
            val editor = preferences.edit()
            editor.putString(resources.getString(R.string.main_settings_phone_no_key), phoneNo)
            editor.apply()
        }

        fun getPersonalCode(): String {
            return preferences.getString(
                resources.getString(R.string.main_settings_personal_code_key),
                "",
            ) ?: ""
        }

        fun setPersonalCode(personalCode: String) {
            val editor = preferences.edit()
            editor.putString(
                resources.getString(R.string.main_settings_personal_code_key),
                personalCode,
            )
            editor.apply()
        }

        fun getSidPersonalCode(): String {
            return preferences.getString(
                resources.getString(R.string.main_settings_sid_personal_code_key),
                "",
            ) ?: ""
        }

        fun setSidPersonalCode(personalCode: String) {
            val editor = preferences.edit()
            editor.putString(
                resources.getString(R.string.main_settings_sid_personal_code_key),
                personalCode,
            )
            editor.apply()
        }

        fun getCountry(): Int {
            return preferences.getInt(
                resources.getString(R.string.main_settings_smartid_country_key),
                0,
            )
        }

        fun setCountry(country: Int) {
            val editor = preferences.edit()
            editor.putInt(
                resources.getString(R.string.main_settings_smartid_country_key),
                country,
            )
            editor.apply()
        }

        fun getSettingsUUID(): String {
            return preferences.getString(resources.getString(R.string.main_settings_uuid_key), "") ?: ""
        }

        fun setSettingsUUID(uuid: String) {
            val editor = preferences.edit()
            editor.putString(resources.getString(R.string.main_settings_uuid_key), uuid)
            editor.apply()
        }

        fun getSettingsTSAUrl(): String {
            return preferences.getString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_url_key),
                "",
            )
                ?: ""
        }

        fun setSettingsTSAUrl(tsaUrl: String) {
            val editor = preferences.edit()
            editor.putString(resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_url_key), tsaUrl)
            editor.apply()
        }

        fun getIsLogFileGenerationEnabled(): Boolean {
            return preferences.getBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_key),
                false,
            )
        }

        fun setIsLogFileGenerationEnabled(isEnabled: Boolean) {
            val editor = preferences.edit()
            editor.putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_key),
                isEnabled,
            )
            editor.apply()
        }

        fun getIsLogFileGenerationRunning(): Boolean {
            return preferences.getBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                false,
            )
        }

        fun setIsLogFileGenerationRunning(isRunning: Boolean) {
            val editor = preferences.edit()
            editor.putBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                isRunning,
            )
            editor.apply()
        }

        fun getSettingsSivaUrl(): String {
            return preferences.getString(
                resources.getString(
                    ee.ria.DigiDoc.network.R.string.main_settings_siva_url_key,
                ),
                "",
            )
                ?: ""
        }

        fun setSettingsSivaUrl(sivaUrl: String) {
            val editor = preferences.edit()
            editor.putString(
                resources.getString(
                    ee.ria.DigiDoc.network.R.string.main_settings_siva_url_key,
                ),
                sivaUrl,
            )
            editor.apply()
        }

        fun getSettingsSivaCertName(): String {
            return preferences.getString(
                resources.getString(
                    ee.ria.DigiDoc.network.R.string.main_settings_siva_cert_key,
                ),
                "",
            )
                ?: ""
        }

        fun setSettingsSivaCertName(cert: String?) {
            val editor = preferences.edit()
            editor.putString(resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_siva_cert_key), cert)
            editor.apply()
        }

        fun setSivaSetting(sivaSetting: SivaSetting) {
            val editor = preferences.edit()
            editor.putString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_siva_setting_key),
                sivaSetting.name,
            )
            editor.apply()
        }

        fun getSivaSetting(): SivaSetting {
            val sivaSetting =
                preferences.getString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_siva_setting_key),
                    SivaSetting.DEFAULT.name,
                )
            try {
                return sivaSetting?.let { SivaSetting.valueOf(it) } ?: SivaSetting.DEFAULT
            } catch (iae: java.lang.IllegalArgumentException) {
                debugLog(logTag, "Unable to get SiVa setting value", iae)
                return SivaSetting.DEFAULT
            }
        }

        fun getSettingsAskRoleAndAddress(): Boolean {
            return preferences.getBoolean(
                resources.getString(R.string.main_settings_ask_role_and_address_key),
                false,
            )
        }

        fun setSettingsAskRoleAndAddress(isRoleAskingEnabled: Boolean) {
            val editor = preferences.edit()
            editor.putBoolean(
                resources.getString(R.string.main_settings_ask_role_and_address_key),
                isRoleAskingEnabled,
            )
            editor.apply()
        }

        fun getRoles(): String {
            return preferences.getString(
                resources.getString(R.string.main_settings_role_key),
                "",
            ) ?: ""
        }

        fun setRoles(roles: String?) {
            val editor = preferences.edit()
            editor.putString(
                resources.getString(R.string.main_settings_role_key),
                roles,
            )
            editor.apply()
        }

        fun getRoleCity(): String {
            return preferences.getString(
                resources.getString(R.string.main_settings_city_key),
                "",
            ) ?: ""
        }

        fun setRoleCity(city: String?) {
            val editor = preferences.edit()
            editor.putString(
                resources.getString(R.string.main_settings_city_key),
                city,
            )
            editor.apply()
        }

        fun getRoleState(): String {
            return preferences.getString(
                resources.getString(R.string.main_settings_county_key),
                "",
            ) ?: ""
        }

        fun setRoleState(state: String?) {
            val editor = preferences.edit()
            editor.putString(
                resources.getString(R.string.main_settings_county_key),
                state,
            )
            editor.apply()
        }

        fun getRoleCountry(): String {
            return preferences.getString(
                resources.getString(R.string.main_settings_country_key),
                "",
            ) ?: ""
        }

        fun setRoleCountry(country: String?) {
            val editor = preferences.edit()
            editor.putString(
                resources.getString(R.string.main_settings_country_key),
                country,
            )
            editor.apply()
        }

        fun getRoleZip(): String {
            return preferences.getString(
                resources.getString(R.string.main_settings_postal_code_key),
                "",
            ) ?: ""
        }

        fun setRoleZip(zip: String?) {
            val editor = preferences.edit()
            editor.putString(
                resources.getString(R.string.main_settings_postal_code_key),
                zip,
            )
            editor.apply()
        }

        fun setTSACertName(cert: String?) {
            val editor = preferences.edit()
            editor.putString(resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_cert_key), cert)
            editor.apply()
        }

        fun getTSACertName(): String {
            return preferences.getString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_cert_key),
                "",
            )
                ?: ""
        }

        fun setIsTsaCertificateViewVisible(isVisible: Boolean) {
            val editor = preferences.edit()
            editor.putBoolean(resources.getString(R.string.main_settings_tsa_cert_view), isVisible)
            editor.apply()
        }

        fun getIsTsaCertificateViewVisible(): Boolean {
            return preferences.getBoolean(
                resources.getString(R.string.main_settings_tsa_cert_view),
                false,
            )
        }

        fun getSettingsOpenAllFileTypes(): Boolean {
            return preferences.getBoolean(
                resources.getString(R.string.main_settings_open_all_filetypes_key),
                true,
            )
        }

        fun setSettingsOpenAllFileTypes(isEnabled: Boolean) {
            val editor = preferences.edit()
            editor.putBoolean(
                resources.getString(R.string.main_settings_open_all_filetypes_key),
                isEnabled,
            )
            editor.apply()
        }

        fun getSettingsAllowScreenshots(): Boolean {
            return preferences.getBoolean(
                resources.getString(R.string.main_settings_allow_screenshots_key),
                false,
            )
        }

        fun setSettingsAllowScreenshots(isEnabled: Boolean) {
            val editor = preferences.edit()
            editor.putBoolean(
                resources.getString(R.string.main_settings_allow_screenshots_key),
                isEnabled,
            )
            editor.apply()
        }

        fun setProxySetting(proxySetting: ProxySetting) {
            val editor = preferences.edit()
            editor.putString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_setting_key),
                proxySetting.name,
            )
            editor.apply()
        }

        fun getProxySetting(): ProxySetting {
            val settingKey =
                preferences.getString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_setting_key),
                    ProxySetting.NO_PROXY.name,
                )
            return try {
                if (settingKey != null) {
                    ProxySetting.valueOf(settingKey)
                } else {
                    ProxySetting.NO_PROXY
                }
            } catch (iae: IllegalArgumentException) {
                errorLog(logTag, "Unable to get proxy setting", iae)
                ProxySetting.NO_PROXY
            }
        }

        fun setProxyHost(host: String) {
            val editor = preferences.edit()
            editor.putString(resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_host_key), host)
            editor.apply()
        }

        fun getProxyHost(): String {
            return preferences.getString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_host_key),
                "",
            )
                ?: ""
        }

        fun setProxyPort(port: Int) {
            val editor = preferences.edit()
            editor.putInt(resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_port_key), port)
            editor.apply()
        }

        fun getProxyPort(): Int {
            return preferences.getInt(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_port_key),
                80,
            )
        }

        fun setProxyUsername(username: String) {
            val editor = preferences.edit()
            editor.putString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_username_key),
                username,
            )
            editor.apply()
        }

        fun getProxyUsername(): String {
            return preferences.getString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_username_key),
                "",
            ) ?: ""
        }

        fun setProxyPassword(password: String) {
            val encryptedPreferences: SharedPreferences? = getEncryptedPreferences(context)
            if (encryptedPreferences != null) {
                val editor = encryptedPreferences.edit()
                editor.putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_password_key),
                    password,
                )
                editor.apply()
            }
            errorLog(logTag, "Unable to set proxy password")
        }

        fun getProxyPassword(): String {
            val encryptedPreferences: SharedPreferences? = getEncryptedPreferences(context)
            if (encryptedPreferences != null) {
                return encryptedPreferences.getString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_password_key),
                    "",
                ) ?: ""
            }
            errorLog(logTag, "Unable to get proxy password")
            return ""
        }

        fun getLocale(): Locale? {
            val locale = preferences.getString(KEY_LOCALE, null)
            if (locale != null) {
                return Locale(locale)
            }
            return null
        }

        fun setLocale(locale: Locale?) {
            if (locale == null) {
                preferences.edit().remove(KEY_LOCALE).apply()
            } else {
                preferences.edit().putString(KEY_LOCALE, locale.language).apply()
            }
        }

        fun getLibdigidocppVersion(): String {
            return digidoc.version()
        }

        fun getManualProxySettings(): ManualProxy {
            return ManualProxy(
                getProxyHost(),
                getProxyPort(),
                getProxyUsername(),
                getProxyPassword(),
            )
        }

        fun getIsCrashSendingAlwaysEnabled(): Boolean {
            return preferences.getBoolean(IS_CRASH_SENDING_ALWAYS_ENABLED, false)
        }

        fun setIsCrashSendingAlwaysEnabled(isEnabled: Boolean) {
            preferences.edit().putBoolean(IS_CRASH_SENDING_ALWAYS_ENABLED, isEnabled).apply()
        }

        private fun getEncryptedPreferences(context: Context): SharedPreferences? {
            return try {
                EncryptedPreferences.getEncryptedPreferences(context)
            } catch (e: GeneralSecurityException) {
                errorLog(logTag, "Unable to get encrypted preferences", e)
                ToastUtil.showMessage(context, R.string.signature_update_mobile_id_error_general_client)
                null
            } catch (e: IOException) {
                errorLog(logTag, "Unable to get encrypted preferences", e)
                ToastUtil.showMessage(context, R.string.signature_update_mobile_id_error_general_client)
                null
            }
        }
    }
