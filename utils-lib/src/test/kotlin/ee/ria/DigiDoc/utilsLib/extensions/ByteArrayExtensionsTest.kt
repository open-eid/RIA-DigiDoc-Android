@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.cert.X509Certificate
import java.util.Base64

class ByteArrayExtensionsTest {
    @Test
    fun byteArrayExtensions_hexString_success() {
        val byteArray =
            byteArrayOf(
                0x01,
                0x23,
                0x45,
                0x67,
                0x89.toByte(),
                0xAB.toByte(),
                0xCD.toByte(),
                0xEF.toByte(),
            )
        val expectedHexString = "01 23 45 67 89 AB CD EF"

        val result = byteArray.hexString().uppercase()

        assertEquals(expectedHexString, result)
    }

    @Test
    fun byteArrayExtensions_x509Certificate_success() {
        val certBase64 =
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
        val certBytes = Base64.getDecoder().decode(certBase64)

        val result = certBytes.x509Certificate()

        assertNotNull(result)
        assertTrue(result is X509Certificate)
    }

    @Test
    fun byteArrayExtensions_x509Certificate_returnNullWithInvalidCertificate() {
        val invalidCertBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)

        val result = invalidCertBytes.x509Certificate()

        assertNull(result)
    }
}
