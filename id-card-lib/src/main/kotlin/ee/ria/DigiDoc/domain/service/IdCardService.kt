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
}
