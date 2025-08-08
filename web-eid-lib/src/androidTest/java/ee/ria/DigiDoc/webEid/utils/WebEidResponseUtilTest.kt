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
    fun createErrorRedirect_withCustomCodeAndMessage_encodesCorrectly() {
        val loginUri = "https://rp.example.com/auth/eid/login"
        val code = "ERR_CUSTOM"
        val message = "Custom error message"

        val resultUri = WebEidResponseUtil.createErrorRedirect(loginUri, code, message)

        val fragment = Uri.parse(resultUri).fragment
        val decodedJson = String(Base64.getUrlDecoder().decode(fragment))
        val json = JSONObject(decodedJson)

        assertEquals(code, json.getString("code"))
        assertEquals(message, json.getString("message"))
    }

    @Test
    fun createErrorRedirect_withDefaults_usesUnknownErrorValues() {
        val loginUri = "https://rp.example.com/auth/eid/login"

        val resultUri = WebEidResponseUtil.createErrorRedirect(loginUri)

        val fragment = Uri.parse(resultUri).fragment
        val decodedJson = String(Base64.getUrlDecoder().decode(fragment))
        val json = JSONObject(decodedJson)

        assertEquals(WebEidErrorCodes.UNKNOWN, json.getString("code"))
        assertEquals(WebEidErrorCodes.UNKNOWN_MESSAGE, json.getString("message"))
    }

    @Test
    fun createSuccessRedirect_containsMockSuccessPayload() {
        val loginUri = "https://rp.example.com/auth/eid/login"

        val resultUri = WebEidResponseUtil.createSuccessRedirect(loginUri)

        val fragment = Uri.parse(resultUri).fragment
        val decodedJson = String(Base64.getUrlDecoder().decode(fragment))
        val json = JSONObject(decodedJson)

        assertEquals("mock-web-eid-auth-token", json.getString("web_eid_auth_token"))
        assertEquals("mock-attestation", json.getString("eid_instance_attestation"))
        assertEquals("mock-attestation-proof", json.getString("eid_instance_attestation_proof"))
    }

    @Test
    fun appendedFragment_keepsBaseUriIntact() {
        val loginUri = "https://rp.example.com/auth/eid/login"
        val resultUri = WebEidResponseUtil.createSuccessRedirect(loginUri)

        assertTrue(resultUri.startsWith(loginUri))
    }
}
