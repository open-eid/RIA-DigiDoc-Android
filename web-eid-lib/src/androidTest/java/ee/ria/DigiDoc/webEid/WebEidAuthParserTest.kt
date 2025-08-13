@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthParser
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthParserImpl
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
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
    private lateinit var parser: WebEidAuthParser
    private val cert =
        "MIIDuzCCAqOgAwIBAgIUBkYXJdruP6EuH/+I4YoXxIQ3WcowDQYJKoZIhvcNAQELBQAw" +
            "bTELMAkGA1UEBhMCRUUxDTALBgNVBAgMBFRlc3QxDTALBgNVBAcMBFRlc3QxDTALBgNV" +
            "BAoMBFRlc3QxDTALBgNVBAsMBFRlc3QxDTALBgNVBAMMBFRlc3QxEzARBgkqhkiG9w0B" +
            "CQEWBHRlc3QwHhcNMjQwNjEwMTI1OTA3WhcNMjUwNjEwMTI1OTA3WjBtMQswCQYDVQQG" +
            "EwJFRTENMAsGA1UECAwEVGVzdDENMAsGA1UEBwwEVGVzdDENMAsGA1UECgwEVGVzdDEN" +
            "MAsGA1UECwwEVGVzdDENMAsGA1UEAwwEVGVzdDETMBEGCSqGSIb3DQEJARYEdGVzdDCC" +
            "ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANNQx56UkGcNvUrEsdzqhn94nHb3" +
            "X8oa1+JUWLHE9KUe2ZiNaIMjMOEuMKtss3tKHHBwLig0by24cwySNozoL156i9a5J8VX" +
            "zkuEr0dKlkGm13BnSBVY+gdRB47oh1ZocSewyyJmWetLiOzgRq4xkYLuV/xP+lmum580" +
            "MomZcwB06/C42FWIlkPqQF4NFTT1mXjHCzl5uY3OZN9+2KGPa5/QOS9ZI3ixp9TiS8oI" +
            "Y7VskIk6tUJcnSF3pN6cI+EkS5zODV3Cs33S2Z3mskC3uBTZQxua75NUxycB5wvg4jbf" +
            "GcKOaA9QhHmaloNDwXcw7v9hTwg/xe148mt+D5wABl8CAwEAAaNTMFEwHQYDVR0OBBYE" +
            "FCM1tdnw9XYxBNieiNJ8liORKwlpMB8GA1UdIwQYMBaAFCM1tdnw9XYxBNieiNJ8liOR" +
            "KwlpMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBALmgdhGrkMLsc/g" +
            "n2BsaFx3S7fHaO3MEV0krghH9TMk+M1y0oghAjotm/bGqOmZ4x/Hv08YputTMLTK2qpa" +
            "Xtf0Q75V7tOr29jpL10lFALuhNtjRt/Ha5mV4qYGDk+vT8+Rw7SzeVhhSr1pM/MmjN3c" +
            "AKDZbI0RINIXarZCb2j963eCfguxXZJbxzW09S6kZ/bDEOwi4PLwE0kln9NqQW6JEBHY" +
            "kDeYQonkKm1VrZklb1obq+g1UIJkTOAXQdJDyvfHWyKzKE8cUHGxYUvlxOL/YCyLkUGa" +
            "eE/VmJs0niWtKlX4UURG0HAGjZIQ/pJejV+7GzknFMZmuiwJQe4yT4mw="

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        parser = WebEidAuthParserImpl()
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
    fun parseAuthUri_invalidScheme_throwsException() {
        val loginUri = "http://rp.example.com/auth/eid/login"
        val uri = android.net.Uri.parse(createAuthUri("abc1234", loginUri, false))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                parser.parseAuthUri(uri)
            }
        assertEquals("login_uri must use HTTPS", exception.message)
    }

    @Test
    fun parseAuthUri_detectsUserInfoPhishing() {
        val loginUri = "https://rp.example.com:pass@evil.example.com/auth/eid/login"
        val uri = android.net.Uri.parse(createAuthUri("abc1235", loginUri, false))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                parser.parseAuthUri(uri)
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
                parser.parseAuthUri(uri)
            }
        assertTrue(exception.message!!.contains("Invalid URI fragment format"))
    }

    @Test
    fun buildAuthToken_returnsExpectedJsonStructure() {
        val certBytes = Base64.getDecoder().decode(cert)
        val signature = byteArrayOf(1, 2, 3, 4, 5)
        val challenge = "abc123"

        val token = parser.buildAuthToken(certBytes, signature, challenge)

        assertEquals("web-eid:1.1", token.getString("format"))
        assertTrue(token.getString("unverifiedCertificate").isNotEmpty())
        assertTrue(token.getString("unverifiedSigningCertificate").isNotEmpty())
        assertEquals(challenge, token.getString("challenge"))
        assertTrue(token.getString("signature").isNotEmpty())
        assertTrue(token.has("algorithm"))
        assertTrue(token.has("supportedSignatureAlgorithms"))
    }
}
