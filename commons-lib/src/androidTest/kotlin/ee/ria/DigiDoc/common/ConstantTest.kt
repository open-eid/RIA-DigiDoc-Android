@file:Suppress("PackageName")

package ee.ria.DigiDoc.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class ConstantTest {
    @Test
    fun testSignatureRequestConstants() {
        assertEquals("time-stamp", Constant.SignatureRequest.SIGNATURE_PROFILE_TS)
        assertEquals("+372", Constant.SignatureRequest.PLUS_PREFIXED_ESTONIAN_PHONE_CODE)
        assertEquals("5", Constant.SignatureRequest.FIRST_NUMBER_IN_ESTONIAN_MOBILE_NUMBER)
        assertEquals(5, Constant.SignatureRequest.MAXIMUM_INITIALIZATION_COUNT)
        assertEquals(40, Constant.SignatureRequest.MAX_DISPLAY_MESSAGE_BYTES)
        assertEquals(200, Constant.SignatureRequest.MAX_DISPLAY_MESSAGE_LENGTH)
        assertEquals("ENG", Constant.SignatureRequest.DEFAULT_LANGUAGE)
        assertTrue(Constant.SignatureRequest.SUPPORTED_LANGUAGES.containsAll(setOf("ENG", "EST", "RUS", "LIT")))
        assertEquals("SHA256", Constant.SignatureRequest.DIGEST_TYPE)
        assertEquals("RIA DigiDoc", Constant.SignatureRequest.RELYING_PARTY_NAME)
        assertEquals("00000000-0000-0000-0000-000000000000", Constant.SignatureRequest.RELYING_PARTY_UUID)
        assertEquals("GSM-7", Constant.SignatureRequest.DISPLAY_TEXT_FORMAT)
        assertEquals("UCS-2", Constant.SignatureRequest.ALTERNATIVE_DISPLAY_TEXT_FORMAT)
    }

    @Test
    fun testNFCConstants() {
        assertEquals(4, Constant.NFCConstants.PIN1_MIN_LENGTH)
        assertEquals(5, Constant.NFCConstants.PIN2_MIN_LENGTH)
        assertEquals(12, Constant.NFCConstants.PIN_MAX_LENGTH)
        assertEquals(8, Constant.NFCConstants.PUK_MIN_LENGTH)
        assertEquals(6, Constant.NFCConstants.CAN_LENGTH)
    }

    @Test
    fun testSmartIdConstantsValuesNotNull() {
        assertTrue(Constant.SmartIdConstants.SID_BROADCAST_ACTION.isNotEmpty())
        assertTrue(Constant.SmartIdConstants.SIGNING_ROLE_DATA.isNotEmpty())
    }

    @Test
    fun testContainerExtensions() {
        assertTrue(Constant.CONTAINER_EXTENSIONS.contains("asice"))
        assertTrue(Constant.ALL_CONTAINER_EXTENSIONS.contains("cdoc"))
        assertTrue(Constant.NON_LEGACY_CONTAINER_EXTENSIONS.contains("bdoc"))
        assertTrue(Constant.CRYPTO_CONTAINER_EXTENSIONS.contains("cdoc2"))
    }

    @Test
    fun testFilenameRestrictions() {
        assertTrue(Constant.RESTRICTED_FILENAME_CHARACTERS_AND_RTL_CHARACTERS_AS_STRING.contains('@'))
        assertTrue(Constant.RESTRICTED_FILENAME_CHARACTERS_AND_RTL_CHARACTERS_AS_STRING.contains('\u202E'))
    }

    @Test
    fun testMimeTypes() {
        assertTrue(Constant.SIGNATURE_CONTAINER_MIMETYPES.contains("application/vnd.etsi.asic-e+zip"))
        assertTrue(Constant.NO_REMOVE_SIGNATURE_BUTTON_FILE_EXTENSIONS.contains("adoc"))
        assertTrue(Constant.UNSIGNABLE_CONTAINER_MIMETYPES.contains("application/vnd.lt.archyvai.adoc-2008"))
    }

    @Test
    fun testDdocStampedDate() {
        val expected = ZonedDateTime.of(2018, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        assertEquals(expected, Constant.DDOC_STAMPED_VALID_UNTIL_DATE)
    }

    @Test
    fun testAllowedPhoneCodes() {
        assertTrue(Constant.ALLOWED_PHONE_NUMBER_COUNTRY_CODES.contains("372"))
        assertEquals(10, Constant.MINIMUM_PHONE_NUMBER_LENGTH)
    }

    @Test
    fun testThemeSettingKey() {
        assertEquals("THEME_SETTING", Constant.Theme.THEME_SETTING)
    }

    @Test
    fun testMyEIDPinLimits() {
        assertEquals(4, Constant.MyEID.PIN1_MINIMUM_LENGTH)
        assertEquals(12, Constant.MyEID.PIN_MAXIMUM_LENGTH)
    }

    @Test
    fun testCryptoSettings() {
        assertEquals("DECRYPT_METHOD_SETTING", Constant.Crypto.DECRYPT_METHOD_SETTING)
    }
}
