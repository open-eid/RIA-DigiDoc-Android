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
