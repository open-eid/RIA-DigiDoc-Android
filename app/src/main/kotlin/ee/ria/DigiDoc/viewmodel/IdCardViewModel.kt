@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.service.IdCardService
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.PersonalData
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderManager
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderStatus
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class IdCardViewModel
    @Inject
    constructor(
        private val smartCardReaderManager: SmartCardReaderManager,
        private val idCardService: IdCardService,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName

        private val _idCardStatus = MutableLiveData(SmartCardReaderStatus.IDLE)
        val idCardStatus: LiveData<SmartCardReaderStatus?> = _idCardStatus

        private val _userData = MutableLiveData<PersonalData?>(null)
        val userData: LiveData<PersonalData?> = _userData

        private val _signStatus = MutableLiveData<Boolean?>(null)
        val signStatus: LiveData<Boolean?> = _signStatus

        private val _signedContainer = MutableLiveData<SignedContainer?>(null)
        val signedContainer: LiveData<SignedContainer?> = _signedContainer

        private val _errorState = MutableLiveData<String?>(null)
        val errorState: LiveData<String?> = _errorState

        private val _pinErrorState = MutableLiveData<String?>(null)
        val pinErrorState: LiveData<String?> = _pinErrorState

        private val _dialogError = MutableLiveData<String?>(null)
        val dialogError: LiveData<String?> = _dialogError

        private val _roleDataRequested = MutableLiveData<Boolean?>(null)
        val roleDataRequested: LiveData<Boolean?> = _roleDataRequested

        init {
            CoroutineScope(Main).launch {
                smartCardReaderManager.status().asFlow().distinctUntilChanged().collect { status ->
                    _idCardStatus.postValue(status)
                }
            }
        }

        suspend fun loadPersonalData(context: Context) =
            withContext(IO) {
                try {
                    val token =
                        withContext(IO) {
                            Token.create(smartCardReaderManager.connectedReader())
                        }

                    val personalData = idCardService.data(token).personalData

                    _userData.postValue(personalData)
                } catch (e: Exception) {
                    _signStatus.postValue(false)

                    _errorState.postValue(
                        context.getString(R.string.error_general_client),
                    )
                    errorLog(logTag, "Unable to get ID-card personal data: ${e.message}", e)

                    resetValues()
                }
            }

        suspend fun sign(
            activity: Activity,
            context: Context,
            signedContainer: SignedContainer,
            pin2: ByteArray,
            roleData: RoleData?,
        ) {
            activity.requestedOrientation = activity.resources.configuration.orientation
            resetRoleDataRequested()

            try {
                val token: Token =
                    withContext(Main) {
                        Token.create(smartCardReaderManager.connectedReader())
                    }

                val signedContainerResult: SignedContainer =
                    withContext(IO) {
                        idCardService.signContainer(token, signedContainer, pin2, roleData)
                    }

                withContext(Main) {
                    _signStatus.postValue(true)
                    _signedContainer.postValue(signedContainerResult)
                }
            } catch (e: Exception) {
                handleSigningError(context, e, signedContainer)
            } finally {
                resetRoleDataRequested()
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        suspend fun removePendingSignature(signedContainer: SignedContainer) {
            val signatures = signedContainer.getSignatures(Main)
            if (signatures.isNotEmpty()) {
                val lastSignatureStatus = signatures.last().validator.status
                if (lastSignatureStatus == ValidatorInterface.Status.Invalid ||
                    lastSignatureStatus == ValidatorInterface.Status.Unknown
                ) {
                    signedContainer.removeSignature(signatures.last())
                }
            }
        }

        private suspend fun getPIN2RetryCount(context: Context): Int =
            withContext(IO) {
                try {
                    val token =
                        withContext(Main) {
                            Token.create(smartCardReaderManager.connectedReader())
                        }

                    val idCardData = idCardService.data(token)

                    idCardData.pin2RetryCount
                } catch (e: Exception) {
                    _signStatus.postValue(false)

                    _errorState.postValue(
                        context.getString(R.string.error_general_client),
                    )
                    errorLog(logTag, "Unable to get ID-card PIN2 retry count: ${e.message}", e)

                    resetValues()
                    -1
                }
            }

        private suspend fun handleSigningError(
            context: Context,
            e: Exception,
            signedContainer: SignedContainer,
        ) {
            removePendingSignature(signedContainer)
            _signStatus.postValue(false)

            val message = e.message ?: ""

            when {
                e is CodeVerificationException -> handlePin2Error(context)
                message.contains("Too Many Requests") ->
                    showErrorDialog(
                        e,
                        "Unable to sign with ID-card - Too Many Requests",
                    )
                message.contains("OCSP response not in valid time slot") ->
                    showErrorDialog(
                        e,
                        "Unable to sign with ID-card - OCSP response not in valid time slot",
                    )
                message.contains("Certificate status: revoked") -> showRevokedCertificateError(context, e)
                message.contains("Failed to connect") -> showNetworkError(context, e)
                message.contains("Failed to create proxy connection with host") -> showProxyError(context, e)
                else -> showGeneralError(context, e)
            }
        }

        private suspend fun handlePin2Error(context: Context) {
            val pin2RetryCount = getPIN2RetryCount(context)
            val pinErrorMessage =
                when {
                    pin2RetryCount > 1 -> context.getString(R.string.id_card_sign_pin2_invalid, pin2RetryCount)
                    pin2RetryCount == 1 -> context.getString(R.string.id_card_sign_pin2_invalid_final)
                    pin2RetryCount == 0 -> context.getString(R.string.id_card_sign_pin2_locked)
                    else -> context.getString(R.string.id_card_sign_pin2_wrong)
                }
            _pinErrorState.postValue(pinErrorMessage)
        }

        private fun showErrorDialog(
            e: Exception,
            logMessage: String,
        ) {
            _dialogError.postValue(e.message)
            errorLog(logTag, logMessage, e)
        }

        private fun showRevokedCertificateError(
            context: Context,
            e: Exception,
        ) {
            _errorState.postValue(
                context.getString(R.string.signature_update_signature_error_message_certificate_revoked),
            )
            errorLog(logTag, "Unable to sign with ID-card - Certificate status: revoked", e)
        }

        private fun showNetworkError(
            context: Context,
            e: Exception,
        ) {
            _errorState.postValue(context.getString(R.string.no_internet_connection))
            errorLog(logTag, "Unable to sign with ID-card - Unable to connect to Internet", e)
        }

        private fun showProxyError(
            context: Context,
            e: Exception,
        ) {
            _errorState.postValue(context.getString(R.string.main_settings_proxy_invalid_settings))
            errorLog(logTag, "Unable to sign with ID-card - Unable to create proxy connection with host", e)
        }

        private fun showGeneralError(
            context: Context,
            e: Exception,
        ) {
            _errorState.postValue(context.getString(R.string.error_general_client))
            errorLog(logTag, "Unable to sign with ID-card: ${e.message}", e)
        }

        fun resetRoleDataRequested() {
            _roleDataRequested.postValue(null)
        }

        fun setRoleDataRequested(roleDataRequested: Boolean) {
            _roleDataRequested.postValue(roleDataRequested)
        }

        fun resetSignStatus() {
            _signStatus.postValue(null)
        }

        fun resetErrorState() {
            _errorState.postValue(null)
        }

        fun resetDialogErrorState() {
            _dialogError.postValue(null)
        }

        fun resetSignedContainer() {
            _signedContainer.postValue(null)
        }

        fun resetPersonalUserData() {
            _userData.postValue(null)
        }

        private fun resetIdCardStatus() {
            _idCardStatus.postValue(null)
        }

        fun resetPINErrorState() {
            _pinErrorState.postValue(null)
        }

        private fun resetValues() {
            resetDialogErrorState()
            resetIdCardStatus()
            resetPersonalUserData()
            resetErrorState()
            resetPINErrorState()
            resetRoleDataRequested()
            resetSignStatus()
            resetSignedContainer()
        }
    }
