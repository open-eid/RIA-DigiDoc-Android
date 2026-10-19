// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc

import ee.ria.DigiDoc.common.model.EIDType
import ee.ria.DigiDoc.common.model.ExtendedCertificate
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.idcard.CardType
import ee.ria.DigiDoc.idcard.PersonalData
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyUsage
import org.mockito.Mockito.mock
import java.time.LocalDate

class IdCardDataCreator {
    companion object {
        fun createMockIdCardData(
            type: EIDType = EIDType.ID_CARD,
            personalData: PersonalData = createPersonalData(),
            authCertificate: ExtendedCertificate = createMockExtendedCertificate(),
            signCertificate: ExtendedCertificate = createMockExtendedCertificate(),
            pin1RetryCount: Int = 3,
            pin2RetryCount: Int = 3,
            pukRetryCount: Int = 3,
            pin1CodeChanged: Boolean = true,
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
                pin1CodeChanged = pin1CodeChanged,
                pin2CodeChanged = pin2CodeChanged,
            )

        private fun createPersonalData(): PersonalData =
            PersonalData(
                "TESTSURNAME",
                "TESTGIVENNAME",
                "EST",
                "EST",
                LocalDate.of(2000, 1, 1),
                "00000000000",
                "T0000000",
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 1, 1),
                CardType.ID1,
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
