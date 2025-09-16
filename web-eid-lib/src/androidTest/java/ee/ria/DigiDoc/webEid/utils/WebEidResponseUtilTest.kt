@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.utils

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class WebEidResponseUtilTest {
    @Test
    fun createRedirect_withCustomPayload_encodesAndAppendsCorrectly() {
        val loginUri = "https://rp.example.com/auth/eid/login"
        val payload =
            JSONObject()
                .put("code", "ERR_CUSTOM")
                .put("message", "Custom error message")

        val resultUri = WebEidResponseUtil.createRedirect(loginUri, payload)

        val fragment = Uri.parse(resultUri).fragment
        val decodedJson = String(android.util.Base64.decode(fragment, android.util.Base64.URL_SAFE))
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

        val resultUri = WebEidResponseUtil.createRedirect(loginUri, payload)

        val fragment = Uri.parse(resultUri).fragment
        val decodedJson = String(android.util.Base64.decode(fragment, android.util.Base64.URL_SAFE))
        val json = JSONObject(decodedJson)

        assertEquals("sample-token", json.getString("auth-token"))
        assertEquals("abc123", json.getString("challenge"))
    }

    @Test
    fun appendFragment_keepsBaseUriIntact() {
        val loginUri = "https://rp.example.com/auth/eid/login"
        val payload = JSONObject().put("foo", "bar")

        val resultUri = WebEidResponseUtil.createRedirect(loginUri, payload)

        assertTrue(resultUri.startsWith(loginUri))
    }
}
