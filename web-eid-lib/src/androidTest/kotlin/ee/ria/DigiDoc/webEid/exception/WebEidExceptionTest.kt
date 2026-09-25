// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.exception

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebEidExceptionTest {
    @Test
    fun constructor_and_getters_workCorrectly() {
        val exception =
            WebEidException(
                WebEidErrorCode.ERR_WEBEID_MOBILE_INVALID_REQUEST,
                "Test message",
                "https://example.com/error",
            )

        assertEquals(WebEidErrorCode.ERR_WEBEID_MOBILE_INVALID_REQUEST, exception.errorCode)
        assertEquals("Test message", exception.message)
        assertEquals("https://example.com/error", exception.responseUri)
        assertNotNull(exception.localizedMessage)
    }
}
