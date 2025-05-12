@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.libdigidoc

import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.libdigidoclib.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.utils.libdigidoc.SignatureStatusUtil.isDdocSignatureValid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SignatureStatusUtilTest {
    @Test
    fun signatureStatusUtilTest_getSignatureStatusText_success() {
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

    @Test
    fun signatureStatusUtilTest_isDdocSignatureValid_returnTrueWhenSignatureDateIsBeforeReferenceDate() {
        val signature = mock(SignatureInterface::class.java)
        `when`(signature.trustedSigningTime).thenReturn("2018-06-30T23:59:59Z")
        assertTrue(isDdocSignatureValid(signature))
    }

    @Test
    fun signatureStatusUtilTest_isDdocSignatureValid_returnFalseWhenSignatureDateIsExactlyAtReferenceDate() {
        val signature = mock(SignatureInterface::class.java)
        `when`(signature.trustedSigningTime).thenReturn("2018-07-01T00:00:00Z")
        assertFalse(isDdocSignatureValid(signature))
    }

    @Test
    fun signatureStatusUtilTest_isDdocSignatureValid_returnFalseWhenSignatureDateIsAfterReferenceDate() {
        val signature = mock(SignatureInterface::class.java)
        `when`(signature.trustedSigningTime).thenReturn("2019-01-01T00:00:00Z")
        assertFalse(isDdocSignatureValid(signature))
    }
}
