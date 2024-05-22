@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.preferences

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.preferences.EncryptedPreferences
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.DigiDoc.utilsLib.toast.ToastUtil
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.Arrays
import javax.inject.Inject

class DataStore
    @Inject
    constructor(application: Application) {
        @Suppress("PropertyName")
        private val LOG_TAG = javaClass.simpleName

        private var preferences: SharedPreferences
        private var resources: Resources

        init {
            preferences = PreferenceManager.getDefaultSharedPreferences(application)
            resources = application.resources
        }

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
            if (!Arrays.asList(*signatureAddMethods).contains(signatureAddMethod)) {
                return Route.MobileId.route
            }
            return signatureAddMethod
        }

        fun setSignatureAddMethod(method: String) {
            val editor = preferences.edit()
            editor.putString(resources.getString(R.string.main_settings_signature_add_method_key), method)
            editor.apply()
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

        fun getCountry(): String {
            return preferences.getString(
                resources.getString(R.string.main_settings_smartid_country_key),
                "EE",
            ) ?: "EE"
        }

        fun setCountry(country: String) {
            val editor = preferences.edit()
            editor.putString(
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
            return preferences.getString(resources.getString(R.string.main_settings_tsa_url_key), "") ?: ""
        }

        fun setSettingsTSAUrl(tsaUrl: String) {
            val editor = preferences.edit()
            editor.putString(resources.getString(R.string.main_settings_tsa_url_key), tsaUrl)
            editor.apply()
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
                errorLog(LOG_TAG, "Unable to get proxy setting", iae)
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

        fun setProxyPassword(
            context: Context,
            password: String,
        ) {
            val encryptedPreferences: SharedPreferences? = getEncryptedPreferences(context)
            if (encryptedPreferences != null) {
                val editor = encryptedPreferences.edit()
                editor.putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_password_key),
                    password,
                )
                editor.apply()
            }
            errorLog(LOG_TAG, "Unable to set proxy password")
        }

        fun getProxyPassword(context: Context): String {
            val encryptedPreferences: SharedPreferences? = getEncryptedPreferences(context)
            if (encryptedPreferences != null) {
                return encryptedPreferences.getString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_password_key),
                    "",
                ) ?: ""
            }
            errorLog(LOG_TAG, "Unable to get proxy password")
            return ""
        }

        fun getManualProxySettings(context: Context): ManualProxy {
            return ManualProxy(
                getProxyHost(),
                getProxyPort(),
                getProxyUsername(),
                getProxyPassword(context),
            )
        }

        private fun getEncryptedPreferences(context: Context): SharedPreferences? {
            return try {
                EncryptedPreferences.getEncryptedPreferences(context)
            } catch (e: GeneralSecurityException) {
                errorLog(LOG_TAG, "Unable to get encrypted preferences", e)
                ToastUtil.showMessage(context, R.string.signature_update_mobile_id_error_general_client)
                null
            } catch (e: IOException) {
                errorLog(LOG_TAG, "Unable to get encrypted preferences", e)
                ToastUtil.showMessage(context, R.string.signature_update_mobile_id_error_general_client)
                null
            }
        }
    }
