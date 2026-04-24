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

import ee.ria.DigiDoc.common.Constant.SignatureRequest.ALTERNATIVE_DISPLAY_TEXT_FORMAT
import ee.ria.DigiDoc.common.Constant.SignatureRequest.DEFAULT_LANGUAGE
import ee.ria.DigiDoc.common.Constant.SignatureRequest.DIGEST_TYPE
import ee.ria.DigiDoc.common.Constant.SignatureRequest.DISPLAY_TEXT_FORMAT
import ee.ria.DigiDoc.common.Constant.SignatureRequest.MAX_DISPLAY_MESSAGE_BYTES
import ee.ria.DigiDoc.common.Constant.SignatureRequest.RELYING_PARTY_NAME
import ee.ria.DigiDoc.common.Constant.SignatureRequest.RELYING_PARTY_UUID
import ee.ria.DigiDoc.common.Constant.SignatureRequest.SUPPORTED_LANGUAGES
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.network.mid.dto.request.MobileCreateSignatureRequest
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.text.MessageUtil
import java.util.Locale

object MobileCreateSignatureRequestHelper {
    private val logTag = javaClass.simpleName

    fun create(
        container: SignedContainer?,
        uuid: String?,
        proxyUrl: String?,
        skUrl: String?,
        locale: Locale?,
        personalCode: String?,
        phoneNo: String,
        displayMessage: String?,
    ): MobileCreateSignatureRequest {
        val request =
            MobileCreateSignatureRequest(
                relyingPartyName = RELYING_PARTY_NAME,
                relyingPartyUUID = if (uuid.isNullOrEmpty()) RELYING_PARTY_UUID else uuid,
                url = if ((uuid == null || uuid.isEmpty()) || uuid == RELYING_PARTY_UUID) proxyUrl else skUrl,
                phoneNumber = "+$phoneNo",
                nationalIdentityNumber = personalCode,
                containerPath = container?.getContainerFile()?.path,
                hashType = DIGEST_TYPE,
                language = getLanguage(locale),
                displayText = getDisplayText(displayMessage, getLanguage(locale)),
                displayTextFormat =
                    if (getLanguage(locale) == "RUS") {
                        ALTERNATIVE_DISPLAY_TEXT_FORMAT
                    } else {
                        DISPLAY_TEXT_FORMAT
                    },
            )
        return request
    }

    private fun getDisplayText(
        displayMessage: String?,
        language: String,
    ): String =
        MessageUtil.escape(
            displayMessage?.let {
                MessageUtil.trimDisplayMessageIfNotWithinSizeLimit(
                    it,
                    MAX_DISPLAY_MESSAGE_BYTES,
                    if (language == "RUS") MessageUtil.UCS2_CHARSET else MessageUtil.GSM_CHARSET,
                )
            },
        )

    private fun getLanguage(locale: Locale?): String {
        if (locale == null) {
            return DEFAULT_LANGUAGE
        }
        try {
            val language = locale.isO3Language.uppercase(Locale.getDefault())
            return if (SUPPORTED_LANGUAGES.contains(language)) language else DEFAULT_LANGUAGE
        } catch (e: Exception) {
            errorLog(logTag, "Unable to get language from locale", e)
            return DEFAULT_LANGUAGE
        }
    }
}
