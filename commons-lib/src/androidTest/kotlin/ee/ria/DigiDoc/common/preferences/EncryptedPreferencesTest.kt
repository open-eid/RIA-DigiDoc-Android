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
import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EncryptedPreferencesTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        EncryptedPreferences.clear(context)
        context
            .getSharedPreferences("encryptedPrefsMigration", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    @Test
    fun encryptedPreferences_putString_success() {
        EncryptedPreferences.putString(context, "test_key", "test_value")
        assertEquals("test_value", EncryptedPreferences.getString(context, "test_key"))
    }

    @Test
    fun encryptedPreferences_putString_successOverwritingExistingKey() {
        EncryptedPreferences.putString(context, "key", "first")
        EncryptedPreferences.putString(context, "key", "second")
        assertEquals("second", EncryptedPreferences.getString(context, "key"))
    }

    @Test
    fun encryptedPreferences_putString_successWithEmptyString() {
        EncryptedPreferences.putString(context, "key", "")
        assertEquals("", EncryptedPreferences.getString(context, "key", "default"))
    }

    @Test
    fun encryptedPreferences_putString_successWithMultipleKeys() {
        EncryptedPreferences.putString(context, "key_a", "value_a")
        EncryptedPreferences.putString(context, "key_b", "value_b")
        assertEquals("value_a", EncryptedPreferences.getString(context, "key_a"))
        assertEquals("value_b", EncryptedPreferences.getString(context, "key_b"))
    }

    @Test
    fun encryptedPreferences_getString_missingKeyReturnsCallerDefault() {
        assertEquals("default", EncryptedPreferences.getString(context, "missing_key", "default"))
    }

    @Test
    fun encryptedPreferences_getString_missingKeyReturnsEmptyStringByDefault() {
        assertEquals("", EncryptedPreferences.getString(context, "missing_key"))
    }

    @Test
    fun encryptedPreferences_clear_removesStoredValues() {
        EncryptedPreferences.putString(context, "key", "value")
        EncryptedPreferences.clear(context)
        assertEquals("default", EncryptedPreferences.getString(context, "key", "default"))
    }

    @Test
    fun encryptedPreferences_getString_returnsDefaultValueOnTamperedCiphertext() {
        EncryptedPreferences.putString(context, "tamper_key", "real_value")
        val underlying = context.getSharedPreferences("encryptedPreferencesStorageV2", Context.MODE_PRIVATE)
        val obfuscatedKey = underlying.all.keys.first()
        val fakeData = ByteArray(32) { it.toByte() }
        underlying.edit().putString(obfuscatedKey, Base64.encodeToString(fakeData, Base64.NO_WRAP)).apply()

        assertEquals("default", EncryptedPreferences.getString(context, "tamper_key", "default"))
    }

    @Test
    fun encryptedPreferences_isMigrated_returnsFalseInitially() {
        assertFalse(EncryptedPreferences.isMigrated(context))
    }

    @Test
    fun encryptedPreferences_setMigrated_success() {
        EncryptedPreferences.setMigrated(context)
        assertTrue(EncryptedPreferences.isMigrated(context))
    }

    @Test
    fun encryptedPreferences_clear_doesNotAffectMigrationFlag() {
        EncryptedPreferences.setMigrated(context)
        EncryptedPreferences.clear(context)
        assertTrue(EncryptedPreferences.isMigrated(context))
    }
}
