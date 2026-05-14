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
