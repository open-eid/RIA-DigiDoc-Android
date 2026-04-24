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

import android.content.Context
import ee.ria.DigiDoc.cryptolib.CertType
import ee.ria.DigiDoc.cryptolib.R

object RecipientCertTypeUtil {
    fun getRecipientCertTypeText(
        context: Context,
        certType: CertType,
    ): String =
        when (certType) {
            CertType.UnknownType -> context.getString(R.string.crypto_container_cert_type_unknown_type)
            CertType.IDCardType -> context.getString(R.string.crypto_container_cert_type_id_card_type)
            CertType.DigiIDType -> context.getString(R.string.crypto_container_cert_type_digi_id_type)
            CertType.EResidentType -> context.getString(R.string.crypto_container_cert_type_e_resident_type)
            CertType.MobileIDType -> context.getString(R.string.crypto_container_cert_type_mobile_id_type)
            CertType.SmartIDType -> context.getString(R.string.crypto_container_cert_type_smart_id_type)
            CertType.ESealType -> context.getString(R.string.crypto_container_cert_type_e_seal_type)
        }
}
