/*
 * Copyright 2017 - 2025 Riigi Infosüsteemi Amet
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

package ee.ria.DigiDoc.domain.service

import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException

interface IdCardService {
    @Throws(CodeVerificationException::class, Exception::class)
    suspend fun signContainer(
        token: Token,
        container: SignedContainer,
        pin2: ByteArray,
        roleData: RoleData? = null,
    ): SignedContainer

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
    ): Triple<ByteArray, ByteArray, ByteArray>

    @Throws(Exception::class)
    fun sign(
        token: Token,
        pin2: ByteArray?,
        hash: ByteArray,
    ): Pair<ByteArray, ByteArray>
}
