@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthParser
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthParserImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class WebEidAuthServiceTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var parser: WebEidAuthParser
    private lateinit var service: WebEidAuthService
    private val authCertBase64 =
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

    private val signingCertBase64 =
        "MIID6zCCA02gAwIBAgIQT7j6zk6pmVRcyspLo5SqejAKBggqhkjOPQQDBDBgMQswCQYD" +
            "VQQGEwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRhDA5OVFJF" +
            "RS0xMDc0NzAxMzEbMBkGA1UEAwwSVEVTVCBvZiBFU1RFSUQyMDE4MB4XDTE5MDUwMjEw" +
            "NDUzMVoXDTI5MDUwMjEwNDUzMVowfzELMAkGA1UEBhMCRUUxFjAUBgNVBCoMDUpBQUst" +
            "S1JJU1RKQU4xEDAOBgNVBAQMB0rDlUVPUkcxKjAoBgNVBAMMIUrDlUVPUkcsSkFBSy1L" +
            "UklTVEpBTiwzODAwMTA4NTcxODEaMBgGA1UEBRMRUE5PRUUtMzgwMDEwODU3MTgwdjAQ" +
            "BgcqhkjOPQIBBgUrgQQAIgNiAASkwENR8GmCpEs6OshDWDfIiKvGuyNMOD2rjIQW321A" +
            "nZD3oIsqD0svBMNEJJj9Dlvq/47TYDObIa12KAU5IuOBfJs2lrFdSXZjaM+a5TWT3O2J" +
            "TM36YDH2GcMe/eisepejggGrMIIBpzAJBgNVHRMEAjAAMA4GA1UdDwEB/wQEAwIGQDBI" +
            "BgNVHSAEQTA/MDIGCysGAQQBg5EhAQIBMCMwIQYIKwYBBQUHAgEWFWh0dHBzOi8vd3d3" +
            "LnNrLmVlL0NQUzAJBgcEAIvsQAECMB0GA1UdDgQWBBTVX3s48Spy/Es2TcXgkRvwUn2Y" +
            "cjCBigYIKwYBBQUHAQMEfjB8MAgGBgQAjkYBATAIBgYEAI5GAQQwEwYGBACORgEGMAkG" +
            "BwQAjkYBBgEwUQYGBACORgEFMEcwRRY/aHR0cHM6Ly9zay5lZS9lbi9yZXBvc2l0b3J5" +
            "L2NvbmRpdGlvbnMtZm9yLXVzZS1vZi1jZXJ0aWZpY2F0ZXMvEwJFTjAfBgNVHSMEGDAW" +
            "gBTAhJkpxE6fOwI09pnhClYACCk+ezBzBggrBgEFBQcBAQRnMGUwLAYIKwYBBQUHMAGG" +
            "IGh0dHA6Ly9haWEuZGVtby5zay5lZS9lc3RlaWQyMDE4MDUGCCsGAQUFBzAChilodHRw" +
            "Oi8vYy5zay5lZS9UZXN0X29mX0VTVEVJRDIwMTguZGVyLmNydDAKBggqhkjOPQQDBAOB" +
            "iwAwgYcCQgGBr+Jbo1GeqgWdIwgMo7SA29AP38JxNm2HWq2Qb+kIHpusAK574Co1K5D4" +
            "+Mk7/ITTuXQaET5WphHoN7tdAciTaQJBAn0zBigYyVPYSTO68HM6hmlwTwi/KlJDdXW/" +
            "2NsMjSqofFFJXpGvpxk2CTqSRCjcavxLPnkasTbNROYSJcmM8Xc="

    @Before
    fun setup() {
        parser = WebEidAuthParserImpl()
        service = WebEidAuthServiceImpl(parser)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun parseAuthUri_validUri_updatesAuthRequest() =
        runTest {
            val uri =
                createAuthUri(
                    challenge = "abc123",
                    loginUri = "https://rp.example.com/auth/eid/login",
                    getCert = true,
                )

            service.parseAuthUri(uri)

            val auth = service.authRequest.value
            assertEquals("abc123", auth?.challenge)
            assertEquals("https://rp.example.com/auth/eid/login", auth?.loginUri)
            assertEquals(true, auth?.getSigningCertificate)
            assertEquals("https://rp.example.com", auth?.origin)
            assertNull(service.errorState.value)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun parseSignUri_validUri_updatesSignRequest() =
        runTest {
            val uri =
                createSignUri(
                    responseUri = "https://rp.example.com/sign/ok",
                    signCert = "CERTDATA",
                    hash = "abcd1234",
                    hashFunc = "SHA-256",
                )

            service.parseSignUri(uri)

            val sign = service.signRequest.value
            assertEquals("https://rp.example.com/sign/ok", sign?.responseUri)
            assertEquals("CERTDATA", sign?.signCertificate)
            assertEquals("abcd1234", sign?.hash)
            assertEquals("SHA-256", sign?.hashFunction)
            assertNull(service.errorState.value)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun resetValues_clearsAllState() =
        runTest {
            val uri = createAuthUri("abc123", "https://rp.example.com", false)
            service.parseAuthUri(uri)

            service.resetValues()

            assertNull(service.authRequest.value)
            assertNull(service.signRequest.value)
            assertNull(service.errorState.value)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun parseAuthUri_invalidUri_setsErrorState() =
        runTest {
            val badUri = Uri.parse("web-eid://auth#not-base64!!!")

            service.parseAuthUri(badUri)

            assertNull(service.authRequest.value)
            assertNull(service.signRequest.value)
            assert(service.errorState.value != null)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun parseAuthUri_validUri_setsRedirectUriToSuccess() =
        runTest {
            val uri =
                createAuthUri(
                    challenge = "abc123",
                    loginUri = "https://rp.example.com/auth/eid/login",
                    getCert = false,
                )

            service.parseAuthUri(uri)

            val redirect = service.redirectUri.value
            assert(redirect != null)
            val decodedFragment =
                String(Base64.getUrlDecoder().decode(Uri.parse(redirect).fragment))
            assert(decodedFragment.contains("mock-web-eid-auth-token"))
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun parseAuthUri_invalidUri_setsRedirectUriToError() =
        runTest {
            val badUri = Uri.parse("web-eid://auth#not-base64!!!")

            service.parseAuthUri(badUri)

            val redirect = service.redirectUri.value
            assert(redirect != null)
            val decodedFragment =
                String(Base64.getUrlDecoder().decode(Uri.parse(redirect).fragment))
            assert(decodedFragment.contains("ERR_WEBEID_INVALID_REQUEST"))
        }

    @Test
    fun buildAuthToken_withValidInputs_returnsValidJson() {
        val authCertBytes = Base64.getDecoder().decode(authCertBase64)
        val signingCertBytes = Base64.getDecoder().decode(signingCertBase64)
        val signature = byteArrayOf(1, 2, 3, 4, 5)
        val challenge = "abc123"

        val token = service.buildAuthToken(authCertBytes, signingCertBytes, signature, challenge)

        assertEquals("web-eid:1.1", token.getString("format"))
        assertEquals(challenge, token.getString("challenge"))
        assert(token.getString("unverifiedCertificate").isNotBlank())
        assert(token.getString("unverifiedSigningCertificate").isNotBlank())
        assert(token.getString("signature").isNotBlank())
        assert(token.has("algorithm"))
        assert(token.has("supportedSignatureAlgorithms"))
        assertEquals(Base64.getEncoder().encodeToString(authCertBytes), token.getString("unverifiedCertificate"))
        assertEquals(
            Base64.getEncoder().encodeToString(signingCertBytes),
            token.getString("unverifiedSigningCertificate"),
        )
        assertNotEquals(
            token.getString("unverifiedCertificate"),
            token.getString("unverifiedSigningCertificate"),
            "Auth certificate and signing certificate should not be identical",
        )
    }

    @Suppress("SameParameterValue")
    private fun createAuthUri(
        challenge: String,
        loginUri: String,
        getCert: Boolean,
    ): Uri {
        val json =
            """
            {
              "challenge": "$challenge",
              "login_uri": "$loginUri",
              "get_signing_certificate": $getCert
            }
            """.trimIndent()
        val encoded = Base64.getEncoder().encodeToString(json.toByteArray())
        return Uri.parse("web-eid://auth#$encoded")
    }

    @Suppress("SameParameterValue")
    private fun createSignUri(
        responseUri: String,
        signCert: String,
        hash: String,
        hashFunc: String,
    ): Uri {
        val json =
            """
            {
              "response_uri": "$responseUri",
              "sign_certificate": "$signCert",
              "hash": "$hash",
              "hash_function": "$hashFunc"
            }
            """.trimIndent()
        val encoded = Base64.getEncoder().encodeToString(json.toByteArray())
        return Uri.parse("web-eid://sign#$encoded")
    }
}
