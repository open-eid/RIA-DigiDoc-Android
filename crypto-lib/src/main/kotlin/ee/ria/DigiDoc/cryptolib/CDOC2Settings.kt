@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import javax.inject.Inject

class CDOC2Settings
    @Inject
    constructor(
        private var context: Context,
    ) {
        private val logTag = javaClass.simpleName
        private var preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        private var resources: Resources = context.resources

        fun getUseEncryption(): Boolean {
            return preferences.getBoolean(
                resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                false,
            )
        }

        fun setUseEncryption(useEncryption: Boolean) {
            preferences.edit {
                putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_encryption),
                    useEncryption,
                )
            }
        }

        fun getUseOnlineEncryption(): Boolean {
            return preferences.getBoolean(
                resources.getString(R.string.crypto_settings_use_cdoc2_online_encryption),
                false,
            )
        }

        fun setUseOnlineEncryption(useOnlineEncryption: Boolean) {
            preferences.edit {
                putBoolean(
                    resources.getString(R.string.crypto_settings_use_cdoc2_online_encryption),
                    useOnlineEncryption,
                )
            }
        }

        fun getCDOC2SelectedService(): String {
            return preferences.getString(
                resources.getString(R.string.crypto_settings_use_cdoc2_selected_service),
                "",
            ) ?: ""
        }

        fun setCDOC2SelectedService(selectedService: String) {
            preferences.edit {
                putString(
                    resources.getString(R.string.crypto_settings_use_cdoc2_selected_service),
                    selectedService,
                )
            }
        }

        fun getCDOC2UUID(): String {
            return preferences.getString(
                resources.getString(R.string.crypto_settings_use_cdoc2_uuid),
                "",
            ) ?: ""
        }

        fun setCDOC2UUID(uuid: String) {
            preferences.edit {
                putString(resources.getString(R.string.crypto_settings_use_cdoc2_uuid), uuid)
            }
        }

        fun getCDOC2PostURL(): String {
            return preferences.getString(
                resources.getString(R.string.crypto_settings_use_cdoc2_post_url),
                "",
            ) ?: ""
        }

        fun setCDOC2PostURL(postUrl: String) {
            preferences.edit {
                putString(resources.getString(R.string.crypto_settings_use_cdoc2_post_url), postUrl)
            }
        }

        fun getCDOC2FetchURL(): String {
            return preferences.getString(
                resources.getString(R.string.crypto_settings_use_cdoc2_fetch_url),
                "",
            ) ?: ""
        }

        fun setCDOC2FetchURL(fetchUrl: String) {
            preferences.edit {
                putString(
                    resources.getString(R.string.crypto_settings_use_cdoc2_fetch_url),
                    fetchUrl,
                )
            }
        }
    }
