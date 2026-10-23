// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.provider

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConfigurationProviderTest {
    @Test
    fun configurationProvider_deserialize_readsRiaddUnsupported() {
        val configuration =
            Gson().fromJson(
                """{"RIADD-UNSUPPORTED":"3.2.0.100","QDIGIDOC4-UNSUPPORTED":"4.8.0.0"}""",
                ConfigurationProvider::class.java,
            )

        assertEquals("3.2.0.100", configuration.riaddUnsupported)
    }

    @Test
    fun configurationProvider_deserialize_riaddUnsupportedIsNullWhenMissing() {
        val configuration =
            Gson().fromJson(
                """{"QDIGIDOC4-UNSUPPORTED":"4.8.0.0"}""",
                ConfigurationProvider::class.java,
            )

        assertNull(configuration.riaddUnsupported)
    }
}
