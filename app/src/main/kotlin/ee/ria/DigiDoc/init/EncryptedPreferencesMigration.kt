// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.init

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import ee.ria.DigiDoc.common.preferences.EncryptedPreferences
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import java.io.File

object EncryptedPreferencesMigration {
    private const val OLD_PREFS_NAME = "encryptedPreferencesStorage"

    // Keys that were stored in the old EncryptedSharedPreferences
    private const val KEY_CAN = "can"
    private const val KEY_PROXY_PASSWORD = "main_settings_proxy_password"

    @Suppress("DEPRECATION")
    fun migrate(context: Context) {
        if (EncryptedPreferences.isMigrated(context)) return

        val oldPrefsFile = File(context.filesDir.parent, "shared_prefs/$OLD_PREFS_NAME.xml")
        if (!oldPrefsFile.exists()) {
            EncryptedPreferences.setMigrated(context)
            return
        }

        try {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val oldPrefs =
                EncryptedSharedPreferences.create(
                    OLD_PREFS_NAME,
                    masterKey,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )

            val can = oldPrefs.getString(KEY_CAN, null)
            val proxyPassword = oldPrefs.getString(KEY_PROXY_PASSWORD, null)

            if (!can.isNullOrEmpty()) {
                EncryptedPreferences.putString(context, KEY_CAN, can)
            }
            if (!proxyPassword.isNullOrEmpty()) {
                EncryptedPreferences.putString(context, KEY_PROXY_PASSWORD, proxyPassword)
            }

            context.deleteSharedPreferences(OLD_PREFS_NAME)
            EncryptedPreferences.setMigrated(context)
        } catch (e: Exception) {
            errorLog("EncryptedPrefsMigration", "Failed to migrate encrypted preferences", e)
        }
    }
}
