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

package ee.ria.DigiDoc.common.preferences

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object EncryptedPreferences {
    private const val PREFS_NAME = "encryptedPreferencesStorageV2"
    private const val ENC_KEY_ALIAS = "digiDocEncryptedPrefsKey"
    private const val HMAC_KEY_ALIAS = "digiDocEncryptedPrefsHmacKey"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val MIGRATION_PREFS_NAME = "encryptedPrefsMigration"
    private const val MIGRATION_DONE_KEY = "migrationDone"

    fun isMigrated(context: Context): Boolean =
        context
            .getSharedPreferences(MIGRATION_PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(MIGRATION_DONE_KEY, false)

    fun setMigrated(context: Context) {
        context
            .getSharedPreferences(MIGRATION_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(MIGRATION_DONE_KEY, true)
            .apply()
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun getString(
        context: Context,
        key: String,
        default: String = "",
    ): String {
        ensureKeysExist()
        val obfuscatedKey = obfuscateKey(key)
        val encoded = prefs(context).getString(obfuscatedKey, null) ?: return default
        return runCatching { decrypt(encoded) }
            .onFailure { Log.e("EncryptedPreferences", "Decryption failed for key $key", it) }
            .getOrDefault(default)
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun putString(
        context: Context,
        key: String,
        value: String,
    ) {
        ensureKeysExist()
        val obfuscatedKey = obfuscateKey(key)
        prefs(context).edit().putString(obfuscatedKey, encrypt(value)).apply()
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun ensureKeysExist() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(ENC_KEY_ALIAS)) {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
                init(
                    KeyGenParameterSpec
                        .Builder(
                            ENC_KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build(),
                )
                generateKey()
            }
        }
        if (!keyStore.containsAlias(HMAC_KEY_ALIAS)) {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, ANDROID_KEYSTORE).apply {
                init(
                    KeyGenParameterSpec
                        .Builder(
                            HMAC_KEY_ALIAS,
                            KeyProperties.PURPOSE_SIGN,
                        ).setDigests(KeyProperties.DIGEST_SHA256)
                        .build(),
                )
                generateKey()
            }
        }
    }

    private fun secretKey(): SecretKey =
        KeyStore.getInstance(ANDROID_KEYSTORE).run {
            load(null)
            getKey(ENC_KEY_ALIAS, null) as SecretKey
        }

    private fun hmacKey(): SecretKey =
        KeyStore.getInstance(ANDROID_KEYSTORE).run {
            load(null)
            getKey(HMAC_KEY_ALIAS, null) as SecretKey
        }

    private fun obfuscateKey(key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(hmacKey())
        val hmac = mac.doFinal(key.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hmac, Base64.NO_WRAP)
    }

    private fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey()) }
        val combined = cipher.iv + cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val data = Base64.decode(encoded, Base64.NO_WRAP)
        val cipher =
            Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_LENGTH, data, 0, GCM_IV_LENGTH))
            }
        return String(cipher.doFinal(data, GCM_IV_LENGTH, data.size - GCM_IV_LENGTH), Charsets.UTF_8)
    }
}
