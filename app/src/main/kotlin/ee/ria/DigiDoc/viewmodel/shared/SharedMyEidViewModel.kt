@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.collect.ImmutableSet
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.domain.model.myeid.MyEidIdentificationMethodSetting
import ee.ria.DigiDoc.domain.model.pin.PinChangeVariant
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.domain.service.IdCardService
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.DateOfBirthUtil
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.idcard.TokenWithPace
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderManager
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderStatus
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReader
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager
import ee.ria.DigiDoc.ui.component.myeid.pinandcertificate.PinChangeContent
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class SharedMyEidViewModel
    @Inject
    constructor(
        private val smartCardReaderManager: SmartCardReaderManager,
        private val idCardService: IdCardService,
        private val nfcSmartCardReaderManager: NfcSmartCardReaderManager,
        val dataStore: DataStore,
    ) : ViewModel() {
        private val logTag = "SharedMyEidViewModel"

        private val _pinScreenContent = MutableStateFlow<PinChangeContent?>(null)
        val pinScreenContent: StateFlow<PinChangeContent?> = _pinScreenContent

        private val _idCardStatus = MutableLiveData(SmartCardReaderStatus.IDLE)
        val idCardStatus: LiveData<SmartCardReaderStatus?> = _idCardStatus

        private val _idCardData = MutableLiveData<IdCardData?>(null)
        val idCardData: LiveData<IdCardData?> = _idCardData

        private val _pinChangingState = MutableLiveData<Boolean>(false)
        val pinChangingState: LiveData<Boolean> = _pinChangingState

        private val _errorState = MutableLiveData<Triple<Int, String?, Int?>?>(null)
        val errorState: LiveData<Triple<Int, String?, Int?>?> = _errorState

        private val _isPinBlocked = MutableLiveData<Boolean>(false)
        val isPinBlocked: LiveData<Boolean> = _isPinBlocked

        private val _identificationMethod = MutableLiveData<MyEidIdentificationMethodSetting?>(null)
        val identificationMethod: LiveData<MyEidIdentificationMethodSetting?> = _identificationMethod

        init {
            CoroutineScope(Main).launch {
                smartCardReaderManager.status().asFlow().distinctUntilChanged().collect { status ->
                    _idCardStatus.postValue(status)
                }
            }
        }

        fun setIdentificationMethod(identificationMethod: MyEidIdentificationMethodSetting) {
            _identificationMethod.postValue(identificationMethod)
        }

        fun setIdCardData(idCardData: IdCardData) {
            _idCardData.postValue(idCardData)
        }

        fun isPinCodeValid(
            codeType: CodeType,
            currentPin: String,
            newPin: String,
            personalCode: String,
        ): Boolean {
            return isPinCodeLengthValid(codeType, newPin) &&
                !pinCodesMatch(currentPin, newPin) &&
                !isNewPinPartOfPersonalCode(newPin, personalCode) &&
                !isNewPinPartOfBirthDate(newPin, personalCode) &&
                !isPinCodeTooEasy(newPin)
        }

        fun pinCodesMatch(
            first: String,
            second: String,
        ): Boolean {
            return first == second
        }

        fun isNewPinPartOfPersonalCode(
            newPin: String,
            personalCode: String,
        ): Boolean {
            return personalCode.contains(newPin)
        }

        fun isNewPinPartOfBirthDate(
            pin: String,
            personalCode: String,
        ): Boolean {
            if (personalCode.isEmpty()) {
                return false
            }

            val dateOfBirth = DateOfBirthUtil.parseDateOfBirth(personalCode)
            val dateOfBirthValuesBuilder = ImmutableSet.builder<String>()
            if (dateOfBirth != null) {
                dateOfBirthValuesBuilder
                    .add(dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy")))
                    .add(dateOfBirth.format(DateTimeFormatter.ofPattern("MMdd")))
                    .add(dateOfBirth.format(DateTimeFormatter.ofPattern("ddMM")))
                    .add(dateOfBirth.format(DateTimeFormatter.ofPattern("ddMMyyyy")))
                    .add(dateOfBirth.format(DateTimeFormatter.ofPattern("yyyyMM")))
                    .add(dateOfBirth.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
            }
            val dateOfBirthValues = dateOfBirthValuesBuilder.build()

            return dateOfBirthValues.contains(pin)
        }

        fun isPinCodeTooEasy(pin: String): Boolean {
            var delta: Int? = null
            for (i in 0..<pin.length - 1) {
                val currentNumber = Character.getNumericValue(pin[i])
                val nextNumber = Character.getNumericValue(pin[i + 1])

                var d = currentNumber - nextNumber

                // Reset sequence
                if (abs(d.toDouble()) == 9.0) {
                    if ((currentNumber == 9 && nextNumber == 0)) {
                        d = -1
                    } else if ((currentNumber == 0 && nextNumber == 9)) {
                        d = 1
                    }
                }

                if (abs(d.toDouble()) > 1) {
                    return false
                }
                if (delta != null && delta != d) {
                    return false
                }
                delta = d
            }
            return true
        }

        fun isPinCodeLengthValid(
            codeType: CodeType,
            pinCode: String,
        ): Boolean {
            return when (codeType) {
                CodeType.PIN1 ->
                    pinCode.length in
                        Constant.MyEID.PIN1_MINIMUM_LENGTH..Constant.MyEID.PIN_MAXIMUM_LENGTH
                CodeType.PIN2 ->
                    pinCode.length in
                        Constant.MyEID.PIN2_MINIMUM_LENGTH..Constant.MyEID.PIN_MAXIMUM_LENGTH
                CodeType.PUK ->
                    pinCode.length in
                        Constant.MyEID.PUK_MINIMUM_LENGTH..Constant.MyEID.PIN_MAXIMUM_LENGTH
            }
        }

        fun getPinCodeMinimumLength(codeType: CodeType): Int {
            return when (codeType) {
                CodeType.PIN1 -> Constant.MyEID.PIN1_MINIMUM_LENGTH
                CodeType.PIN2 -> Constant.MyEID.PIN2_MINIMUM_LENGTH
                CodeType.PUK -> Constant.MyEID.PUK_MINIMUM_LENGTH
            }
        }

        fun setScreenContent(pinVariant: PinChangeVariant) {
            _pinScreenContent.value =
                when (pinVariant) {
                    PinChangeVariant.ChangePin1 ->
                        PinChangeContent(R.string.myeid_pin_change_title, CodeType.PIN1)
                    PinChangeVariant.ChangePin2 ->
                        PinChangeContent(R.string.myeid_pin_change_title, CodeType.PIN2)
                    PinChangeVariant.ChangePuk ->
                        PinChangeContent(R.string.myeid_pin_change_title, CodeType.PUK)
                    PinChangeVariant.ForgotPin1 ->
                        PinChangeContent(R.string.myeid_pin_unblock_title, CodeType.PIN1, true)
                    PinChangeVariant.ForgotPin2 ->
                        PinChangeContent(R.string.myeid_pin_unblock_title, CodeType.PIN2, true)
                }
        }

        fun resetScreenContent() {
            _pinScreenContent.value = null
        }

        @Throws(CodeVerificationException::class, SmartCardReaderException::class)
        suspend fun editPin(
            token: Token,
            codeType: CodeType,
            currentPin: ByteArray,
            newPin: ByteArray,
        ) {
            try {
                val idCardData = idCardService.editPin(token, codeType, currentPin, newPin)
                _pinChangingState.postValue(idCardData)
            } catch (cve: CodeVerificationException) {
                _pinChangingState.postValue(false)
                if (cve.retries == 0) {
                    _isPinBlocked.postValue(true)
                    _errorState.postValue(Triple(R.string.myeid_pin_blocked, cve.type.name, null))
                } else {
                    _errorState.postValue(
                        Triple(R.plurals.myeid_pin_error_code_verification, cve.type.name, cve.retries),
                    )
                }
            } catch (scre: SmartCardReaderException) {
                errorLog(
                    tag = logTag,
                    message = "Unable to change PIN code. ${scre.message}",
                    throwable = scre,
                )
                _pinChangingState.postValue(false)
                _errorState.postValue(Triple(R.string.error_general_client, null, null))
            }
        }

        @Throws(CodeVerificationException::class, SmartCardReaderException::class)
        suspend fun unblockAndEditPin(
            token: Token,
            codeType: CodeType,
            currentPuk: ByteArray,
            newPin: ByteArray,
        ) {
            resetIdCardData()

            try {
                val idCardData = idCardService.unblockAndEditPin(token, codeType, currentPuk, newPin)
                _pinChangingState.postValue(idCardData)
            } catch (cve: CodeVerificationException) {
                _pinChangingState.postValue(false)
                _errorState.postValue(Triple(R.plurals.myeid_pin_error_code_verification, cve.type.name, cve.retries))
            } catch (scre: SmartCardReaderException) {
                errorLog(
                    tag = logTag,
                    message = "Unable to unblock and change PIN code. ${scre.message}",
                    throwable = scre,
                )
                _pinChangingState.postValue(false)
                _errorState.postValue(Triple(R.string.error_general_client, null, null))
            }
        }

        fun getNotAfter(x509Certificate: X509Certificate?): String {
            if (x509Certificate == null) {
                return ""
            }
            return DateUtil.formattedDateTime(
                x509Certificate.notAfter.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString(),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            ).date
        }

        fun getToken(
            activity: Activity,
            onResult: (Token?, Exception?) -> Unit,
        ) {
            try {
                when (identificationMethod.value) {
                    MyEidIdentificationMethodSetting.NFC -> {
                        nfcSmartCardReaderManager.startDiscovery(activity) { reader, error ->
                            if (error != null) {
                                onResult(null, error)
                                return@startDiscovery
                            }

                            if (reader == null) {
                                onResult(
                                    null,
                                    SmartCardReaderException(
                                        activity.getString(R.string.error_general_client),
                                    ),
                                )
                                return@startDiscovery
                            }

                            handleNfcToken(reader, onResult)
                        }
                    }

                    else -> {
                        try {
                            val reader = smartCardReaderManager.connectedReader()
                            val token = Token.create(reader)
                            onResult(token, null)
                        } catch (e: Exception) {
                            onResult(null, e)
                        }
                    }
                }
            } catch (e: Exception) {
                onResult(null, e)
            }
        }

        private fun handleNfcToken(
            reader: NfcSmartCardReader,
            onResult: (Token?, Exception?) -> Unit,
        ) {
            try {
                val token = TokenWithPace.create(reader)
                val canNumber = dataStore.getCanNumber()

                token.tunnel(canNumber)
                onResult(token, null)
            } catch (e: Exception) {
                errorLog(logTag, "Unable to get NFC token", e)
                onResult(null, e)
            }
        }

        fun resetIdCardData() {
            _idCardData.postValue(null)
        }

        fun resetPinChangingState() {
            _pinChangingState.postValue(false)
        }

        fun resetErrorState() {
            _errorState.postValue(null)
        }

        fun resetIsPinBlocked() {
            _isPinBlocked.postValue(false)
        }

        fun resetIdentificationMethod() {
            _identificationMethod.postValue(null)
        }

        fun resetValues() {
            resetErrorState()
            resetIsPinBlocked()
            resetScreenContent()
            resetPinChangingState()
            resetIdentificationMethod()
        }
    }
