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
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.domain.service.IdCardService
import ee.ria.DigiDoc.exceptions.NFCError
import ee.ria.DigiDoc.idcard.CodeNotActivatedException
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.PaceTunnelException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapperImpl
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.libdigidoclib.init.LibdigidocLibraryLoader
import ee.ria.DigiDoc.smartcardreader.ApduResponseException
import ee.ria.DigiDoc.smartcardreader.CardConnectionLostException
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager.NfcStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
import java.io.IOException

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

    private lateinit var cdoc2Settings: CDOC2Settings

    private lateinit var configurationRepository: ConfigurationRepository

    private lateinit var dataStore: DataStore

    @Mock
    private lateinit var idCardService: IdCardService

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val context = InstrumentationRegistry.getInstrumentation().targetContext
                    val configurationRepository = mock(ConfigurationRepository::class.java)
                    LibdigidocLibraryLoader().init(context)
                    Initialization(configurationRepository).init(context)
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
        configurationRepository = mock(ConfigurationRepository::class.java)
        cdoc2Settings = CDOC2Settings(context, configurationRepository)
        dataStore = DataStore(context)

        viewModel =
            NFCViewModel(
                NfcSmartCardReaderManager(),
                containerWrapper,
                cdoc2Settings,
                configurationRepository,
                idCardService,
                dataStore,
            )

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

            assertEquals(1, signedContainer.getSignatures().size)
        }

    @Test
    fun nfcViewModel_resetShouldResetPIN_success() =
        runTest {
            val shouldResetPINObserver: Observer<Boolean?> = mock()
            viewModel.shouldResetPIN.observeForever(shouldResetPINObserver)

            viewModel.resetShouldResetPIN()
            verify(shouldResetPINObserver, atLeastOnce()).onChanged(false)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun nfcViewModel_resetErrorState_success() =
        runTest {
            val values = mutableListOf<NFCError?>()
            val job = launch { viewModel.errorState.toList(values) }

            viewModel.performNFCSignWorkRequest(
                activity = activity,
                context = context,
                container = null,
                pin2Code = byteArrayOf(),
                canNumber = "",
                roleData = null,
            )

            viewModel.resetErrorState()

            advanceUntilIdle()
            job.cancel()

            assertTrue(values.isNotEmpty())
            assertNull(values.last())
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
    fun nfcViewModel_positiveButtonEnabledForPIN1_true() =
        runTest {
            val result =
                viewModel
                    .positiveButtonEnabled("444222", byteArrayOf(1, 1, 5, 5, 5), CodeType.PIN1)
            assertTrue(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_PIN1InvalidReturnFalse() =
        runTest {
            val result =
                viewModel
                    .positiveButtonEnabled("444222", byteArrayOf(1, 1, 5), CodeType.PIN1)
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_PIN1NullReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled("444222", null, CodeType.PIN1)
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabledForPIN2_true() =
        runTest {
            val result =
                viewModel
                    .positiveButtonEnabled("444222", byteArrayOf(1, 1, 5, 5, 5), CodeType.PIN2)
            assertTrue(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_PIN2InvalidReturnFalse() =
        runTest {
            val result =
                viewModel
                    .positiveButtonEnabled("444222", byteArrayOf(1, 1, 5, 5), CodeType.PIN2)
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_PIN2NullReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled("444222", null, CodeType.PIN2)
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_canNumberInvalidReturnFalse() =
        runTest {
            val result =
                viewModel
                    .positiveButtonEnabled("44422", byteArrayOf(1, 1, 5, 5, 5), CodeType.PIN2)
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_false() =
        runTest {
            val result =
                viewModel
                    .positiveButtonEnabled("44422", byteArrayOf(1, 1, 5, 5), CodeType.PIN2)
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_personalCodeNullReturnFalse() =
        runTest {
            val result =
                viewModel
                    .positiveButtonEnabled(null, byteArrayOf(1, 1, 5, 5, 5), CodeType.PIN2)
            assertFalse(result)
        }

    @Test
    fun nfcViewModel_positiveButtonEnabled_bothNullsReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled(null, null, CodeType.PIN2)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun nfcViewModel_performNFCSignWorkRequest_success() =
        runTest {
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val values = mutableListOf<NFCError?>()
            val job = launch { viewModel.errorState.toList(values) }

            viewModel.performNFCSignWorkRequest(
                activity,
                context,
                signedContainer,
                byteArrayOf(1, 1, 5, 5, 5),
                "444222",
                null,
            )

            advanceUntilIdle()
            job.cancel()

            assertTrue(values.isNotEmpty())
            assertNull(values.last())

            val signStatusObserver: Observer<Boolean?> = mock()
            viewModel.signStatus.observeForever(signStatusObserver)
            verify(signStatusObserver, atLeastOnce()).onChanged(null)
            viewModel.signStatus.removeObserver(signStatusObserver)

            val messageObserver: Observer<Int?> = mock()
            viewModel.message.observeForever(messageObserver)
            verify(messageObserver, atLeastOnce()).onChanged(R.string.signature_update_nfc_adapter_missing)
            viewModel.message.removeObserver(messageObserver)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun nfcViewModel_performNFCSignWorkRequest_nullContainer() =
        runTest {
            val values = mutableListOf<NFCError?>()
            val job = launch { viewModel.errorState.toList(values) }

            viewModel.performNFCSignWorkRequest(activity, context, null, byteArrayOf(1, 1, 5, 5, 5), "444222", null)

            advanceUntilIdle()
            job.cancel()

            assertTrue(values.isNotEmpty())
            val error = values.last()
            assertNotNull(error)
            assertEquals(R.string.error_general_client, error?.message)
        }

    @Test
    fun nfcViewModel_cancelNFCSignWorkRequest_success() =
        runTest {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            viewModel.cancelNFCSignWorkRequest(signedContainer)

            assertEquals(1, signedContainer.getSignatures().size)
        }

    @Test
    fun nfcViewModel_handleBackButton_success() =
        runTest {
            val errorStateObserver: Observer<Boolean> = mock()
            viewModel.shouldResetPIN.observeForever(errorStateObserver)

            viewModel.resetDialogErrorState()
            verify(errorStateObserver, atLeastOnce()).onChanged(false)

            viewModel.shouldResetPIN.removeObserver(errorStateObserver)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun nfcViewModel_loadPersonalData_success() =
        runTest {
            val values = mutableListOf<NFCError?>()
            val job = launch { viewModel.errorState.toList(values) }

            viewModel.loadPersonalData(activity, "123456")

            advanceUntilIdle()
            job.cancel()

            assertNull(values.last())

            val userDataObserver: Observer<IdCardData?> = mock()
            viewModel.userData.observeForever(userDataObserver)
            verify(userDataObserver, atLeastOnce()).onChanged(null)
            viewModel.userData.removeObserver(userDataObserver)

            val messageObserver: Observer<Int?> = mock()
            viewModel.message.observeForever(messageObserver)
            verify(messageObserver, atLeastOnce()).onChanged(R.string.signature_update_nfc_adapter_missing)
            viewModel.message.removeObserver(messageObserver)
        }

    @Test
    fun nfcViewModel_resetIdCardUserData_success() =
        runTest {
            val userDataObserver: Observer<IdCardData?> = mock()
            viewModel.userData.observeForever(userDataObserver)

            viewModel.resetIdCardUserData()
            verify(userDataObserver, atLeastOnce()).onChanged(null)

            viewModel.userData.removeObserver(userDataObserver)
        }

    private fun handleException(
        ex: SmartCardReaderException,
        codeType: CodeType = CodeType.PIN2,
    ) = viewModel.handleSmartCardReaderException(ex, codeType, codeType.name)

    @Test
    fun nfcViewModel_handleSmartCardReaderException_cardConnectionLostReportsTagLost() =
        runTest {
            handleException(CardConnectionLostException(IOException("tag left the field")))
            assertEquals(
                NFCError.TagLost(R.string.signature_update_nfc_tag_lost),
                viewModel.errorState.value,
            )
        }

    @Test
    fun nfcViewModel_handleSmartCardReaderException_codeNotActivatedShowsDialogWhenSigning() =
        runTest {
            handleException(CodeNotActivatedException(CodeType.PIN2))
            assertEquals(R.string.sign_blocked_pin2_unchanged_message, viewModel.dialogError.value)
            assertNull(viewModel.errorState.value)
        }

    @Test
    fun nfcViewModel_handleSmartCardReaderException_codeNotActivatedIsTechnicalErrorWhenNotSigning() =
        runTest {
            handleException(CodeNotActivatedException(CodeType.PIN2), CodeType.PIN1)
            assertEquals(
                NFCError.TechnicalError(R.string.signature_update_nfc_technical_error),
                viewModel.errorState.value,
            )
        }

    @Test
    fun nfcViewModel_handleSmartCardReaderException_twoRetriesLeftReportsWrongPin() =
        runTest {
            handleException(CodeVerificationException(CodeType.PIN2, 2))
            assertEquals(
                NFCError.WrongPin("PIN2", 2, R.string.id_card_sign_pin_invalid),
                viewModel.errorState.value,
            )
            assertTrue(viewModel.shouldResetPIN.value == true)
        }

    @Test
    fun nfcViewModel_handleSmartCardReaderException_oneRetryLeftReportsFinalAttempt() =
        runTest {
            handleException(CodeVerificationException(CodeType.PIN2, 1))
            assertEquals(
                NFCError.WrongPin("PIN2", 1, R.string.id_card_sign_pin_invalid_final),
                viewModel.errorState.value,
            )
        }

    @Test
    fun nfcViewModel_handleSmartCardReaderException_noRetriesLeftReportsPinBlocked() =
        runTest {
            handleException(CodeVerificationException(CodeType.PIN2, 0))
            assertEquals(
                NFCError.PinBlocked("PIN2", R.string.id_card_sign_pin_locked),
                viewModel.errorState.value,
            )
        }

    @Test
    fun nfcViewModel_handleSmartCardReaderException_pin1FlowReportsItsOwnRetryCount() =
        runTest {
            handleException(CodeVerificationException(CodeType.PIN1, 1), CodeType.PIN1)
            assertEquals(
                NFCError.WrongPin("PIN1", 1, R.string.id_card_sign_pin_invalid_final),
                viewModel.errorState.value,
            )
        }

    @Test
    fun nfcViewModel_handleSmartCardReaderException_codeTypeMismatchFallsThroughToTechnicalError() =
        runTest {
            handleException(CodeVerificationException(CodeType.PUK, 2))
            assertEquals(
                NFCError.TechnicalError(R.string.signature_update_nfc_technical_error),
                viewModel.errorState.value,
            )
            assertFalse(viewModel.shouldResetPIN.value == true)
        }

    @Test
    fun nfcViewModel_handleSmartCardReaderException_apduResponseReportsTechnicalError() =
        runTest {
            handleException(ApduResponseException(0x6A.toByte(), 0x88.toByte()))
            assertEquals(
                NFCError.ApduResponse(R.string.signature_update_nfc_technical_error),
                viewModel.errorState.value,
            )
        }

    @Test
    fun nfcViewModel_handleSmartCardReaderException_paceTunnelReportsWrongCan() =
        runTest {
            handleException(PaceTunnelException(IOException("wrong CAN")))
            assertEquals(
                NFCError.WrongCan(R.string.signature_update_nfc_wrong_can),
                viewModel.errorState.value,
            )
        }

    @Test
    fun nfcViewModel_handleSmartCardReaderException_signingFailureClearsSignStatus() =
        runTest {
            handleException(CardConnectionLostException(IOException()))
            assertFalse(viewModel.signStatus.value == true)
        }
}
