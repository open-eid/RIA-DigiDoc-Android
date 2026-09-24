// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebEidUriUtilTest {
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
        assertTrue(WebEidUriUtil.isWebEidUri(Uri.parse("https://id-test.eesti.ee/auth")))
    }

    @Test
    fun isWebEidUri_appLinks_cert() {
        assertTrue(WebEidUriUtil.isWebEidUri(Uri.parse("https://id-test.eesti.ee/cert")))
    }

    @Test
    fun isWebEidUri_appLinks_sign() {
        assertTrue(WebEidUriUtil.isWebEidUri(Uri.parse("https://id-test.eesti.ee/sign")))
    }

    @Test
    fun isWebEidUri_appLinks_unknownOperation() {
        assertFalse(WebEidUriUtil.isWebEidUri(Uri.parse("https://id-test.eesti.ee/unknown")))
    }

    @Test
    fun isWebEidUri_wrongHost() {
        assertFalse(WebEidUriUtil.isWebEidUri(Uri.parse("https://evil.com/auth")))
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
            WebEidUriUtil.getOperation(Uri.parse("https://id-test.eesti.ee/auth#dGVzdA")),
        )
    }

    @Test
    fun getOperation_appLinks_cert() {
        assertEquals(
            WebEidOperation.CERT,
            WebEidUriUtil.getOperation(Uri.parse("https://id-test.eesti.ee/cert#dGVzdA")),
        )
    }

    @Test
    fun getOperation_appLinks_sign() {
        assertEquals(
            WebEidOperation.SIGN,
            WebEidUriUtil.getOperation(Uri.parse("https://id-test.eesti.ee/sign#dGVzdA")),
        )
    }

    @Test
    fun getOperation_unknownOperation_returnsNull() {
        assertNull(WebEidUriUtil.getOperation(Uri.parse("web-eid-mobile://unknown")))
    }

    @Test
    fun getOperation_appLinks_unknownOperation_returnsNull() {
        assertNull(WebEidUriUtil.getOperation(Uri.parse("https://id-test.eesti.ee/unknown")))
    }

    @Test
    fun getOperation_unrelatedUri_returnsNull() {
        assertNull(WebEidUriUtil.getOperation(Uri.parse("https://example.com/auth")))
    }
}
