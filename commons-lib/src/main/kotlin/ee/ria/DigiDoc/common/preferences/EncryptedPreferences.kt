@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.IOException
import java.security.GeneralSecurityException

object EncryptedPreferences {
    private const val ENCRYPTED_PREFERENCES_KEY = "encryptedPreferencesStorage"

    @Throws(IOException::class, GeneralSecurityException::class)
    fun getEncryptedPreferences(context: Context): SharedPreferences {
        val masterKey: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            ENCRYPTED_PREFERENCES_KEY,
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }
}
