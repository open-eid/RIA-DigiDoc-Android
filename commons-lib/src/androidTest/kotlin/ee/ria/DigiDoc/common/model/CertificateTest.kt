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

package ee.ria.DigiDoc.common.model

import ee.ria.DigiDoc.common.certificate.CertificateService
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class CertificateTest {
    @Mock
    private lateinit var certificateService: CertificateService

    @Mock
    private lateinit var x509CertificateHolder: X509CertificateHolder

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun certificate_create_success() {
        val testData = byteArrayOf(1, 2, 3)
        val testName = "Test name"

        `when`(certificateService.extractFriendlyName(anyOrNull())).thenReturn(testName)

        val certificate = Certificate.create(testData, certificateService)

        assertEquals(testName, certificate.friendlyName)
    }

    @Test(expected = IOException::class)
    fun certificate_create_throwIOExceptionWhenInvalidCertificate() {
        val testData = byteArrayOf(1, 2, 3)

        `when`(certificateService.parseCertificate(anyOrNull()))
            .thenThrow(IOException())

        Certificate.create(testData, certificateService)
    }
}
