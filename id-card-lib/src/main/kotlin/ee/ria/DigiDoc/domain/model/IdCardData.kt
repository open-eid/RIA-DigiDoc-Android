@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model

import ee.ria.DigiDoc.common.model.EIDType
import ee.ria.DigiDoc.common.model.IdCardCertificate
import ee.ria.DigiDoc.idcard.PersonalData

data class IdCardData(
    val type: EIDType,
    val personalData: PersonalData,
    val authCertificate: IdCardCertificate,
    val signCertificate: IdCardCertificate,
    val pin1RetryCount: Int,
    val pin2RetryCount: Int,
    val pukRetryCount: Int,
)
