@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapperImpl
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager.NfcStatus
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.atLeastOnce
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class NFCViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var resources: Resources

    private lateinit var containerWrapper: ContainerWrapper

    private lateinit var container: File

    private lateinit var context: Context

    private lateinit var scenario: ActivityScenario<ComponentActivity>

    private lateinit var activity: ComponentActivity

    private lateinit var viewModel: NFCViewModel

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val configurationRepository = mock(ConfigurationRepository::class.java)
                    Initialization(configurationRepository)
                        .init(InstrumentationRegistry.getInstrumentation().targetContext)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        `when`(mockContext.resources).thenReturn(resources)

        containerWrapper = ContainerWrapperImpl()

        viewModel = NFCViewModel(NfcSmartCardReaderManager(), containerWrapper)

        scenario = ActivityScenario.launch(ComponentActivity::class.java)

        scenario.onActivity { activityInstance ->
            activity = activityInstance
        }

        container =
            AssetFile.getResourceFileAsFile(
                context,
                "example.asice",
                ee.ria.DigiDoc.common.R.raw.example,
            )
    }

    @Test
    fun nfcViewModel_setRoleDataRequested_successWithTrue() {
        runTest {
            viewModel.setRoleDataRequested(true)

            val roleDataRequested = viewModel.roleDataRequested.value

            if (roleDataRequested != null) {
                assertTrue(roleDataRequested)
            } else {
                fail("roleDataRequested is null")
            }
        }
    }

    @Test
    fun nfcViewModel_setRoleDataRequested_successWithFalse() {
        runTest {
            viewModel.setRoleDataRequested(false)

            val roleDataRequested = viewModel.roleDataRequested.value

            if (roleDataRequested != null) {
                assertFalse(roleDataRequested)
            } else {
                fail("roleDataRequested is null")
            }
        }
    }

    @Test
    fun nfcViewModel_removePendingSignature_success() =
        runTest {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            viewModel.removePendingSignature(signedContainer)

            assertEquals(1, signedContainer.getSignatures(Main).size)
        }

    @Test
    fun nfcViewModel_resetShouldResetPIN2_success() =
        runTest {
            val shouldResetPIN2Observer: Observer<Boolean?> = mock()
            viewModel.shouldResetPIN2.observeForever(shouldResetPIN2Observer)

            viewModel.resetShouldResetPIN2()
            verify(shouldResetPIN2Observer, atLeastOnce()).onChanged(null)

            viewModel.roleDataRequested.removeObserver(shouldResetPIN2Observer)
        }

    @Test
    fun nfcViewModel_resetRoleDataRequested_success() =
        runTest {
            val roleDataRequestedObserver: Observer<Boolean?> = mock()
            viewModel.roleDataRequested.observeForever(roleDataRequestedObserver)

            viewModel.resetRoleDataRequested()
            verify(roleDataRequestedObserver, atLeastOnce()).onChanged(null)

            viewModel.roleDataRequested.removeObserver(roleDataRequestedObserver)
        }

    @Test
    fun nfcViewModel_resetSignStatus_success() =
        runTest {
            val resetSignStatusObserver: Observer<Boolean?> = mock()
            viewModel.signStatus.observeForever(resetSignStatusObserver)

            viewModel.resetSignStatus()
            verify(resetSignStatusObserver, atLeastOnce()).onChanged(null)

            viewModel.signStatus.removeObserver(resetSignStatusObserver)
        }

    @Test
    fun nfcViewModel_resetErrorState_success() =
        runTest {
            val errorStateObserver: Observer<String?> = mock()
            viewModel.errorState.observeForever(errorStateObserver)

            viewModel.resetErrorState()
            verify(errorStateObserver, atLeastOnce()).onChanged(null)

            viewModel.errorState.removeObserver(errorStateObserver)
        }

    @Test
    fun nfcViewModel_resetSignedContainer_success() =
        runTest {
            val signedContainerObserver: Observer<SignedContainer?> = mock()
            viewModel.signedContainer.observeForever(signedContainerObserver)

            viewModel.resetSignedContainer()
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)

            viewModel.signedContainer.removeObserver(signedContainerObserver)
        }

    @Test
    fun nfcViewModel_shouldShowCANNumberError_false() =
        runTest {
            val result = viewModel.shouldShowCANNumberError("444222")
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_shouldShowCANNumberError_nullReturnFalse() =
        runTest {
            val result = viewModel.shouldShowCANNumberError(null)
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_isCANNumberValid_shouldShowEmptyReturnFalse() =
        runTest {
            val result = viewModel.shouldShowCANNumberError("")
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_shouldShowCANNumberError_returnTrueMinLength() =
        runTest {
            val result = viewModel.shouldShowCANNumberError("44422")
            assertTrue(result)
        }

    @Test
    fun nfcViewModel_shouldShowCANNumberError_returnTrueMaxLength() =
        runTest {
            val result = viewModel.shouldShowCANNumberError("4442222")
            assertTrue(result)
        }

    @Test
    fun nfcViewModel_shouldShowPIN2CodeError_false() =
        runTest {
            val result = viewModel.shouldShowPIN2CodeError(byteArrayOf(1, 1, 5, 5, 5))
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_isPIN2CodeValid_shouldShowNullReturnFalse() =
        runTest {
            val result = viewModel.shouldShowPIN2CodeError(null)
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_isPIN2CodeValid_shouldShowEmptyReturnFalse() =
        runTest {
            val result = viewModel.shouldShowPIN2CodeError(byteArrayOf())
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_shouldShowPIN2CodeError_returnTrueMinLength() =
        runTest {
            val result = viewModel.shouldShowPIN2CodeError(byteArrayOf(1, 1, 5, 5))
            assertTrue(result)
        }

    @Test
    fun nfcViewModel_shouldShowPIN2CodeError_returnTrueMaxLength() =
        runTest {
            val result = viewModel.shouldShowPIN2CodeError(byteArrayOf(1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7))
            assertTrue(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_true() =
        runTest {
            val result = viewModel.positiveButtonEnabled("444222", byteArrayOf(1, 1, 5, 5, 5))
            assertTrue(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_pin2InvalidReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled("444222", byteArrayOf(1, 1, 5, 5))
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_canNumberInvalidReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled("44422", byteArrayOf(1, 1, 5, 5, 5))
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_false() =
        runTest {
            val result = viewModel.positiveButtonEnabled("44422", byteArrayOf(1, 1, 5, 5))
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_personalCodeNullReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled(null, byteArrayOf(1, 1, 5, 5, 5))
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_pin2NullReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled("444222", null)
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_bothNullsReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled(null, null)
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_getNFCStatus_success() =
        runTest {
            viewModel.getNFCStatus(activity)
        }

    @Test
    fun nfcViewModel_checkNFCStatus_statusNFCActive() =
        runTest {
            viewModel.checkNFCStatus(NfcStatus.NFC_ACTIVE)

            val nfcStatus = viewModel.nfcStatus.value

            if (nfcStatus != null) {
                assertEquals(NfcStatus.NFC_ACTIVE, nfcStatus)
            } else {
                fail("nfcStatus is null")
            }

            val messageObserver: Observer<Int?> = mock()
            viewModel.message.observeForever(messageObserver)
            verify(messageObserver, atLeastOnce()).onChanged(R.string.signature_update_nfc_hold)
            viewModel.message.removeObserver(messageObserver)
        }

    @Test
    fun nfcViewModel_checkNFCStatus_statusNFCNotSupported() =
        runTest {
            viewModel.checkNFCStatus(NfcStatus.NFC_NOT_SUPPORTED)

            val nfcStatus = viewModel.nfcStatus.value

            if (nfcStatus != null) {
                assertEquals(NfcStatus.NFC_NOT_SUPPORTED, nfcStatus)
            } else {
                fail("nfcStatus is null")
            }

            val messageObserver: Observer<Int?> = mock()
            viewModel.message.observeForever(messageObserver)
            verify(messageObserver, atLeastOnce()).onChanged(R.string.signature_update_nfc_adapter_missing)
            viewModel.message.removeObserver(messageObserver)
        }

    @Test
    fun nfcViewModel_checkNFCStatus_statusNFCNotActive() =
        runTest {
            viewModel.checkNFCStatus(NfcStatus.NFC_NOT_ACTIVE)

            val nfcStatus = viewModel.nfcStatus.value

            if (nfcStatus != null) {
                assertEquals(NfcStatus.NFC_NOT_ACTIVE, nfcStatus)
            } else {
                fail("nfcStatus is null")
            }

            val messageObserver: Observer<Int?> = mock()
            viewModel.message.observeForever(messageObserver)
            verify(messageObserver, atLeastOnce()).onChanged(R.string.signature_update_nfc_turned_off)
            viewModel.message.removeObserver(messageObserver)
        }

    @Test
    fun nfcViewModel_performNFCWorkRequest_success() =
        runTest {
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            viewModel.performNFCWorkRequest(
                activity,
                context,
                signedContainer,
                byteArrayOf(1, 1, 5, 5, 5),
                "444222",
                null,
            )

            val errorStateObserver: Observer<String?> = mock()
            viewModel.errorState.observeForever(errorStateObserver)
            verify(errorStateObserver, atLeastOnce()).onChanged(null)
            viewModel.errorState.removeObserver(errorStateObserver)

            val signStatusObserver: Observer<Boolean?> = mock()
            viewModel.signStatus.observeForever(signStatusObserver)
            verify(signStatusObserver, atLeastOnce()).onChanged(null)
            viewModel.signStatus.removeObserver(signStatusObserver)

            val messageObserver: Observer<Int?> = mock()
            viewModel.message.observeForever(messageObserver)
            verify(messageObserver, atLeastOnce()).onChanged(R.string.signature_update_nfc_adapter_missing)
            viewModel.message.removeObserver(messageObserver)
        }

    @Test
    fun nfcViewModel_performNFCWorkRequest_nullContainer() =
        runTest {
            viewModel.performNFCWorkRequest(activity, context, null, byteArrayOf(1, 1, 5, 5, 5), "444222", null)

            val errorStateObserver: Observer<String?> = mock()
            viewModel.errorState.observeForever(errorStateObserver)
            verify(errorStateObserver, atLeastOnce()).onChanged(context.getString(R.string.error_general_client))
            viewModel.errorState.removeObserver(errorStateObserver)
        }

    @Test
    fun nfcViewModel_cancelNFCWorkRequest_success() =
        runTest {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            viewModel.cancelNFCWorkRequest(signedContainer)

            assertEquals(1, signedContainer.getSignatures(Main).size)
        }

    @Test
    fun nfcViewModel_handleBackButton_success() =
        runTest {
            val errorStateObserver: Observer<Boolean> = mock()
            viewModel.shouldResetPIN2.observeForever(errorStateObserver)

            viewModel.resetDialogErrorState()
            verify(errorStateObserver, atLeastOnce()).onChanged(false)

            viewModel.shouldResetPIN2.removeObserver(errorStateObserver)
        }

    @Test
    fun nfcViewModel_resetDialogErrorState_success() =
        runTest {
            val errorStateObserver: Observer<Int> = mock()
            viewModel.dialogError.observeForever(errorStateObserver)

            viewModel.resetDialogErrorState()
            verify(errorStateObserver, atLeastOnce()).onChanged(0)

            viewModel.dialogError.removeObserver(errorStateObserver)
        }
}
