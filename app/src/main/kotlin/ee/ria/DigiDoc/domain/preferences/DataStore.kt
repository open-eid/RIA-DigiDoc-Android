/*
 * Copyright 2017 - 2025 Riigi Infosüsteemi Amet
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

package ee.ria.DigiDoc.domain.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.Crypto.DECRYPT_METHOD_SETTING
import ee.ria.DigiDoc.common.Constant.Defaults.DEFAULT_UUID_VALUE
import ee.ria.DigiDoc.common.Constant.IS_CRASH_SENDING_ALWAYS_ENABLED
import ee.ria.DigiDoc.common.Constant.KEY_LOCALE
import ee.ria.DigiDoc.common.Constant.MyEID.IDENTIFICATION_METHOD_SETTING
import ee.ria.DigiDoc.common.Constant.Theme.THEME_SETTING
import ee.ria.DigiDoc.common.preferences.EncryptedPreferences
import ee.ria.DigiDoc.domain.model.crypto.DecryptMethodSetting
import ee.ria.DigiDoc.domain.model.methods.SigningMethod
import ee.ria.DigiDoc.domain.model.myeid.MyEidIdentificationMethodSetting
import ee.ria.DigiDoc.domain.model.settings.CDOCSetting
import ee.ria.DigiDoc.domain.model.settings.TSASetting
import ee.ria.DigiDoc.domain.model.settings.UUIDSetting
import ee.ria.DigiDoc.domain.model.theme.ThemeSetting
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.siva.SivaSetting
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utilsLib.locale.LocaleUtil.getLocale
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.libdigidocpp.digidoc
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.Locale
import javax.inject.Inject

class DataStore
    @Inject
    constructor(
        private var context: Context,
    ) {
        private val logTag = javaClass.simpleName

        private var preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        private var resources: Resources = context.resources

        fun getSignatureAddMethod(): String {
            val signatureAddMethod =
                preferences.getString(
                    resources.getString(R.string.main_settings_signature_add_method_key),
                    SigningMethod.NFC.methodName,
                ) ?: SigningMethod.NFC.methodName
            val signatureAddMethods =
                arrayOf(
                    SigningMethod.MOBILE_ID.methodName,
                    SigningMethod.SMART_ID.methodName,
                    SigningMethod.ID_CARD.methodName,
                    SigningMethod.NFC.methodName,
                )
            if (!listOf(*signatureAddMethods).contains(signatureAddMethod)) {
                return SigningMethod.NFC.methodName
            }
            return signatureAddMethod
        }

        fun setSignatureAddMethod(method: String) {
            preferences.edit {
                putString(
                    resources.getString(R.string.main_settings_signature_add_method_key),
                    method,
                )
            }
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
                encryptedPreferences.edit {
                    putString(resources.getString(R.string.main_settings_can_key), can)
                }
                return
            }
            errorLog(logTag, "Unable to save CAN")
        }

        fun getSigningCertificate(): String {
            val encryptedPrefs = getEncryptedPreferences(context)
            if (encryptedPrefs == null) {
                errorLog(logTag, "Unable to read signing certificate")
                return ""
            }

            val currentCan = getCanNumber()
            val key = "${resources.getString(R.string.main_settings_signing_cert_key)}_$currentCan"
            return encryptedPrefs.getString(key, "") ?: ""
        }

        @SuppressLint("ApplySharedPref")
        fun setSigningCertificate(cert: String) {
            val encryptedPrefs = getEncryptedPreferences(context)
            if (encryptedPrefs == null) {
                errorLog(logTag, "Unable to save signing certificate")
                return
            }

            val currentCanNumber = getCanNumber()
            val key = "${resources.getString(R.string.main_settings_signing_cert_key)}_$currentCanNumber"
            val editor = encryptedPrefs.edit()

            editor.remove(key).commit()
            if (cert.isNotEmpty()) editor.putString(key, cert).commit()
        }

        fun getPhoneNo(): String =
            preferences.getString(
                resources.getString(R.string.main_settings_phone_no_key),
                "372",
            ) ?: "372"

        fun setPhoneNo(phoneNo: String) {
            preferences.edit {
                putString(resources.getString(R.string.main_settings_phone_no_key), phoneNo)
            }
        }

        fun getPersonalCode(): String =
            preferences.getString(
                resources.getString(R.string.main_settings_personal_code_key),
                "",
            ) ?: ""

        fun setPersonalCode(personalCode: String) {
            preferences.edit {
                putString(
                    resources.getString(R.string.main_settings_personal_code_key),
                    personalCode,
                )
            }
        }

        fun getSidPersonalCode(): String =
            preferences.getString(
                resources.getString(R.string.main_settings_sid_personal_code_key),
                "",
            ) ?: ""

        fun setSidPersonalCode(personalCode: String) {
            preferences.edit {
                putString(
                    resources.getString(R.string.main_settings_sid_personal_code_key),
                    personalCode,
                )
            }
        }

        fun getCountry(): Int =
            preferences.getInt(
                resources.getString(R.string.main_settings_smartid_country_key),
                0,
            )

        fun setCountry(country: Int) {
            preferences.edit {
                putInt(
                    resources.getString(R.string.main_settings_smartid_country_key),
                    country,
                )
            }
        }

        fun getSettingsUUID(): String =
            preferences.getString(
                resources.getString(R.string.main_settings_uuid_key),
                DEFAULT_UUID_VALUE,
            ) ?: DEFAULT_UUID_VALUE

        fun setSettingsUUID(uuid: String) {
            preferences.edit {
                putString(resources.getString(R.string.main_settings_uuid_key), uuid)
            }
        }

        fun getSettingsTSAUrl(): String =
            preferences.getString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_url_key),
                "",
            )
                ?: ""

        fun setSettingsTSAUrl(tsaUrl: String) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_url_key),
                    tsaUrl,
                )
            }
        }

        fun getIsLogFileGenerationEnabled(): Boolean =
            preferences.getBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_key),
                false,
            )

        fun setIsLogFileGenerationEnabled(isEnabled: Boolean) {
            preferences.edit {
                putBoolean(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_key),
                    isEnabled,
                )
            }
        }

        fun getIsLogFileGenerationRunning(): Boolean =
            preferences.getBoolean(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                false,
            )

        fun setIsLogFileGenerationRunning(isRunning: Boolean) {
            preferences.edit {
                putBoolean(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
                    isRunning,
                )
            }
        }

        fun getSettingsSivaUrl(): String =
            preferences.getString(
                resources.getString(
                    ee.ria.DigiDoc.network.R.string.main_settings_siva_url_key,
                ),
                "",
            )
                ?: ""

        fun setSettingsSivaUrl(sivaUrl: String) {
            preferences.edit {
                putString(
                    resources.getString(
                        ee.ria.DigiDoc.network.R.string.main_settings_siva_url_key,
                    ),
                    sivaUrl,
                )
            }
        }

        fun getSettingsSivaCertName(): String =
            preferences.getString(
                resources.getString(
                    ee.ria.DigiDoc.network.R.string.main_settings_siva_cert_key,
                ),
                "",
            )
                ?: ""

        fun setSettingsSivaCertName(cert: String?) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_siva_cert_key),
                    cert,
                )
            }
        }

        fun setSivaSetting(sivaSetting: SivaSetting) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_siva_setting_key),
                    sivaSetting.name,
                )
            }
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

        fun setTsaSetting(tsaSetting: TSASetting) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_setting_key),
                    tsaSetting.name,
                )
            }
        }

        fun getTsaSetting(): TSASetting {
            val tsaSetting =
                preferences.getString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_setting_key),
                    TSASetting.DEFAULT.name,
                )
            try {
                return tsaSetting?.let { TSASetting.valueOf(it) } ?: TSASetting.DEFAULT
            } catch (iae: IllegalArgumentException) {
                debugLog(logTag, "Unable to get TSA setting value", iae)
                return TSASetting.DEFAULT
            }
        }

        fun setUuidSetting(uuidSetting: UUIDSetting) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_uuid_setting_key),
                    uuidSetting.name,
                )
            }
        }

        fun getUuidSetting(): UUIDSetting {
            val uuidSetting =
                preferences.getString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_uuid_setting_key),
                    UUIDSetting.DEFAULT.name,
                )
            try {
                return uuidSetting?.let { UUIDSetting.valueOf(it) } ?: UUIDSetting.DEFAULT
            } catch (iae: IllegalArgumentException) {
                debugLog(logTag, "Unable to get UUID setting value", iae)
                return UUIDSetting.DEFAULT
            }
        }

        fun getUseEncryption(defaultValue: Boolean): Boolean =
            preferences.getBoolean(
                resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_encryption),
                defaultValue,
            )

        fun setUseEncryption(useEncryption: Boolean) {
            preferences.edit {
                putBoolean(
                    resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_encryption),
                    useEncryption,
                )
            }
        }

        fun getUseOnlineEncryption(defaultValue: Boolean): Boolean =
            preferences.getBoolean(
                resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_online_encryption),
                defaultValue,
            )

        fun setUseOnlineEncryption(useOnlineEncryption: Boolean) {
            preferences.edit {
                putBoolean(
                    resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_online_encryption),
                    useOnlineEncryption,
                )
            }
        }

        fun getCDOC2SelectedService(defaultValue: String): String =
            preferences.getString(
                resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_selected_service),
                defaultValue,
            ) ?: defaultValue

        fun setCDOC2SelectedService(selectedService: String) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_selected_service),
                    selectedService,
                )
            }
        }

        fun getCDOC2UUID(defaultValue: String): String =
            preferences.getString(
                resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_uuid),
                defaultValue,
            ) ?: defaultValue

        fun setCDOC2UUID(uuid: String) {
            preferences.edit {
                putString(resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_uuid), uuid)
            }
        }

        fun getCDOC2PostURL(defaultValue: String): String =
            preferences.getString(
                resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_post_url),
                defaultValue,
            ) ?: defaultValue

        fun setCDOC2PostURL(postUrl: String) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_post_url),
                    postUrl,
                )
            }
        }

        fun getCDOC2FetchURL(defaultValue: String): String =
            preferences.getString(
                resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_fetch_url),
                defaultValue,
            ) ?: defaultValue

        fun setCDOC2FetchURL(fetchUrl: String) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.cryptolib.R.string.crypto_settings_use_cdoc2_fetch_url),
                    fetchUrl,
                )
            }
        }

        fun setCdocSetting(cdocSetting: CDOCSetting) {
            if (cdocSetting == CDOCSetting.CDOC2) {
                setUseEncryption(true)
            } else {
                setUseEncryption(false)
            }
        }

        fun getCdocSetting(defaultValue: Boolean): CDOCSetting {
            if (getUseEncryption(defaultValue)) {
                return CDOCSetting.CDOC2
            }
            return CDOCSetting.CDOC1
        }

        fun getSettingsAskRoleAndAddress(): Boolean =
            preferences.getBoolean(
                resources.getString(R.string.main_settings_ask_role_and_address_key),
                false,
            )

        fun setSettingsAskRoleAndAddress(isRoleAskingEnabled: Boolean) {
            preferences.edit {
                putBoolean(
                    resources.getString(R.string.main_settings_ask_role_and_address_key),
                    isRoleAskingEnabled,
                )
            }
        }

        fun getRoles(): String =
            preferences.getString(
                resources.getString(R.string.main_settings_role_key),
                "",
            ) ?: ""

        fun setRoles(roles: String?) {
            preferences.edit {
                putString(
                    resources.getString(R.string.main_settings_role_key),
                    roles,
                )
            }
        }

        fun getRoleCity(): String =
            preferences.getString(
                resources.getString(R.string.main_settings_city_key),
                "",
            ) ?: ""

        fun setRoleCity(city: String?) {
            preferences.edit {
                putString(
                    resources.getString(R.string.main_settings_city_key),
                    city,
                )
            }
        }

        fun getRoleState(): String =
            preferences.getString(
                resources.getString(R.string.main_settings_county_key),
                "",
            ) ?: ""

        fun setRoleState(state: String?) {
            preferences.edit {
                putString(
                    resources.getString(R.string.main_settings_county_key),
                    state,
                )
            }
        }

        fun getRoleCountry(): String =
            preferences.getString(
                resources.getString(R.string.main_settings_country_key),
                "",
            ) ?: ""

        fun setRoleCountry(country: String?) {
            preferences.edit {
                putString(
                    resources.getString(R.string.main_settings_country_key),
                    country,
                )
            }
        }

        fun getRoleZip(): String =
            preferences.getString(
                resources.getString(R.string.main_settings_postal_code_key),
                "",
            ) ?: ""

        fun setRoleZip(zip: String?) {
            preferences.edit {
                putString(
                    resources.getString(R.string.main_settings_postal_code_key),
                    zip,
                )
            }
        }

        fun setTSACertName(cert: String?) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_cert_key),
                    cert,
                )
            }
        }

        fun getTSACertName(): String =
            preferences.getString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_tsa_cert_key),
                "",
            )
                ?: ""

        fun setCryptoCertName(cert: String?) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_crypto_cert_key),
                    cert,
                )
            }
        }

        fun getCryptoCertName(): String =
            preferences.getString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_crypto_cert_key),
                "",
            )
                ?: ""

        fun setIsTsaCertificateViewVisible(isVisible: Boolean) {
            preferences.edit {
                putBoolean(resources.getString(R.string.main_settings_tsa_cert_view), isVisible)
            }
        }

        fun getIsTsaCertificateViewVisible(): Boolean =
            preferences.getBoolean(
                resources.getString(R.string.main_settings_tsa_cert_view),
                false,
            )

        fun getSettingsOpenAllFileTypes(): Boolean =
            preferences.getBoolean(
                resources.getString(R.string.main_settings_open_all_filetypes_key),
                true,
            )

        fun setSettingsOpenAllFileTypes(isEnabled: Boolean) {
            preferences.edit {
                putBoolean(
                    resources.getString(R.string.main_settings_open_all_filetypes_key),
                    isEnabled,
                )
            }
        }

        fun getSettingsAllowScreenshots(): Boolean =
            preferences.getBoolean(
                resources.getString(R.string.main_settings_allow_screenshots_key),
                false,
            )

        fun setSettingsAllowScreenshots(isEnabled: Boolean) {
            preferences.edit {
                putBoolean(
                    resources.getString(R.string.main_settings_allow_screenshots_key),
                    isEnabled,
                )
            }
        }

        fun setProxySetting(proxySetting: ProxySetting) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_setting_key),
                    proxySetting.name,
                )
            }
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
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_host_key),
                    host,
                )
            }
        }

        fun getProxyHost(): String =
            preferences.getString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_host_key),
                "",
            )
                ?: ""

        fun setProxyPort(port: Int) {
            preferences.edit {
                putInt(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_port_key),
                    port,
                )
            }
        }

        fun getProxyPort(): Int =
            preferences.getInt(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_port_key),
                80,
            )

        fun isValidPortNumber(portNumber: String): Boolean {
            try {
                val number = portNumber.toInt()
                return number in 1..65535
            } catch (e: NumberFormatException) {
                errorLog(logTag, "Invalid number: $portNumber", e)
                return false
            }
        }

        fun setProxyUsername(username: String) {
            preferences.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_username_key),
                    username,
                )
            }
        }

        fun getProxyUsername(): String =
            preferences.getString(
                resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_username_key),
                "",
            ) ?: ""

        fun setProxyPassword(password: String) {
            getEncryptedPreferences(context)?.edit {
                putString(
                    resources.getString(ee.ria.DigiDoc.network.R.string.main_settings_proxy_password_key),
                    password,
                )
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
                return getLocale(locale)
            }
            return null
        }

        fun setLocale(locale: Locale?) {
            if (locale == null) {
                preferences.edit { remove(KEY_LOCALE) }
            } else {
                preferences.edit { putString(KEY_LOCALE, locale.language) }
            }
        }

        fun getLibdigidocppVersion(): String = digidoc.version()

        fun getManualProxySettings(): ManualProxy =
            ManualProxy(
                getProxyHost(),
                getProxyPort(),
                getProxyUsername(),
                getProxyPassword(),
            )

        fun getIsCrashSendingAlwaysEnabled(): Boolean = preferences.getBoolean(IS_CRASH_SENDING_ALWAYS_ENABLED, false)

        fun setIsCrashSendingAlwaysEnabled(isEnabled: Boolean) {
            preferences.edit { putBoolean(IS_CRASH_SENDING_ALWAYS_ENABLED, isEnabled) }
        }

        fun getThemeSetting(): ThemeSetting =
            ThemeSetting.fromMode(
                preferences.getString(THEME_SETTING, ThemeSetting.SYSTEM.mode) ?: ThemeSetting.SYSTEM.mode,
            )

        fun setThemeSetting(themeSetting: ThemeSetting) {
            preferences.edit { putString(THEME_SETTING, themeSetting.mode) }
        }

        fun getDecryptMethodSetting(): DecryptMethodSetting =
            DecryptMethodSetting.fromMethod(
                preferences.getString(DECRYPT_METHOD_SETTING, DecryptMethodSetting.NFC.methodName)
                    ?: DecryptMethodSetting.NFC.methodName,
            )

        fun setDecryptMethodSetting(decryptMethodSetting: DecryptMethodSetting) {
            preferences.edit { putString(DECRYPT_METHOD_SETTING, decryptMethodSetting.methodName) }
        }

        fun getIdentificationMethodSetting(): MyEidIdentificationMethodSetting =
            MyEidIdentificationMethodSetting.fromMethod(
                preferences.getString(IDENTIFICATION_METHOD_SETTING, MyEidIdentificationMethodSetting.NFC.methodName)
                    ?: MyEidIdentificationMethodSetting.NFC.methodName,
            )

        fun setIdentificationMethodSetting(myEidIdentificationMethodSetting: MyEidIdentificationMethodSetting) {
            preferences.edit { putString(IDENTIFICATION_METHOD_SETTING, myEidIdentificationMethodSetting.methodName) }
        }

        private fun getEncryptedPreferences(context: Context): SharedPreferences? =
            try {
                EncryptedPreferences.getEncryptedPreferences(context)
            } catch (e: GeneralSecurityException) {
                errorLog(logTag, "Unable to get encrypted preferences", e)
                showMessage(context, R.string.error_general_client)
                null
            } catch (e: IOException) {
                errorLog(logTag, "Unable to get encrypted preferences", e)
                showMessage(context, R.string.error_general_client)
                null
            }
    }
