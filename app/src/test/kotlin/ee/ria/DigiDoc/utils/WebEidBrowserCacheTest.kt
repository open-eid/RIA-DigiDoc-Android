// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
