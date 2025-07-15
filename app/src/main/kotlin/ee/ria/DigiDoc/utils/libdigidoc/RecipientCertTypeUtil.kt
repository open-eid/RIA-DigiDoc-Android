@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.libdigidoc

import android.content.Context
import ee.ria.DigiDoc.cryptolib.CertType
import ee.ria.DigiDoc.cryptolib.R

object RecipientCertTypeUtil {
    fun getRecipientCertTypeText(
        context: Context,
        certType: CertType,
    ): String {
        return when (certType) {
            CertType.UnknownType -> context.getString(R.string.crypto_container_cert_type_unknown_type)
            CertType.IDCardType -> context.getString(R.string.crypto_container_cert_type_id_card_type)
            CertType.DigiIDType -> context.getString(R.string.crypto_container_cert_type_digi_id_type)
            CertType.EResidentType -> context.getString(R.string.crypto_container_cert_type_e_resident_type)
            CertType.MobileIDType -> context.getString(R.string.crypto_container_cert_type_mobile_id_type)
            CertType.SmartIDType -> context.getString(R.string.crypto_container_cert_type_smart_id_type)
            CertType.ESealType -> context.getString(R.string.crypto_container_cert_type_e_seal_type)
        }
    }
}
