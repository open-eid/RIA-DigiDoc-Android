@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model

import ee.ria.DigiDoc.common.model.EIDType
import ee.ria.DigiDoc.common.model.ExtendedCertificate
import ee.ria.DigiDoc.idcard.PersonalData

data class IdCardData(
    val type: EIDType,
    val personalData: PersonalData,
    val authCertificate: ExtendedCertificate,
    val signCertificate: ExtendedCertificate,
    val pin1RetryCount: Int,
    val pin2RetryCount: Int,
    val pukRetryCount: Int,
)
