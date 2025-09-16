@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.utils.WebEidAuthParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
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

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun parseAuthUri_httpsOriginIsValid() {
        val loginUri = "https://rp.example.com/auth/eid/login"
        val uri = android.net.Uri.parse(createAuthUri("abc123", loginUri, true))
        val result: WebEidAuthRequest = WebEidAuthParser.parseAuthUri(uri)

        assertEquals("abc123", result.challenge)
        assertEquals(loginUri, result.loginUri)
        assertEquals(true, result.getSigningCertificate)
        assertTrue(result.origin.startsWith("https://rp.example.com"))
    }

    @Test
    fun parseAuthUri_invalidScheme_throwsException() {
        val loginUri = "http://rp.example.com/auth/eid/login"
        val uri = android.net.Uri.parse(createAuthUri("abc1234", loginUri, false))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidAuthParser.parseAuthUri(uri)
            }
        assertEquals("login_uri must use HTTPS", exception.message)
    }

    @Test
    fun parseAuthUri_detectsUserInfoPhishing() {
        val loginUri = "https://rp.example.com:pass@evil.example.com/auth/eid/login"
        val uri = android.net.Uri.parse(createAuthUri("abc1235", loginUri, false))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidAuthParser.parseAuthUri(uri)
            }
        assertTrue(exception.message!!.contains("Login URI contains userinfo"))
    }

    private fun createAuthUri(
        challenge: String,
        loginUri: String,
        getCert: Boolean,
    ): String {
        val json =
            """
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
    fun parseAuthUri_invalidBase64_throwsException() {
        val uri = android.net.Uri.parse("web-eid://auth#%%%INVALID%%%")
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidAuthParser.parseAuthUri(uri)
            }
        assertTrue(exception.message!!.contains("Invalid URI fragment format"))
    }
}
