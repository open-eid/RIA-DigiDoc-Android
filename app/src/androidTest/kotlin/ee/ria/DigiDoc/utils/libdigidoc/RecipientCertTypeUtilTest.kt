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
