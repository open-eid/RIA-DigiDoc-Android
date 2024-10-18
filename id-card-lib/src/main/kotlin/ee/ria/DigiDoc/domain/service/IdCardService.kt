@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service

import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData

interface IdCardService {
    @Throws(Exception::class)
    suspend fun signContainer(
        token: Token,
        container: SignedContainer,
        pin2: ByteArray,
        roleData: RoleData? = null,
    ): SignedContainer

    @Throws(Exception::class)
    suspend fun data(token: Token): IdCardData
}
