@file:Suppress("PackageName")

package ee.ria.DigiDoc.smartId.utils

import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.Constant.SignatureRequest.DIGEST_TYPE
import ee.ria.DigiDoc.common.Constant.SignatureRequest.RELYING_PARTY_NAME
import ee.ria.DigiDoc.common.Constant.SignatureRequest.RELYING_PARTY_UUID
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile.Companion.getResourceFileAsFile
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer.Companion.openOrCreate
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mockito.mock

class SmartCreateSignatureRequestHelperTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val configurationRepository = mock(ConfigurationRepository::class.java)
                    Initialization(configurationRepository)
                        .init(InstrumentationRegistry.getInstrumentation().targetContext)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Test
    fun smartCreateSignatureRequestHelper_create_success() =
        runTest {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val container =
                getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer = openOrCreate(context, container, listOf(container), true)

            val proxyUrl = "proxyUrl"
            val skUrl = "skUrl"
            val personalCode = "1234567890"
            val country = "EE"
            val displayMessage = "displayMessage"

            val request =
                SmartCreateSignatureRequestHelper.create(
                    signedContainer,
                    null,
                    proxyUrl,
                    skUrl,
                    country,
                    personalCode,
                    displayMessage,
                )

            assertEquals(RELYING_PARTY_NAME, request.relyingPartyName)
            assertEquals(RELYING_PARTY_UUID, request.relyingPartyUUID)
            assertEquals(proxyUrl, request.url)
            assertEquals(country, request.country)
            assertEquals(personalCode, request.nationalIdentityNumber)
            assertEquals(signedContainer.getContainerFile()?.path, request.containerPath)
            assertEquals(DIGEST_TYPE, request.hashType)
            assertEquals("displayMessage exa...ple.asice", request.displayText)
        }

    @Test
    fun smartCreateSignatureRequestHelper_create_containerNull() =
        runTest {
            val proxyUrl = "proxyUrl"
            val skUrl = "skUrl"
            val personalCode = "1234567890"
            val country = "EE"
            val displayMessage = "displayMessage"

            val request =
                SmartCreateSignatureRequestHelper.create(
                    null,
                    "",
                    proxyUrl,
                    skUrl,
                    country,
                    personalCode,
                    displayMessage,
                )

            assertEquals(RELYING_PARTY_NAME, request.relyingPartyName)
            assertEquals(RELYING_PARTY_UUID, request.relyingPartyUUID)
            assertEquals(proxyUrl, request.url)
            assertEquals(country, request.country)
            assertEquals(personalCode, request.nationalIdentityNumber)
            assertEquals(null, request.containerPath)
            assertEquals(DIGEST_TYPE, request.hashType)
            assertEquals(displayMessage, request.displayText)
        }

    @Test
    fun smartCreateSignatureRequestHelper_create_uuidIsSet() =
        runTest {
            val uuid = "uuid"
            val proxyUrl = "proxyUrl"
            val skUrl = "skUrl"
            val personalCode = "1234567890"
            val country = "EE"
            val displayMessage = "displayMessage"

            val request =
                SmartCreateSignatureRequestHelper.create(
                    null,
                    uuid,
                    proxyUrl,
                    skUrl,
                    country,
                    personalCode,
                    displayMessage,
                )

            assertEquals(RELYING_PARTY_NAME, request.relyingPartyName)
            assertEquals(uuid, request.relyingPartyUUID)
            assertEquals(skUrl, request.url)
            assertEquals(country, request.country)
            assertEquals(personalCode, request.nationalIdentityNumber)
            assertEquals(null, request.containerPath)
            assertEquals(DIGEST_TYPE, request.hashType)
            assertEquals(displayMessage, request.displayText)
        }
}
