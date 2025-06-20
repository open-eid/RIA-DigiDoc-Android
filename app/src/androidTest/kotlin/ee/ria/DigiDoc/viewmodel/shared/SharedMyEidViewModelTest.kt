@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.CertificateCreator
import ee.ria.DigiDoc.IdCardDataCreator.Companion.createMockIdCardData
import ee.ria.DigiDoc.MainActivity
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.domain.model.myeid.MyEidIdentificationMethodSetting
import ee.ria.DigiDoc.domain.model.pin.PinChangeVariant
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.domain.service.IdCardService
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.smartcardreader.SmartCardReader
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderManager
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderStatus
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReader
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager
import ee.ria.DigiDoc.ui.component.myeid.pinandcertificate.PinChangeContent
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.test.runTest
import org.bouncycastle.util.encoders.Hex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.ZoneId
import java.util.Locale

class SharedMyEidViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    lateinit var context: Context

    @Mock
    lateinit var errorStateObserver: Observer<Triple<Int, String?, Int?>?>

    @Mock
    lateinit var idCardStatusObserver: Observer<SmartCardReaderStatus?>

    @Mock
    lateinit var idCardDataObserver: Observer<IdCardData?>

    @Mock
    lateinit var pinChangingStateObserver: Observer<Boolean?>

    @Mock
    lateinit var isPinBlockedObserver: Observer<Boolean?>

    @Mock
    lateinit var identificationMethodObserver: Observer<MyEidIdentificationMethodSetting?>

    @Mock
    lateinit var mockSmartCardReaderManager: SmartCardReaderManager

    @Mock
    lateinit var mockSmartCardReader: SmartCardReader

    @Mock
    lateinit var mockNfcSmartCardReader: NfcSmartCardReader

    @Mock
    lateinit var mockIdCardService: IdCardService

    @Mock
    lateinit var mockToken: Token

    lateinit var nfcSmartCardReaderManager: NfcSmartCardReaderManager

    lateinit var dataStore: DataStore

    private lateinit var viewModel: SharedMyEidViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        context = InstrumentationRegistry.getInstrumentation().targetContext

        dataStore = DataStore(context)
        nfcSmartCardReaderManager = NfcSmartCardReaderManager()

        `when`(mockSmartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.IDLE))

        viewModel =
            SharedMyEidViewModel(
                mockSmartCardReaderManager,
                mockIdCardService,
                nfcSmartCardReaderManager,
                dataStore,
            )

        viewModel.idCardData.observeForever(idCardDataObserver)
        viewModel.pinChangingState.observeForever(pinChangingStateObserver)
        viewModel.isPinBlocked.observeForever(isPinBlockedObserver)
        viewModel.identificationMethod.observeForever(identificationMethodObserver)
        viewModel.idCardStatus.observeForever(idCardStatusObserver)
        viewModel.errorState.observeForever(errorStateObserver)
    }

    @Test
    fun sharedMyEidViewModel_setIdentificationMethod_success() {
        val identificationMethod = MyEidIdentificationMethodSetting.ID_CARD
        viewModel.setIdentificationMethod(identificationMethod)

        assertEquals(identificationMethod, viewModel.identificationMethod.value)
    }

    @Test
    fun sharedMyEidViewModel_setIdCardData_success() {
        val idCardData = createMockIdCardData()
        viewModel.setIdCardData(idCardData)

        assertEquals(idCardData, viewModel.idCardData.value)
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeValid_returnTrue() {
        val result =
            viewModel.isPinCodeValid(
                codeType = CodeType.PIN1,
                currentPin = byteArrayOf(49, 50, 51, 52),
                newPin = byteArrayOf(49, 50, 51, 53),
                personalCode = "38703294793",
            )
        assertTrue(result)
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeValid_returnFalseNewWhenPinIsSameAsCurrent() {
        val result =
            viewModel.isPinCodeValid(
                codeType = CodeType.PIN1,
                currentPin = byteArrayOf(49, 50, 51, 52),
                newPin = byteArrayOf(49, 50, 51, 52),
                personalCode = "38703294793",
            )
        assertFalse(result)
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeValid_returnFalseWhenNewPinIsPartOfPersonalCode() {
        val result =
            viewModel.isPinCodeValid(
                codeType = CodeType.PIN1,
                currentPin = byteArrayOf(49, 50, 51, 52),
                newPin = byteArrayOf(55, 48, 51, 50),
                personalCode = "38703294793",
            )
        assertFalse(result)
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeValid_returnFalseWhenNewPinIsPartOfBirthDate() {
        val result =
            viewModel.isPinCodeValid(
                codeType = CodeType.PIN1,
                currentPin = byteArrayOf(49, 50, 51, 52),
                newPin = byteArrayOf(49, 57, 56, 55),
                personalCode = "38703294793",
            )
        assertFalse(result)
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeValid_returnFalseWhenNewPinIsTooEasy() {
        val result =
            viewModel.isPinCodeValid(
                codeType = CodeType.PIN1,
                currentPin = byteArrayOf(49, 50, 51, 52),
                newPin = byteArrayOf(49, 49, 49, 49),
                personalCode = "38703294793",
            )
        assertFalse(result)
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeValid_returnFalseWhenNewPinIsTooShort() {
        val result =
            viewModel.isPinCodeValid(
                codeType = CodeType.PIN1,
                currentPin = byteArrayOf(49, 50, 51, 52),
                newPin = byteArrayOf(49, 50, 51),
                personalCode = "38703294793",
            )
        assertFalse(result)
    }

    @Test
    fun sharedMyEidViewModel_pinCodesMatch_returnTrue() {
        val result = viewModel.pinCodesMatch(byteArrayOf(49, 50, 51, 52), byteArrayOf(49, 50, 51, 52))
        assertTrue(result)
    }

    @Test
    fun sharedMyEidViewModel_pinCodesMatch_returnFalse() {
        val result = viewModel.pinCodesMatch(byteArrayOf(49, 50, 51, 52), byteArrayOf(52, 51, 50, 49))
        assertFalse(result)
    }

    @Test
    fun sharedMyEidViewModel_isNewPinPartOfPersonalCode_returnTrue() {
        val result = viewModel.isNewPinPartOfPersonalCode(byteArrayOf(48, 51, 50, 57), "38703294793")
        assertTrue(result)
    }

    @Test
    fun sharedMyEidViewModel_isNewPinPartOfPersonalCode_returnFalse() {
        val result = viewModel.isNewPinPartOfPersonalCode(byteArrayOf(49, 50, 51, 52), "38703294793")
        assertFalse(result)
    }

    @Test
    fun sharedMyEidViewModel_isNewPinPartOfBirthDate_returnFalse() {
        val result = viewModel.isNewPinPartOfBirthDate(byteArrayOf(49, 50, 51, 52), "38703294793")
        assertFalse(result)
    }

    @Test
    fun sharedMyEidViewModel_isNewPinPartOfBirthDate_returnTrueWhenPinMatches_yyyy() {
        assertTrue(viewModel.isNewPinPartOfBirthDate(byteArrayOf(49, 57, 56, 55), "38703294793"))
    }

    @Test
    fun sharedMyEidViewModel_isNewPinPartOfBirthDate_returnTrueWhenPinMatches_MMdd() {
        assertTrue(viewModel.isNewPinPartOfBirthDate(byteArrayOf(48, 51, 50, 57), "38703294793"))
    }

    @Test
    fun sharedMyEidViewModel_isNewPinPartOfBirthDate_returnTrueWhenPinMatches_ddMM() {
        assertTrue(viewModel.isNewPinPartOfBirthDate(byteArrayOf(50, 57, 48, 51), "38703294793"))
    }

    @Test
    fun sharedMyEidViewModel_isNewPinPartOfBirthDate_returnTrueWhenPinMatches_ddMMyyyy() {
        assertTrue(viewModel.isNewPinPartOfBirthDate(byteArrayOf(50, 57, 48, 51, 49, 57, 56, 55), "38703294793"))
    }

    @Test
    fun sharedMyEidViewModel_isNewPinPartOfBirthDate_returnTrueWhenPinMatches_yyyyMM() {
        assertTrue(viewModel.isNewPinPartOfBirthDate(byteArrayOf(49, 57, 56, 55, 48, 51), "38703294793"))
    }

    @Test
    fun sharedMyEidViewModel_isNewPinPartOfBirthDate_returnTrueWhenPinMatches_yyyyMMdd() {
        assertTrue(viewModel.isNewPinPartOfBirthDate(byteArrayOf(49, 57, 56, 55, 48, 51, 50, 57), "38703294793"))
    }

    @Test
    fun sharedMyEidViewModel_isNewPinPartOfBirthDate_returnFalseWhenPinDoesNotMatchAnyFormat() {
        assertFalse(viewModel.isNewPinPartOfBirthDate(byteArrayOf(49, 50, 52, 54), "38703294793"))
    }

    @Test
    fun sharedMyEidViewModel_isNewPinPartOfBirthDate_returnFalseWhenPersonalCodeEmpty() {
        assertFalse(viewModel.isNewPinPartOfBirthDate(byteArrayOf(49, 50, 51, 52), ""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun sharedMyEidViewModel_isNewPinPartOfBirthDate_throwsIllegalArgumentExceptionForInvalidPersonalCode() {
        val invalidPersonalCode = "08702314793"
        viewModel.isNewPinPartOfBirthDate(byteArrayOf(49, 57, 56, 55), invalidPersonalCode)
    }

    @Test(expected = DateTimeException::class)
    fun sharedMyEidViewModel_isNewPinPartOfBirthDate_throwsDateTimeExceptionForInvalidPersonalCode() {
        val invalidPersonalCode = "38702314793"
        viewModel.isNewPinPartOfBirthDate(byteArrayOf(49, 57, 56, 55), invalidPersonalCode)
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeTooEasy_returnFalse() {
        assertFalse(viewModel.isPinCodeTooEasy(byteArrayOf(48, 51, 53, 55)))
        assertFalse(viewModel.isPinCodeTooEasy(byteArrayOf(49, 50, 52, 54)))
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeTooEasy_returnTrueForAscendingSequence() {
        assertTrue(viewModel.isPinCodeTooEasy(byteArrayOf(49, 50, 51, 52)))
        assertTrue(viewModel.isPinCodeTooEasy(byteArrayOf(48, 49, 50, 51, 52, 53)))
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeTooEasy_returnTrueForDescendingSequence() {
        assertTrue(viewModel.isPinCodeTooEasy(byteArrayOf(57, 56, 55, 54)))
        assertTrue(viewModel.isPinCodeTooEasy(byteArrayOf(51, 50, 49, 48)))
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeTooEasy_returnTrueForWrapAround9To0() {
        assertTrue(viewModel.isPinCodeTooEasy(byteArrayOf(55, 56, 57, 48)))
        assertTrue(viewModel.isPinCodeTooEasy(byteArrayOf(56, 57, 48, 49)))
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeTooEasy_returnTrueForWrapAround0To9() {
        assertTrue(viewModel.isPinCodeTooEasy(byteArrayOf(50, 49, 48, 57)))
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeTooEasy_returnTrueForRepeatingNonSequentialDigits() {
        assertFalse(viewModel.isPinCodeTooEasy(byteArrayOf(49, 49, 50, 50)))
        assertFalse(viewModel.isPinCodeTooEasy(byteArrayOf(53, 53, 54, 54)))
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeTooEasy_returnTrueForRepeatingSingleDigits() {
        assertTrue(viewModel.isPinCodeTooEasy(byteArrayOf(48, 48, 48, 48)))
        assertTrue(viewModel.isPinCodeTooEasy(byteArrayOf(49, 49, 49, 49)))
        assertTrue(viewModel.isPinCodeTooEasy(byteArrayOf(57, 57, 57, 57)))
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeTooEasy_returnsCorrectMinimumLengthForPIN1() {
        val result = viewModel.getPinCodeMinimumLength(CodeType.PIN1)
        assertEquals(Constant.MyEID.PIN1_MINIMUM_LENGTH, result)
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeTooEasy_returnsCorrectMinimumLengthForPIN2() {
        val result = viewModel.getPinCodeMinimumLength(CodeType.PIN2)
        assertEquals(Constant.MyEID.PIN2_MINIMUM_LENGTH, result)
    }

    @Test
    fun sharedMyEidViewModel_isPinCodeTooEasy_returnsCorrectMinimumLengthForPUK() {
        val result = viewModel.getPinCodeMinimumLength(CodeType.PUK)
        assertEquals(Constant.MyEID.PUK_MINIMUM_LENGTH, result)
    }

    @Test
    fun sharedMyEidViewModel_setScreenContent_setsCorrectContentForChangePin1() {
        viewModel.setScreenContent(PinChangeVariant.ChangePin1)
        assertEquals(
            PinChangeContent(R.string.myeid_pin_change_title, CodeType.PIN1),
            viewModel.pinScreenContent.value,
        )
    }

    @Test
    fun sharedMyEidViewModel_setScreenContent_setsCorrectContentForChangePin2() {
        viewModel.setScreenContent(PinChangeVariant.ChangePin2)
        assertEquals(
            PinChangeContent(R.string.myeid_pin_change_title, CodeType.PIN2),
            viewModel.pinScreenContent.value,
        )
    }

    @Test
    fun sharedMyEidViewModel_setScreenContent_setsCorrectContentForChangePuk() {
        viewModel.setScreenContent(PinChangeVariant.ChangePuk)
        assertEquals(
            PinChangeContent(R.string.myeid_pin_change_title, CodeType.PUK),
            viewModel.pinScreenContent.value,
        )
    }

    @Test
    fun sharedMyEidViewModel_setScreenContent_setsCorrectContentForForgotPin1() {
        viewModel.setScreenContent(PinChangeVariant.ForgotPin1)
        assertEquals(
            PinChangeContent(R.string.myeid_pin_unblock_title, CodeType.PIN1, true),
            viewModel.pinScreenContent.value,
        )
    }

    @Test
    fun sharedMyEidViewModel_setScreenContent_setsCorrectContentForForgotPin2() {
        viewModel.setScreenContent(PinChangeVariant.ForgotPin2)
        assertEquals(
            PinChangeContent(R.string.myeid_pin_unblock_title, CodeType.PIN2, true),
            viewModel.pinScreenContent.value,
        )
    }

    @Test
    fun sharedMyEidViewModel_editPin_success() =
        runTest {
            val mockIdCardData = createMockIdCardData()

            `when`(mockIdCardService.editPin(any(), any(), any(), any())).thenReturn(mockIdCardData)

            viewModel.editPin(mockToken, CodeType.PIN1, byteArrayOf(49, 50, 51, 52), byteArrayOf(52, 53, 54, 55))

            assertTrue(viewModel.pinChangingState.value == true)
            assertTrue(viewModel.idCardData.value == mockIdCardData)
            verify(errorStateObserver, never()).onChanged(any())
        }

    @Test
    fun sharedMyEidViewModel_editPin_handlesCodeVerificationExceptionWith2RetriesLeft() =
        runTest {
            val exception = CodeVerificationException(CodeType.PIN1, 2)
            `when`(mockIdCardService.editPin(any(), any(), any(), any())).thenThrow(exception)

            viewModel.editPin(mockToken, CodeType.PIN1, byteArrayOf(49, 50, 51, 52), byteArrayOf(52, 53, 54, 55))

            assertFalse(viewModel.pinChangingState.value == true)
            assertEquals(
                Triple(R.plurals.myeid_pin_error_code_verification, CodeType.PIN1.name, 2),
                viewModel.errorState.value,
            )
            assertFalse(viewModel.isPinBlocked.value == true)
        }

    @Test
    fun sharedMyEidViewModel_editPin_handlesCodeVerificationExceptionWith0RetriesLeft() =
        runTest {
            val exception = CodeVerificationException(CodeType.PIN2, 0)
            `when`(mockIdCardService.editPin(any(), any(), any(), any())).thenThrow(exception)

            viewModel.editPin(mockToken, CodeType.PIN2, byteArrayOf(49, 50, 51, 52), byteArrayOf(52, 53, 54, 55))

            assertFalse(viewModel.pinChangingState.value == true)
            assertTrue(viewModel.isPinBlocked.value == true)
            assertEquals(
                Triple(R.string.myeid_pin_blocked, CodeType.PIN2.name, null),
                viewModel.errorState.value,
            )
        }

    @Test
    fun sharedMyEidViewModel_editPin_handlesSmartCardReaderException() =
        runTest {
            val exception = SmartCardReaderException("Reader disconnected")
            `when`(mockIdCardService.editPin(any(), any(), any(), any())).thenThrow(exception)

            viewModel.editPin(
                mockToken,
                CodeType.PUK,
                byteArrayOf(49, 50, 51, 52, 53, 54, 55, 56),
                byteArrayOf(49, 50, 51, 52),
            )

            assertEquals(false, viewModel.pinChangingState.value)
            assertEquals(
                Triple(R.string.error_general_client, null, null),
                viewModel.errorState.value,
            )
        }

    @Test
    fun sharedMyEidViewModel_unblockAndEditPin_success() =
        runTest {
            val mockIdCardData = createMockIdCardData()

            `when`(mockIdCardService.unblockAndEditPin(any(), any(), any(), any())).thenReturn(mockIdCardData)

            viewModel.unblockAndEditPin(
                mockToken,
                CodeType.PIN1,
                byteArrayOf(49, 50, 51, 52),
                byteArrayOf(52, 53, 54, 55),
            )

            assertTrue(viewModel.pinChangingState.value == true)
            assertTrue(viewModel.idCardData.value == mockIdCardData)
            verify(errorStateObserver, never()).onChanged(any())
        }

    @Test
    fun sharedMyEidViewModel_unblockAndEditPin_handlesCodeVerificationExceptionWith2RetriesLeft() =
        runTest {
            val exception = CodeVerificationException(CodeType.PIN1, 2)
            `when`(mockIdCardService.unblockAndEditPin(any(), any(), any(), any())).thenThrow(exception)

            viewModel.unblockAndEditPin(
                mockToken,
                CodeType.PIN1,
                byteArrayOf(49, 50, 51, 52),
                byteArrayOf(52, 53, 54, 55),
            )

            assertFalse(viewModel.pinChangingState.value == true)
            assertEquals(
                Triple(R.plurals.myeid_pin_error_code_verification, CodeType.PIN1.name, 2),
                viewModel.errorState.value,
            )
            assertFalse(viewModel.isPinBlocked.value == true)
        }

    @Test
    fun sharedMyEidViewModel_unblockAndEditPin_handlesCodeVerificationExceptionWith0RetriesLeft() =
        runTest {
            val exception = CodeVerificationException(CodeType.PIN2, 0)
            `when`(mockIdCardService.unblockAndEditPin(any(), any(), any(), any())).thenThrow(exception)

            viewModel.unblockAndEditPin(
                mockToken,
                CodeType.PIN2,
                byteArrayOf(49, 50, 51, 52),
                byteArrayOf(52, 53, 54, 55),
            )

            assertFalse(viewModel.pinChangingState.value == true)
            assertFalse(viewModel.isPinBlocked.value == true)
            assertEquals(
                Triple(R.string.myeid_pin_blocked, CodeType.PIN2.name, null),
                viewModel.errorState.value,
            )
        }

    @Test
    fun sharedMyEidViewModel_unblockAndEditPin_handlesSmartCardReaderException() =
        runTest {
            val exception = SmartCardReaderException("Reader disconnected")
            `when`(mockIdCardService.unblockAndEditPin(any(), any(), any(), any())).thenThrow(exception)

            viewModel.unblockAndEditPin(
                mockToken,
                CodeType.PUK,
                byteArrayOf(49, 50, 51, 52, 53, 54, 55, 56),
                byteArrayOf(49, 50, 51, 52),
            )

            assertEquals(false, viewModel.pinChangingState.value)
            assertEquals(
                Triple(R.string.error_general_client, null, null),
                viewModel.errorState.value,
            )
        }

    @Test
    fun sharedMyEidViewModel_getNotAfter_success() {
        val cert = CertificateCreator.createSelfSignedCertificate()

        val notAfter = viewModel.getNotAfter(cert)

        val formattedNotAfterDate =
            DateUtil.formattedDateTime(
                cert?.notAfter?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate().toString(),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            ).date

        assertEquals(formattedNotAfterDate, notAfter)
    }

    @Test
    fun sharedMyEidViewModel_getNotAfter_returnsEmptyStringWhenCertIsNull() {
        val result = viewModel.getNotAfter(null)
        assertEquals("", result)
    }

    @Test
    fun sharedMyEidViewModel_getToken_success() {
        viewModel.setIdentificationMethod(MyEidIdentificationMethodSetting.ID_CARD)

        `when`(mockSmartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)
        `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))

        var resultToken: Token? = null
        var resultError: Exception? = null

        activityRule.scenario.onActivity { activity ->
            viewModel.getToken(activity) { token, error ->
                resultToken = token
                resultError = error
            }
        }

        assertNotNull(resultToken)
        assertNull(resultError)
    }

    @Test
    fun sharedMyEidViewModel_getToken_returnsSmartCardReaderException() {
        viewModel.setIdentificationMethod(MyEidIdentificationMethodSetting.ID_CARD)

        `when`(mockSmartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)
        `when`(mockSmartCardReader.atr()).thenReturn(byteArrayOf(49, 50, 51, 52))

        var resultToken: Token? = null
        var resultError: Exception? = null

        activityRule.scenario.onActivity { activity ->
            viewModel.getToken(activity) { token, error ->
                resultToken = token
                resultError = error
            }
        }

        assertNull(resultToken)
        assertNotNull(resultError)
    }

    @Test
    fun sharedMyEidViewModel_resetScreenContent_success() =
        runTest {
            viewModel.resetScreenContent()
            assertNull(viewModel.pinScreenContent.value)
        }

    @Test
    fun sharedMyEidViewModel_resetIdCardData_success() =
        runTest {
            viewModel.resetIdCardData()
            assertNull(viewModel.idCardData.value)
        }

    @Test
    fun sharedMyEidViewModel_resetPinChangingState_success() =
        runTest {
            viewModel.resetPinChangingState()
            assertFalse(viewModel.pinChangingState.value == true)
        }

    @Test
    fun sharedMyEidViewModel_resetErrorState_success() =
        runTest {
            viewModel.resetErrorState()
            verify(errorStateObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun sharedMyEidViewModel_resetIsPinBlocked_success() =
        runTest {
            viewModel.resetIsPinBlocked()
            assertFalse(viewModel.isPinBlocked.value == true)
        }

    @Test
    fun sharedMyEidViewModel_resetIdentificationMethod_success() =
        runTest {
            viewModel.resetIdentificationMethod()
            assertNull(viewModel.identificationMethod.value)
        }

    @Test
    fun sharedMyEidViewModel_resetValues_success() =
        runTest {
            viewModel.resetErrorState()
            viewModel.resetIsPinBlocked()
            viewModel.resetScreenContent()
            viewModel.resetPinChangingState()
            viewModel.resetIdentificationMethod()
            assertNull(viewModel.errorState.value)
            assertFalse(viewModel.isPinBlocked.value == true)
            assertNull(viewModel.pinScreenContent.value)
            assertFalse(viewModel.pinChangingState.value == true)
            assertNull(viewModel.identificationMethod.value)
        }
}
