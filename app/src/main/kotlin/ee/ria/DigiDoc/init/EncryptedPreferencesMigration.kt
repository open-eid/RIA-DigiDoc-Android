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
