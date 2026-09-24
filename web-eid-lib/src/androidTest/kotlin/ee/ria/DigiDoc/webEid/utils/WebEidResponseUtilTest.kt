// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.utils

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import ee.ria.DigiDoc.webEid.exception.WebEidErrorCode
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebEidResponseUtilTest {
    @Test
    fun createRedirect_withCustomPayload_encodesAndAppendsCorrectly() {
        val loginUri = "https://rp.example.com/auth/eid/login"
        val payload =
            JSONObject()
                .put("code", "ERR_CUSTOM")
                .put("message", "Custom error message")

        val resultUri = WebEidResponseUtil.createResponseUri(loginUri, payload)

        val fragment = resultUri.fragment
        val decodedJson = String(Base64.decode(fragment, Base64.URL_SAFE))
        val json = JSONObject(decodedJson)

        assertEquals("ERR_CUSTOM", json.getString("code"))
        assertEquals("Custom error message", json.getString("message"))
    }

    @Test
    fun createRedirect_withSuccessPayload_encodesAndAppendsCorrectly() {
        val loginUri = "https://rp.example.com/auth/eid/login"
        val payload =
            JSONObject()
                .put("auth-token", "sample-token")
                .put("challenge", "abc123")

        val resultUri = WebEidResponseUtil.createResponseUri(loginUri, payload)

        val fragment = resultUri.fragment
        val decodedJson = String(Base64.decode(fragment, Base64.URL_SAFE))
        val json = JSONObject(decodedJson)

        assertEquals("sample-token", json.getString("auth-token"))
        assertEquals("abc123", json.getString("challenge"))
    }

    @Test
    fun appendFragment_keepsBaseUriIntact() {
        val loginUri = "https://rp.example.com/auth/eid/login"
        val payload = JSONObject().put("foo", "bar")

        val resultUri = WebEidResponseUtil.createResponseUri(loginUri, payload)

        assertTrue(resultUri.toString().startsWith(loginUri))
    }

    @Test
    fun createErrorPayload_and_createResponseUri_areCovered() {
        val loginUri = "https://rp.example.com/auth/eid/login"

        val errorPayload =
            WebEidResponseUtil.createErrorPayload(
                WebEidErrorCode.ERR_WEBEID_MOBILE_INVALID_REQUEST,
                "Invalid request",
            )

        val resultUri = WebEidResponseUtil.createResponseUri(loginUri, errorPayload)
        val decodedJson = String(Base64.decode(resultUri.fragment, Base64.URL_SAFE))
        val json = JSONObject(decodedJson)

        assertTrue(json.getBoolean("error"))
        assertEquals("Invalid request", json.getString("message"))
    }
}
