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
    fun signatureStatusUtilTest_getTimestampStatusText_success() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals(
            context.getString(R.string.signing_container_timestamp_status_valid),
            SignatureStatusUtil.getTimestampStatusText(context, ValidatorInterface.Status.Valid),
        )
        assertEquals(
            "${context.getString(R.string.signing_container_timestamp_status_valid)} " +
                context.getString(R.string.signing_container_signature_status_warning),
            SignatureStatusUtil.getTimestampStatusText(context, ValidatorInterface.Status.Warning),
        )
        assertEquals(
            "${context.getString(R.string.signing_container_timestamp_status_valid)} " +
                context.getString(R.string.signing_container_signature_status_non_qscd),
            SignatureStatusUtil.getTimestampStatusText(context, ValidatorInterface.Status.NonQSCD),
        )
        assertEquals(
            context.getString(R.string.signing_container_timestamp_status_invalid),
            SignatureStatusUtil.getTimestampStatusText(context, ValidatorInterface.Status.Invalid),
        )
        assertEquals(
            context.getString(R.string.signing_container_timestamp_status_unknown),
            SignatureStatusUtil.getTimestampStatusText(context, ValidatorInterface.Status.Unknown),
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
