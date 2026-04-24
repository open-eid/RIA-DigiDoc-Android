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

package ee.ria.DigiDoc.smartId.utils

import ee.ria.DigiDoc.common.Constant.SignatureRequest.DIGEST_TYPE
import ee.ria.DigiDoc.common.Constant.SignatureRequest.MAX_DISPLAY_MESSAGE_LENGTH
import ee.ria.DigiDoc.common.Constant.SignatureRequest.RELYING_PARTY_NAME
import ee.ria.DigiDoc.common.Constant.SignatureRequest.RELYING_PARTY_UUID
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.network.sid.dto.request.SmartCreateSignatureRequest
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.text.MessageUtil
import org.apache.commons.lang3.StringUtils

object SmartCreateSignatureRequestHelper {
    fun create(
        container: SignedContainer?,
        uuid: String?,
        proxyUrlV2: String?,
        skUrlV2: String?,
        country: String?,
        nationalIdentityNumber: String?,
        displayMessage: String?,
    ): SmartCreateSignatureRequest {
        val request =
            SmartCreateSignatureRequest(
                relyingPartyName = RELYING_PARTY_NAME,
                relyingPartyUUID = if (uuid.isNullOrEmpty()) RELYING_PARTY_UUID else uuid,
                url =
                    if ((uuid == null || uuid.isEmpty()) || uuid == RELYING_PARTY_UUID) {
                        proxyUrlV2
                    } else {
                        skUrlV2
                    },
                country = country,
                nationalIdentityNumber = nationalIdentityNumber,
                containerPath = container?.getContainerFile()?.path,
                hashType = DIGEST_TYPE,
                displayText = getDisplayText(displayMessage, container),
            )

        return request
    }

    private fun getDisplayText(
        displayMessage: String?,
        container: SignedContainer?,
    ): String? {
        if (container != null) {
            return MessageUtil.escape(
                displayMessage?.let {
                    StringUtils.truncate(
                        java.lang.String.format(
                            "%s %s",
                            displayMessage,
                            container
                                .getContainerFile()
                                ?.let { it1 -> FileUtil.getSignDocumentFileName(it1) },
                        ),
                        MAX_DISPLAY_MESSAGE_LENGTH,
                    )
                },
            )
        }
        return displayMessage
    }
}
