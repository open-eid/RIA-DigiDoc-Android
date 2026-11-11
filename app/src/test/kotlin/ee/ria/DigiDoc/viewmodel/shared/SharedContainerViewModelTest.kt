// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import android.content.Context
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SharedContainerViewModelTest {
    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var signedContainer: SignedContainer

    @Mock
    private lateinit var signature: SignatureInterface

    @Mock
    private lateinit var dataFile: DataFileInterface

    private lateinit var viewModel: SharedContainerViewModel

    @Before
    fun setUp() {
        viewModel = SharedContainerViewModel(context)
    }

    @Test
    fun removeSignature_bumpsContentVersionAndKeepsSameContainer() =
        runTest {
            viewModel.setSignedContainer(signedContainer)

            viewModel.removeSignature(signedContainer, signature)

            verify(signedContainer).removeSignature(signature)
            assertSame(signedContainer, viewModel.signedContainer.value)
            assertEquals(1, viewModel.signedContainerContentVersion.value)
        }

    @Test
    fun removeSignature_bumpsContentVersionOnEveryRemoval() =
        runTest {
            viewModel.setSignedContainer(signedContainer)

            viewModel.removeSignature(signedContainer, signature)
            viewModel.removeSignature(signedContainer, signature)

            assertEquals(2, viewModel.signedContainerContentVersion.value)
        }

    @Test
    fun removeContainerDataFile_bumpsContentVersionAndKeepsSameContainer() =
        runTest {
            viewModel.setSignedContainer(signedContainer)

            viewModel.removeContainerDataFile(signedContainer, dataFile)

            verify(signedContainer).removeDataFile(dataFile)
            assertSame(signedContainer, viewModel.signedContainer.value)
            assertEquals(1, viewModel.signedContainerContentVersion.value)
        }
}
