@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.libdigidoc

import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.libdigidoclib.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import org.junit.Assert.assertEquals
import org.junit.Test

class SignatureStatusUtilTest {
    @Test
    fun testGetSignatureStatusText() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals(
            context.getString(R.string.signing_container_signature_status_valid),
            SignatureStatusUtil.getSignatureStatusText(context, ValidatorInterface.Status.Valid),
        )
        assertEquals(
            "${context.getString(R.string.signing_container_signature_status_valid)} " +
                context.getString(R.string.signing_container_signature_status_warning),
            SignatureStatusUtil.getSignatureStatusText(context, ValidatorInterface.Status.Warning),
        )
        assertEquals(
            "${context.getString(R.string.signing_container_signature_status_valid)} " +
                context.getString(R.string.signing_container_signature_status_non_qscd),
            SignatureStatusUtil.getSignatureStatusText(context, ValidatorInterface.Status.NonQSCD),
        )
        assertEquals(
            context.getString(R.string.signing_container_signature_status_invalid),
            SignatureStatusUtil.getSignatureStatusText(context, ValidatorInterface.Status.Invalid),
        )
        assertEquals(
            context.getString(R.string.signing_container_signature_status_unknown),
            SignatureStatusUtil.getSignatureStatusText(context, ValidatorInterface.Status.Unknown),
        )
    }
}
