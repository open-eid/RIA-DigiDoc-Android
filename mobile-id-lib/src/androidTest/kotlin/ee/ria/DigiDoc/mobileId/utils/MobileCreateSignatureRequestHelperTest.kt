/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.mobileId.utils

import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.Constant.SignatureRequest.DEFAULT_LANGUAGE
import ee.ria.DigiDoc.common.Constant.SignatureRequest.DIGEST_TYPE
import ee.ria.DigiDoc.common.Constant.SignatureRequest.DISPLAY_TEXT_FORMAT
import ee.ria.DigiDoc.common.Constant.SignatureRequest.RELYING_PARTY_NAME
import ee.ria.DigiDoc.common.Constant.SignatureRequest.RELYING_PARTY_UUID
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer.Companion.openOrCreate
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.libdigidoclib.init.LibdigidocLibraryLoader
import ee.ria.DigiDoc.utilsLib.locale.LocaleUtil.getLocale
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mockito

class MobileCreateSignatureRequestHelperTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val context = InstrumentationRegistry.getInstrumentation().targetContext
                    val configurationRepository = Mockito.mock(ConfigurationRepository::class.java)
                    LibdigidocLibraryLoader().init(context)
                    Initialization(configurationRepository).init(context)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Test
    fun mobileCreateSignatureRequestHelper_create_uuidAndLocaleNull() =
        runTest {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer = openOrCreate(context, container, listOf(container), true)

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
    fun mobileCreateSignatureRequestHelper_create_success() =
        runTest {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer = openOrCreate(context, container, listOf(container), true)

            val uuid = ""
            val proxyUrl = "proxyUrl"
            val skUrl = "skUrl"
            val locale = getLocale("zz")
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
    fun mobileCreateSignatureRequestHelper_create_anotherLocale() =
        runTest {
            val uuid = "uuid"
            val proxyUrl = "proxyUrl"
            val skUrl = "skUrl"
            val locale = getLocale("lt")
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
            assertEquals("LIT", request.language)
            assertEquals(displayMessage, request.displayText)
            assertEquals(DISPLAY_TEXT_FORMAT, request.displayTextFormat)
        }
}
