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

import android.net.Uri
import ee.ria.DigiDoc.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebEidUriUtilTest {
    private val appLinkHostUri = "https://${BuildConfig.APP_LINKS_HOST}"

    @Test
    fun isWebEidUri_customScheme_auth() {
        assertTrue(WebEidUriUtil.isWebEidUri(Uri.parse("web-eid-mobile://auth")))
    }

    @Test
    fun isWebEidUri_customScheme_cert() {
        assertTrue(WebEidUriUtil.isWebEidUri(Uri.parse("web-eid-mobile://cert")))
    }

    @Test
    fun isWebEidUri_customScheme_sign() {
        assertTrue(WebEidUriUtil.isWebEidUri(Uri.parse("web-eid-mobile://sign")))
    }

    @Test
    fun isWebEidUri_appLinks_auth() {
        assertTrue(WebEidUriUtil.isWebEidUri(Uri.parse("$appLinkHostUri/auth")))
    }

    @Test
    fun isWebEidUri_appLinks_cert() {
        assertTrue(WebEidUriUtil.isWebEidUri(Uri.parse("$appLinkHostUri/cert")))
    }

    @Test
    fun isWebEidUri_appLinks_sign() {
        assertTrue(WebEidUriUtil.isWebEidUri(Uri.parse("$appLinkHostUri/sign")))
    }

    @Test
    fun isWebEidUri_appLinks_unknownOperation() {
        assertFalse(WebEidUriUtil.isWebEidUri(Uri.parse("$appLinkHostUri/unknown")))
    }

    @Test
    fun isWebEidUri_wrongHost() {
        assertFalse(WebEidUriUtil.isWebEidUri(Uri.parse("https://example.org/auth")))
    }

    @Test
    fun isWebEidUri_contentScheme() {
        assertFalse(WebEidUriUtil.isWebEidUri(Uri.parse("content://some/path")))
    }

    @Test
    fun isWebEidUri_fileScheme() {
        assertFalse(WebEidUriUtil.isWebEidUri(Uri.parse("file:///some/path")))
    }

    @Test
    fun isWebEidUri_customScheme_unknownOperation() {
        assertFalse(WebEidUriUtil.isWebEidUri(Uri.parse("web-eid-mobile://unknown")))
    }

    @Test
    fun browserPackageCandidate_applicationIdPreferredOverReferrer() {
        assertEquals(
            "com.example.browser",
            WebEidUriUtil.browserPackageCandidate(
                "com.example.browser",
                Uri.parse("android-app://com.example.otherbrowser"),
            ),
        )
    }

    @Test
    fun browserPackageCandidate_emptyApplicationIdFallsBackToReferrer() {
        assertEquals(
            "com.example.otherbrowser",
            WebEidUriUtil.browserPackageCandidate("", Uri.parse("android-app://com.example.otherbrowser")),
        )
    }

    @Test
    fun browserPackageCandidate_androidAppReferrer() {
        assertEquals(
            "com.example.otherbrowser",
            WebEidUriUtil.browserPackageCandidate(null, Uri.parse("android-app://com.example.otherbrowser")),
        )
    }

    @Test
    fun browserPackageCandidate_httpsReferrerIsNotAPackage() {
        assertNull(WebEidUriUtil.browserPackageCandidate(null, Uri.parse("https://example.com/auth")))
    }

    @Test
    fun browserPackageCandidate_referrerWithoutAuthority() {
        assertNull(WebEidUriUtil.browserPackageCandidate(null, Uri.parse("android-app://")))
    }

    @Test
    fun browserPackageCandidate_noApplicationIdAndNoReferrer() {
        assertNull(WebEidUriUtil.browserPackageCandidate(null, null))
    }

    @Test
    fun getOperation_customScheme_auth() {
        assertEquals(WebEidOperation.AUTH, WebEidUriUtil.getOperation(Uri.parse("web-eid-mobile://auth#dGVzdA")))
    }

    @Test
    fun getOperation_customScheme_cert() {
        assertEquals(WebEidOperation.CERT, WebEidUriUtil.getOperation(Uri.parse("web-eid-mobile://cert#dGVzdA")))
    }

    @Test
    fun getOperation_customScheme_sign() {
        assertEquals(WebEidOperation.SIGN, WebEidUriUtil.getOperation(Uri.parse("web-eid-mobile://sign#dGVzdA")))
    }

    @Test
    fun getOperation_appLinks_auth() {
        assertEquals(
            WebEidOperation.AUTH,
            WebEidUriUtil.getOperation(Uri.parse("$appLinkHostUri/auth#dGVzdA")),
        )
    }

    @Test
    fun getOperation_appLinks_cert() {
        assertEquals(
            WebEidOperation.CERT,
            WebEidUriUtil.getOperation(Uri.parse("$appLinkHostUri/cert#dGVzdA")),
        )
    }

    @Test
    fun getOperation_appLinks_sign() {
        assertEquals(
            WebEidOperation.SIGN,
            WebEidUriUtil.getOperation(Uri.parse("$appLinkHostUri/sign#dGVzdA")),
        )
    }

    @Test
    fun getOperation_unknownOperation_returnsNull() {
        assertNull(WebEidUriUtil.getOperation(Uri.parse("web-eid-mobile://unknown")))
    }

    @Test
    fun getOperation_appLinks_unknownOperation_returnsNull() {
        assertNull(WebEidUriUtil.getOperation(Uri.parse("$appLinkHostUri/unknown")))
    }

    @Test
    fun getOperation_unrelatedUri_returnsNull() {
        assertNull(WebEidUriUtil.getOperation(Uri.parse("https://example.com/auth")))
    }
}
