// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service

import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException

interface IdCardService {
    @Throws(Exception::class)
    suspend fun data(token: Token): IdCardData

    @Throws(CodeVerificationException::class, SmartCardReaderException::class)
    suspend fun editPin(
        token: Token,
        codeType: CodeType,
        currentPin: ByteArray,
        newPin: ByteArray,
    ): IdCardData

    @Throws(CodeVerificationException::class, SmartCardReaderException::class)
    suspend fun unblockAndEditPin(
        token: Token,
        codeType: CodeType,
        currentPuk: ByteArray,
        newPin: ByteArray,
    ): IdCardData

    @Throws(Exception::class)
    fun authenticate(
        token: Token,
        pin1: ByteArray,
        origin: String,
        challenge: String,
    ): Triple<ByteArray, ByteArray?, ByteArray>

    @Throws(Exception::class)
    fun sign(
        token: Token,
        pin2: ByteArray?,
        hash: ByteArray,
    ): Pair<ByteArray, ByteArray>
}
