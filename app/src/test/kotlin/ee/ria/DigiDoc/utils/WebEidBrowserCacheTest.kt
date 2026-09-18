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

package ee.ria.DigiDoc.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WebEidBrowserCacheTest {
    private val resolvedTtlMillis = 30 * 1000L
    private val selectedTtlMillis = 5 * 60 * 1000L
    private val origin = "https://example.com"

    @Test
    fun recall_returnsResolvedPackageWithinTtl() {
        WebEidBrowserCache.rememberResolved(origin, "com.example.browser", now = 1_000L)

        assertEquals(
            "com.example.browser",
            WebEidBrowserCache.recall(origin, now = 1_000L + resolvedTtlMillis),
        )
    }

    @Test
    fun recall_returnsNullAfterResolvedTtl() {
        WebEidBrowserCache.rememberResolved(origin, "com.example.browser", now = 1_000L)

        assertNull(WebEidBrowserCache.recall(origin, now = 1_000L + resolvedTtlMillis + 1))
    }

    @Test
    fun recall_returnsSelectedPackageAfterResolvedTtl() {
        WebEidBrowserCache.rememberSelected(origin, "com.example.browser", now = 1_000L)

        assertEquals(
            "com.example.browser",
            WebEidBrowserCache.recall(origin, now = 1_000L + resolvedTtlMillis + 1),
        )
    }

    @Test
    fun recall_returnsNullAfterSelectedTtl() {
        WebEidBrowserCache.rememberSelected(origin, "com.example.browser", now = 1_000L)

        assertNull(WebEidBrowserCache.recall(origin, now = 1_000L + selectedTtlMillis + 1))
    }

    @Test
    fun recall_returnsNullOnSecondCall() {
        WebEidBrowserCache.rememberSelected(origin, "com.example.browser", now = 1_000L)

        assertEquals("com.example.browser", WebEidBrowserCache.recall(origin, now = 1_000L))
        assertNull(WebEidBrowserCache.recall(origin, now = 1_000L))
    }

    @Test
    fun recall_returnsNullForAnotherOrigin() {
        WebEidBrowserCache.rememberSelected(origin, "com.example.browser", now = 1_000L)

        assertNull(WebEidBrowserCache.recall("https://other.example", now = 1_000L))
    }

    @Test
    fun recall_returnsLatestRememberedPackage() {
        WebEidBrowserCache.rememberSelected(origin, "com.example.browser", now = 1_000L)
        WebEidBrowserCache.rememberResolved(origin, "com.example.otherbrowser", now = 2_000L)

        assertEquals("com.example.otherbrowser", WebEidBrowserCache.recall(origin, now = 2_000L))
    }
}
