// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model

import ee.ria.DigiDoc.common.model.EIDType
import ee.ria.DigiDoc.common.model.ExtendedCertificate
import ee.ria.DigiDoc.idcard.PersonalData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class IdCardDataTest {
    private val personalData: PersonalData = mock(PersonalData::class.java)
    private val authCertificate: ExtendedCertificate = mock(ExtendedCertificate::class.java)
    private val signCertificate: ExtendedCertificate = mock(ExtendedCertificate::class.java)

    init {
        `when`(authCertificate.type).thenReturn(EIDType.ID_CARD)
        `when`(signCertificate.type).thenReturn(EIDType.ID_CARD)
    }

    private fun buildIdCardData(
        pin1CodeChanged: Boolean,
        pin2CodeChanged: Boolean,
    ) = IdCardData(
        type = EIDType.ID_CARD,
        personalData = personalData,
        authCertificate = authCertificate,
        signCertificate = signCertificate,
        pin1RetryCount = 3,
        pin2RetryCount = 3,
        pukRetryCount = 3,
        pin1CodeChanged = pin1CodeChanged,
        pin2CodeChanged = pin2CodeChanged,
    )

    @Test
    fun idCardData_pin1CodeChanged_returnTrueWhenFlagIsOne() {
        val data = buildIdCardData(pin1CodeChanged = true, pin2CodeChanged = true)
        assertTrue(data.pin1CodeChanged)
    }

    @Test
    fun idCardData_pin1CodeChanged_returnFalseWhenFlagIsZero() {
        val data = buildIdCardData(pin1CodeChanged = false, pin2CodeChanged = false)
        assertFalse(data.pin1CodeChanged)
    }

    @Test
    fun idCardData_pin2CodeChanged_returnTrueWhenFlagIsOne() {
        val data = buildIdCardData(pin1CodeChanged = true, pin2CodeChanged = true)
        assertTrue(data.pin2CodeChanged)
    }

    @Test
    fun idCardData_pin2CodeChanged_returnFalseWhenFlagIsZero() {
        val data = buildIdCardData(pin1CodeChanged = true, pin2CodeChanged = false)
        assertFalse(data.pin2CodeChanged)
    }

    @Test
    fun idCardData_courierCard_returnTrueForCourierCardWhenPin1NotChanged() {
        val data = buildIdCardData(pin1CodeChanged = false, pin2CodeChanged = false)
        val isCourierCard = !data.pin1CodeChanged
        assertTrue(isCourierCard)
    }

    @Test
    fun idCardData_activatedCard_returnFalseForCourierCardWhenBothPINsChanged() {
        val data = buildIdCardData(pin1CodeChanged = true, pin2CodeChanged = true)
        val isCourierCard = !data.pin1CodeChanged
        assertFalse(isCourierCard)
    }

    @Test
    fun idCardData_regularThalesCard_returnFalseForCourierCardWhenOnlyPin2Changed() {
        // Regular Thales card: PIN1 has been changed, only PIN2 is unused
        val data = buildIdCardData(pin1CodeChanged = true, pin2CodeChanged = false)
        val isCourierCard = !data.pin1CodeChanged
        assertFalse(isCourierCard)
    }
}
