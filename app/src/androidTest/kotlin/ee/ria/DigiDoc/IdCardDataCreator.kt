@file:Suppress("PackageName")

package ee.ria.DigiDoc

import ee.ria.DigiDoc.common.model.EIDType
import ee.ria.DigiDoc.common.model.ExtendedCertificate
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.idcard.PersonalData
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyUsage
import org.mockito.Mockito.mock

class IdCardDataCreator {
    companion object {
        fun createMockIdCardData(
            type: EIDType = EIDType.ID_CARD,
            personalData: PersonalData = mock(PersonalData::class.java),
            authCertificate: ExtendedCertificate = createMockExtendedCertificate(),
            signCertificate: ExtendedCertificate = createMockExtendedCertificate(),
            pin1RetryCount: Int = 3,
            pin2RetryCount: Int = 3,
            pukRetryCount: Int = 3,
            pin2CodeChanged: Boolean = true,
        ): IdCardData =
            IdCardData(
                type = type,
                personalData = personalData,
                authCertificate = authCertificate,
                signCertificate = signCertificate,
                pin1RetryCount = pin1RetryCount,
                pin2RetryCount = pin2RetryCount,
                pukRetryCount = pukRetryCount,
                pin2CodeChanged = pin2CodeChanged,
            )

        private fun createMockExtendedCertificate(): ExtendedCertificate =
            ExtendedCertificate(
                type = EIDType.ID_CARD,
                data = byteArrayOf(0x01, 0x02, 0x03),
                keyUsage = mock(KeyUsage::class.java),
                extendedKeyUsage = mock(ExtendedKeyUsage::class.java),
                ellipticCurve = true,
            )
    }
}
