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
import androidx.lifecycle.Observer
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@Suppress("UNCHECKED_CAST")
class SharedSignatureViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SharedSignatureViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = SharedSignatureViewModel()
    }

    @Test
    fun sharedSignatureViewModel_setCertificate_success() {
        val signature = mock(SignatureInterface::class.java)
        val observer = mock(Observer::class.java) as Observer<SignatureInterface?>

        viewModel.signature.observeForever(observer)
        viewModel.setSignature(signature)

        verify(observer).onChanged(signature)
        assertEquals(signature, viewModel.signature.value)
    }

    @Test
    fun sharedSignatureViewModel_resetSignature_success() {
        val signature = mock(SignatureInterface::class.java)
        val observer = mock(Observer::class.java) as Observer<SignatureInterface?>

        viewModel.signature.observeForever(observer)
        viewModel.setSignature(signature)
        viewModel.resetSignature()

        verify(observer).onChanged(null)
        assertNull(viewModel.signature.value)
    }
}
