// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.cryptolib.CertType
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.cryptolib.exception.CryptoException
import ee.ria.DigiDoc.cryptolib.repository.RecipientRepository
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class EncryptRecipientViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mimeTypeResolver: MimeTypeResolver

    @Mock
    private lateinit var recipientRepository: RecipientRepository

    @Mock
    private lateinit var configurationRepository: ConfigurationRepository

    @Mock
    private lateinit var errorStateObserver: Observer<Int?>

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var context: Context
    private lateinit var sharedContainerViewModel: SharedContainerViewModel
    private lateinit var viewModel: EncryptRecipientViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        sharedContainerViewModel = SharedContainerViewModel(context)
        viewModel =
            EncryptRecipientViewModel(
                context,
                mimeTypeResolver,
                recipientRepository,
                CDOC2Settings(context, configurationRepository),
                configurationRepository,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun encryptRecipientViewModel_encryptWithPassword_returnsEncryptErrorWithNoContainer() =
        runTest {
            viewModel.errorState.observeForever(errorStateObserver)

            viewModel.encryptWithPassword("MyKey", "password123".toByteArray(), sharedContainerViewModel)?.join()

            assertEquals(R.string.crypto_encrypt_error, viewModel.errorState.value)
        }

    @Test
    fun encryptRecipientViewModel_decryptContainerWithPassword_throwsCryptoExceptionWithNoContainer() =
        runTest {
            try {
                viewModel.decryptContainerWithPassword("password123".toByteArray(), sharedContainerViewModel)
                fail("Expected CryptoException to be thrown")
            } catch (e: CryptoException) {
                assertEquals("No container to decrypt", e.message)
            }
        }

    @Test
    fun encryptRecipientViewModel_resetErrorState_clearsErrorState() =
        runTest {
            viewModel.encryptWithPassword("key", "pass".toByteArray(), sharedContainerViewModel)?.join()
            viewModel.resetErrorState()

            assertNull(viewModel.errorState.value)
        }

    @Test
    fun encryptRecipientViewModel_encryptWithPassword_returnsDataFilesEmptyErrorWithEmptyDataFiles() =
        runTest {
            sharedContainerViewModel.setCryptoContainer(
                CryptoContainer(
                    context = context,
                    file = File(context.cacheDir, "test.cdoc"),
                    dataFiles = ArrayList(),
                    recipients =
                        ArrayList<Addressee>().apply {
                            add(
                                Addressee(
                                    data = ByteArray(0),
                                    identifier = "key",
                                    serialNumber = null,
                                    givenName = null,
                                    surname = null,
                                    certType = CertType.PasswordType,
                                    validTo = null,
                                    concatKDFAlgorithmURI = null,
                                ),
                            )
                        },
                    decrypted = false,
                    encrypted = false,
                ),
            )

            viewModel.encryptWithPassword("key", "password".toByteArray(), sharedContainerViewModel)?.join()

            assertEquals(R.string.crypto_encrypt_data_files_empty_error, viewModel.errorState.value)
        }

    @Test
    fun encryptRecipientViewModel_recipientList_hidesRecipientsAlreadyInContainer() =
        runTest(testDispatcher) {
            val addedRecipient = recipient(byteArrayOf(1))
            val foundRecipient = recipient(byteArrayOf(2))
            `when`(recipientRepository.find(context, SEARCH_QUERY))
                .thenReturn(Pair(listOf(addedRecipient, foundRecipient), 2))
            sharedContainerViewModel.setCryptoContainer(cryptoContainer(addedRecipient))
            viewModel.updateContainerRecipients(sharedContainerViewModel)

            val emitted = mutableListOf<RecipientSearchState>()
            val job = launch { viewModel.recipientSearchState.toList(emitted) }

            viewModel.onQueryTextChange(SEARCH_QUERY)
            advanceUntilIdle()
            job.cancel()

            assertTrue(emitted.isNotEmpty())
            assertEquals(listOf(foundRecipient), emitted.last().recipients)
            assertFalse(emitted.last().allFoundRecipientsAdded)
        }

    @Test
    fun encryptRecipientViewModel_allFoundRecipientsAdded_returnsTrueWhenEveryResultIsInContainer() =
        runTest(testDispatcher) {
            val addedRecipient = recipient(byteArrayOf(1))
            `when`(recipientRepository.find(context, SEARCH_QUERY))
                .thenReturn(Pair(listOf(addedRecipient), 1))
            sharedContainerViewModel.setCryptoContainer(cryptoContainer(addedRecipient))
            viewModel.updateContainerRecipients(sharedContainerViewModel)

            val emitted = mutableListOf<RecipientSearchState>()
            val job = launch { viewModel.recipientSearchState.toList(emitted) }

            viewModel.onQueryTextChange(SEARCH_QUERY)
            advanceUntilIdle()
            job.cancel()

            assertTrue(emitted.isNotEmpty())
            assertEquals(listOf<Addressee>(), emitted.last().recipients)
            assertTrue(emitted.last().allFoundRecipientsAdded)
        }

    @Test
    fun encryptRecipientViewModel_recipientList_returnsRecipientRemovedFromContainer() =
        runTest(testDispatcher) {
            val addedRecipient = recipient(byteArrayOf(1))
            `when`(recipientRepository.find(context, SEARCH_QUERY))
                .thenReturn(Pair(listOf(addedRecipient), 1))
            val container = cryptoContainer(addedRecipient)
            sharedContainerViewModel.setCryptoContainer(container)
            viewModel.updateContainerRecipients(sharedContainerViewModel)

            val emitted = mutableListOf<RecipientSearchState>()
            val job = launch { viewModel.recipientSearchState.toList(emitted) }

            viewModel.onQueryTextChange(SEARCH_QUERY)
            advanceUntilIdle()
            assertTrue(emitted.isNotEmpty())
            assertEquals(listOf<Addressee>(), emitted.last().recipients)

            sharedContainerViewModel.removeRecipient(container, addedRecipient)
            viewModel.updateContainerRecipients(sharedContainerViewModel)
            advanceUntilIdle()
            job.cancel()

            assertTrue(emitted.isNotEmpty())
            assertEquals(listOf(addedRecipient), emitted.last().recipients)
            verify(recipientRepository, times(1)).find(context, SEARCH_QUERY)
        }

    private fun cryptoContainer(recipient: Addressee): CryptoContainer =
        CryptoContainer(
            context = context,
            file = File(context.cacheDir, "test.cdoc"),
            dataFiles = ArrayList(),
            recipients = arrayListOf(recipient),
            decrypted = false,
            encrypted = false,
        )

    private fun recipient(data: ByteArray): Addressee =
        Addressee(
            data = data,
            identifier = "11111111111",
            serialNumber = "11111111111",
            givenName = "Test",
            surname = "Recipient",
            certType = CertType.IDCardType,
            validTo = null,
            concatKDFAlgorithmURI = null,
        )

    private companion object {
        const val SEARCH_QUERY = "recipient"
    }
}
