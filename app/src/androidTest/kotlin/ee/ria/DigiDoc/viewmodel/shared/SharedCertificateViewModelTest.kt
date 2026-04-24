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

package ee.ria.DigiDoc.viewmodel.shared

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import java.security.cert.X509Certificate

class SharedCertificateViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SharedCertificateViewModel

    @Before
    fun setUp() {
        viewModel = SharedCertificateViewModel()
    }

    @Test
    fun sharedCertificateViewModel_setSivaCertificate_success() {
        val certificate = mock(X509Certificate::class.java)
        viewModel.setCertificate(certificate)

        assertEquals(certificate, viewModel.certificate.value)
    }

    @Test
    fun sharedCertificateViewModel_resetSivaCertificate_success() {
        val certificate = mock(X509Certificate::class.java)
        viewModel.setCertificate(certificate)
        viewModel.resetCertificate()

        assertNull(viewModel.certificate.value)
    }
}
