@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthParser
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class WebEidAuthParserTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var parser: WebEidAuthParser

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        parser = WebEidAuthParser()
    }

    private fun createAuthUri(challenge: String, loginUri: String, getCert: Boolean): String {
        val json = """
            {
              "challenge": "$challenge",
              "login_uri": "$loginUri",
              "get_signing_certificate": $getCert
            }
        """.trimIndent()
        val encoded = Base64.getEncoder().encodeToString(json.toByteArray())
        return "web-eid://auth#$encoded"
    }

    @Test
    fun parseAuthUri_httpsOriginIsValid() {
        val loginUri = "https://rp.example.com/auth/eid/login"
        val uri = android.net.Uri.parse(createAuthUri("abc123", loginUri, true))
        val result: WebEidAuthRequest = parser.parseAuthUri(uri)

        assertEquals("abc123", result.challenge)
        assertEquals(loginUri, result.loginUri)
        assertEquals(true, result.getSigningCertificate)
        assertTrue(result.origin.startsWith("https://rp.example.com"))
    }

    @Test
    fun parseAuthUri_invalidSchemeLogsError() {
        val loginUri = "http://rp.example.com/auth/eid/login"
        val uri = android.net.Uri.parse(createAuthUri("abc1234", loginUri, false))
        val result = parser.parseAuthUri(uri)

        assertEquals("http://rp.example.com/auth/eid/login", result.loginUri)
        assertTrue(result.origin.contains("rp.example.com"))
    }

    @Test
    fun parseAuthUri_detectsUserInfoPhishing() {
        val loginUri = "https://rp.example.com:pass@evil.example.com/auth/eid/login"
        val uri = android.net.Uri.parse(createAuthUri("abc1235", loginUri, false))
        val result = parser.parseAuthUri(uri)

        assertTrue(result.origin.contains("evil.example.com"))
    }

    @Test
    fun parseAuthUri_invalidUriFormatReturnsEmptyOrigin() {
        val loginUri = "not-a-valid-uri"
        val uri = android.net.Uri.parse(createAuthUri("abc1236", loginUri, false))
        val result = parser.parseAuthUri(uri)

        assertEquals("", result.origin)
    }
}
