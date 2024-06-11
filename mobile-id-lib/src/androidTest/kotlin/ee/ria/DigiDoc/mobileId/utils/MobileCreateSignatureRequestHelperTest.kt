@file:Suppress("PackageName")

package ee.ria.DigiDoc.mobileId.utils

import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.Constant.SignatureRequest.ALTERNATIVE_DISPLAY_TEXT_FORMAT
import ee.ria.DigiDoc.common.Constant.SignatureRequest.DEFAULT_LANGUAGE
import ee.ria.DigiDoc.common.Constant.SignatureRequest.DIGEST_TYPE
import ee.ria.DigiDoc.common.Constant.SignatureRequest.DISPLAY_TEXT_FORMAT
import ee.ria.DigiDoc.common.Constant.SignatureRequest.RELYING_PARTY_NAME
import ee.ria.DigiDoc.common.Constant.SignatureRequest.RELYING_PARTY_UUID
import ee.ria.DigiDoc.common.test.AssetFile
import ee.ria.DigiDoc.libdigidoclib.SignedContainer.Companion.openOrCreate
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.util.Locale

class MobileCreateSignatureRequestHelperTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    Initialization.init(InstrumentationRegistry.getInstrumentation().targetContext)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Test
    fun mobileCreateSignatureRequestHelperTest_create_uuidAndLocaleNull() =
        runTest {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer = openOrCreate(context, container, listOf(container))

            val proxyUrl = "proxyUrl"
            val skUrl = "skUrl"
            val personalCode = "1234567890"
            val phoneNo = "1234567890"
            val displayMessage = "displayMessage"

            val request =
                MobileCreateSignatureRequestHelper.create(
                    signedContainer,
                    null,
                    proxyUrl,
                    skUrl,
                    null,
                    personalCode,
                    phoneNo,
                    displayMessage,
                )

            assertEquals(RELYING_PARTY_NAME, request.relyingPartyName)
            assertEquals(RELYING_PARTY_UUID, request.relyingPartyUUID)
            assertEquals(proxyUrl, request.url)
            assertEquals("+$phoneNo", request.phoneNumber)
            assertEquals(personalCode, request.nationalIdentityNumber)
            assertEquals(signedContainer.getContainerFile()?.path, request.containerPath)
            assertEquals(DIGEST_TYPE, request.hashType)
            assertEquals(DEFAULT_LANGUAGE, request.language)
            assertEquals(displayMessage, request.displayText)
            assertEquals(DISPLAY_TEXT_FORMAT, request.displayTextFormat)
        }

    @Test
    fun mobileCreateSignatureRequestHelperTest_create_success() =
        runTest {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer = openOrCreate(context, container, listOf(container))

            val uuid = ""
            val proxyUrl = "proxyUrl"
            val skUrl = "skUrl"
            val locale = Locale("zz")
            val personalCode = "1234567890"
            val phoneNo = "1234567890"
            val displayMessage = "displayMessage"

            val request =
                MobileCreateSignatureRequestHelper.create(
                    signedContainer,
                    uuid,
                    proxyUrl,
                    skUrl,
                    locale,
                    personalCode,
                    phoneNo,
                    displayMessage,
                )

            assertEquals(RELYING_PARTY_NAME, request.relyingPartyName)
            assertEquals(RELYING_PARTY_UUID, request.relyingPartyUUID)
            assertEquals(proxyUrl, request.url)
            assertEquals("+$phoneNo", request.phoneNumber)
            assertEquals(personalCode, request.nationalIdentityNumber)
            assertEquals(signedContainer.getContainerFile()?.path, request.containerPath)
            assertEquals(DIGEST_TYPE, request.hashType)
            assertEquals(DEFAULT_LANGUAGE, request.language)
            assertEquals(displayMessage, request.displayText)
            assertEquals(DISPLAY_TEXT_FORMAT, request.displayTextFormat)
        }

    @Test
    fun mobileCreateSignatureRequestHelperTest_create_anotherLocale() =
        runTest {
            val uuid = "uuid"
            val proxyUrl = "proxyUrl"
            val skUrl = "skUrl"
            val locale = Locale("ru")
            val personalCode = "1234567890"
            val phoneNo = "1234567890"
            val displayMessage = "displayMessage"

            val request =
                MobileCreateSignatureRequestHelper.create(
                    null,
                    uuid,
                    proxyUrl,
                    skUrl,
                    locale,
                    personalCode,
                    phoneNo,
                    displayMessage,
                )

            assertEquals(RELYING_PARTY_NAME, request.relyingPartyName)
            assertEquals(uuid, request.relyingPartyUUID)
            assertEquals(skUrl, request.url)
            assertEquals("+$phoneNo", request.phoneNumber)
            assertEquals(personalCode, request.nationalIdentityNumber)
            assertEquals(null, request.containerPath)
            assertEquals(DIGEST_TYPE, request.hashType)
            assertEquals("RUS", request.language)
            assertEquals(displayMessage, request.displayText)
            assertEquals(ALTERNATIVE_DISPLAY_TEXT_FORMAT, request.displayTextFormat)
        }
}
