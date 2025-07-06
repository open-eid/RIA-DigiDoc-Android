@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.preferences

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.preferences.EncryptedPreferences.getEncryptedPreferences
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class EncryptedPreferencesTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun encryptedPreferences_getEncryptedPreferences_success() {
        val result = getEncryptedPreferences(context)

        assertNotNull(result)
    }
}
