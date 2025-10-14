@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class WebEidAuthServiceTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: WebEidAuthService
    private val authCertBase64 =
        """
        MIIECTCCA4+gAwIBAgIUN2tgxiz6MdXE3QfegLIoan8ZNW0wCgYIKoZIzj0EAwMwXDEYMBYGA1UEAwwPVGVzdCBFU1RFSUQyMDI1MRcwFQYDVQRh
        DA5OVFJFRS0xNzA2NjA0OTEaMBgGA1UECgwRWmV0ZXMgRXN0b25pYSBPw5wxCzAJBgNVBAYTAkVFMB4XDTI0MTIxODEwMjYxMloXDTI5MTIwOTIw
        NTkxMlowfzEqMCgGA1UEAwwhSsOVRU9SRyxKQUFLLUtSSVNUSkFOLDM4MDAxMDg1NzE4MRowGAYDVQQFExFQTk9FRS0zODAwMTA4NTcxODEWMBQG
        A1UEKgwNSkFBSy1LUklTVEpBTjEQMA4GA1UEBAwHSsOVRU9SRzELMAkGA1UEBhMCRUUwdjAQBgcqhkjOPQIBBgUrgQQAIgNiAARCqN9WLBaVniOO
        qXCKa5yzvlXZNNfmTxxhduZX/81iNvB6BRDJEyyRgKMyn/32NuKUUxa+JqExAvT534kOOTQVPOcp/e2X5NUc+qCw1qsNcsMs60C7FSxzoyvZ+HIt
        /oajggHtMIIB6TAJBgNVHRMEAjAAMB8GA1UdIwQYMBaAFO7ylT+MsvxRnoTm5l6EEX5CuiA2MHAGCCsGAQUFBwEBBGQwYjA4BggrBgEFBQcwAoYs
        aHR0cDovL2NydC10ZXN0LmVpZHBraS5lZS90ZXN0RVNURUlEMjAyNS5jcnQwJgYIKwYBBQUHMAGGGmh0dHA6Ly9vY3NwLXRlc3QuZWlkcGtpLmVl
        MB8GA1UdEQQYMBaBFDM4MDAxMDg1NzE4QGVlc3RpLmVlMFYGA1UdIARPME0wCAYGBACPegECMEEGDog3AQMGAQQBg5EhAgEBMC8wLQYIKwYBBQUH
        AgEWIWh0dHBzOi8vcmVwb3NpdG9yeS10ZXN0LmVpZHBraS5lZTAdBgNVHSUEFjAUBggrBgEFBQcDAgYIKwYBBQUHAwQwQwYIKwYBBQUHAQMENzA1
        MDMGBgQAjkYBBTApMCcWIWh0dHBzOi8vcmVwb3NpdG9yeS10ZXN0LmVpZHBraS5lZRMCZW4wPQYDVR0fBDYwNDAyoDCgLoYsaHR0cDovL2NybC10
        ZXN0LmVpZHBraS5lZS90ZXN0RVNURUlEMjAyNS5jcmwwHQYDVR0OBBYEFIl1MYmBknWP4qF6QZmMHHVO4pnTMA4GA1UdDwEB/wQEAwIDiDAKBggq
        hkjOPQQDAwNoADBlAjAk2dWjje4yKfESIYN2fU0vQM7+8BOyOD4qHdwSnh+XqphWXGEDIra6FgS4mY/uu0oCMQC4Hg18SnB6oy6dL4vEMFyTyx2F
        iaiMnWMYd1/TyTQvUzvT2jmEA1a7DrALs0Pt3aA=
        """.trimIndent()

    private val signingCertBase64 =
        """
        MIID8zCCA3mgAwIBAgIUeHSVTuHxrs0ASYMbqOjDX5yFVnswCgYIKoZIzj0EAwMwXDEYMBYGA1UEAwwPVGVzdCBFU1RFSUQyMDI1MRcwFQYDVQRh
        DA5OVFJFRS0xNzA2NjA0OTEaMBgGA1UECgwRWmV0ZXMgRXN0b25pYSBPw5wxCzAJBgNVBAYTAkVFMB4XDTI0MTIxODEwMjY0MVoXDTI5MTIwOTIw
        NTk0MVowfzEqMCgGA1UEAwwhSsOVRU9SRyxKQUFLLUtSSVNUSkFOLDM4MDAxMDg1NzE4MRowGAYDVQQFExFQTk9FRS0zODAwMTA4NTcxODEWMBQG
        A1UEKgwNSkFBSy1LUklTVEpBTjEQMA4GA1UEBAwHSsOVRU9SRzELMAkGA1UEBhMCRUUwdjAQBgcqhkjOPQIBBgUrgQQAIgNiAAR9DpcXt4J2NwqG
        B3pS1RcGlBM7tcoG82OGpLwCr4xn9LZgc5QRk/oGmRoJ6Nk9/BbHgoYYvBXW8xzcTNZwKIxwz7FRI9cFF+4+4i/ywqkRV9ApH112xQ7L+p9ANCP/
        va6jggHXMIIB0zAJBgNVHRMEAjAAMB8GA1UdIwQYMBaAFO7ylT+MsvxRnoTm5l6EEX5CuiA2MHAGCCsGAQUFBwEBBGQwYjA4BggrBgEFBQcwAoYs
        aHR0cDovL2NydC10ZXN0LmVpZHBraS5lZS90ZXN0RVNURUlEMjAyNS5jcnQwJgYIKwYBBQUHMAGGGmh0dHA6Ly9vY3NwLXRlc3QuZWlkcGtpLmVl
        MFcGA1UdIARQME4wCQYHBACL7EABAjBBBg6INwEDBgEEAYORIQIBATAvMC0GCCsGAQUFBwIBFiFodHRwczovL3JlcG9zaXRvcnktdGVzdC5laWRw
        a2kuZWUwbAYIKwYBBQUHAQMEYDBeMAgGBgQAjkYBATAIBgYEAI5GAQQwEwYGBACORgEGMAkGBwQAjkYBBgEwMwYGBACORgEFMCkwJxYhaHR0cHM6
        Ly9yZXBvc2l0b3J5LXRlc3QuZWlkcGtpLmVlEwJlbjA9BgNVHR8ENjA0MDKgMKAuhixodHRwOi8vY3JsLXRlc3QuZWlkcGtpLmVlL3Rlc3RFU1RF
        SUQyMDI1LmNybDAdBgNVHQ4EFgQUH6IlbFh9H8w0BIsDCgq01rqaFVUwDgYDVR0PAQH/BAQDAgZAMAoGCCqGSM49BAMDA2gAMGUCMQDGeR+QV6MF
        sWnB7LoXrpOfPQFTT366CLbdmQQMbIzJtysZTrOSQ95yxpulvpxOKsoCMAsT41AJ3de5JSrW89S5x5zgvi1K7PG1zhzSGgUuMElzDZPJSyp4TE8k
        FvCDizwjaQ==
        """.trimIndent()

    @Before
    fun setup() {
        service = WebEidAuthServiceImpl()
    }

    @Test
    fun buildAuthToken_withValidInputs_returnsValidJson() {
        val authCertBytes = Base64.getMimeDecoder().decode(authCertBase64)
        val signingCertBytes = Base64.getMimeDecoder().decode(signingCertBase64)
        val signature = byteArrayOf(1, 2, 3, 4, 5)

        val token = service.buildAuthToken(authCertBytes, signingCertBytes, signature)

        assertEquals("web-eid:1.1", token.getString("format"))
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

    @Test
    fun buildAuthToken_withoutSigningCertificate_returnsV1Format() {
        val authCertBytes = Base64.getMimeDecoder().decode(authCertBase64)
        val signature = byteArrayOf(1, 2, 3, 4, 5)

        val token = service.buildAuthToken(authCertBytes, null, signature)

        assertEquals("web-eid:1.0", token.getString("format"))
        assert(token.getString("unverifiedCertificate").isNotBlank())
        assert(token.getString("signature").isNotBlank())
        assertFalse(token.has("unverifiedSigningCertificate"))
        assertFalse(token.has("supportedSignatureAlgorithms"))
    }
}
