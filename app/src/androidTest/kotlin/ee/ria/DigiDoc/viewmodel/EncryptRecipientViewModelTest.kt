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
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
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

    private lateinit var context: Context
    private lateinit var sharedContainerViewModel: SharedContainerViewModel
    private lateinit var viewModel: EncryptRecipientViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        sharedContainerViewModel = SharedContainerViewModel(context, context.contentResolver)
        viewModel =
            EncryptRecipientViewModel(
                context,
                mimeTypeResolver,
                recipientRepository,
                CDOC2Settings(context, configurationRepository),
                configurationRepository,
            )
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
}
