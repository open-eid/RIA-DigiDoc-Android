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

package ee.ria.DigiDoc.utils.libdigidoc

import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.cryptolib.CertType
import ee.ria.DigiDoc.cryptolib.R
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipientCertTypeUtilTest {
    @Test
    fun recipientCertTypeUtilTest_getRecipientCertTypeText_success() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals(
            context.getString(R.string.crypto_container_cert_type_unknown_type),
            RecipientCertTypeUtil.getRecipientCertTypeText(context, CertType.UnknownType),
        )
        assertEquals(
            context.getString(R.string.crypto_container_cert_type_id_card_type),
            RecipientCertTypeUtil.getRecipientCertTypeText(context, CertType.IDCardType),
        )
        assertEquals(
            context.getString(R.string.crypto_container_cert_type_digi_id_type),
            RecipientCertTypeUtil.getRecipientCertTypeText(context, CertType.DigiIDType),
        )
        assertEquals(
            context.getString(R.string.crypto_container_cert_type_e_resident_type),
            RecipientCertTypeUtil.getRecipientCertTypeText(context, CertType.EResidentType),
        )
        assertEquals(
            context.getString(R.string.crypto_container_cert_type_mobile_id_type),
            RecipientCertTypeUtil.getRecipientCertTypeText(context, CertType.MobileIDType),
        )
        assertEquals(
            context.getString(R.string.crypto_container_cert_type_smart_id_type),
            RecipientCertTypeUtil.getRecipientCertTypeText(context, CertType.SmartIDType),
        )
        assertEquals(
            context.getString(R.string.crypto_container_cert_type_e_seal_type),
            RecipientCertTypeUtil.getRecipientCertTypeText(context, CertType.ESealType),
        )
    }
}
