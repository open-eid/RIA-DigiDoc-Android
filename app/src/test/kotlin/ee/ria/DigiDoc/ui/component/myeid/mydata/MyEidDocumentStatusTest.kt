// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.myeid.mydata

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateNotYetValidException
import java.security.cert.X509Certificate

class MyEidDocumentStatusTest {
    @Test
    fun myEidDocumentStatus_certificateStatus_returnsUnknownWhenCertificateIsNull() {
        assertEquals(MyEidDocumentStatus.UNKNOWN, certificateStatus(null))
    }

    @Test
    fun myEidDocumentStatus_certificateStatus_returnsExpiredWhenCertificateHasExpired() {
        val certificate = mock<X509Certificate>()
        whenever(certificate.checkValidity()).thenThrow(CertificateExpiredException())

        assertEquals(MyEidDocumentStatus.EXPIRED, certificateStatus(certificate))
    }

    @Test
    fun myEidDocumentStatus_certificateStatus_returnsUnknownWhenCertificateIsNotYetValid() {
        val certificate = mock<X509Certificate>()
        whenever(certificate.checkValidity()).thenThrow(CertificateNotYetValidException())

        assertEquals(MyEidDocumentStatus.UNKNOWN, certificateStatus(certificate))
    }

    @Test
    fun myEidDocumentStatus_certificateStatus_returnsValidWhenCertificateIsWithinItsValidityPeriod() {
        val certificate = mock<X509Certificate>()

        assertEquals(MyEidDocumentStatus.VALID, certificateStatus(certificate))
    }
}
