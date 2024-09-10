@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DDOC_MIMETYPE
import ee.ria.DigiDoc.common.Constant.SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecentDocumentsViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val sivaRepository: SivaRepository,
    ) : ViewModel() {
        private val _sendToSigningViewWithSiva = MutableLiveData(false)
        val sendToSigningViewWithSiva: LiveData<Boolean> = _sendToSigningViewWithSiva

        private val _errorState = MutableLiveData<Int?>(null)
        val errorState: LiveData<Int?> = _errorState

        @Throws(Exception::class)
        suspend fun openDocument(
            document: File,
            isSivaConfirmed: Boolean,
        ): SignedContainer {
            val signedContainer = SignedContainer.openOrCreate(context, document, listOf(document), isSivaConfirmed)
            if (sivaRepository.isTimestampedContainer(signedContainer, isSivaConfirmed)) {
                return sivaRepository.getTimestampedContainer(context, signedContainer)
            }

            return SignedContainer.openOrCreate(context, document, listOf(document), isSivaConfirmed)
        }

        fun getRecentDocumentList(): List<File> {
            return ContainerUtil.findSignatureContainerFiles(context)
        }

        suspend fun handleDocument(
            document: File,
            mimeType: String,
            confirmed: Boolean,
            sharedContainerViewModel: SharedContainerViewModel,
        ) {
            if (SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES.contains(mimeType)) {
                if (mimeType == ASICS_MIMETYPE || (mimeType == DDOC_MIMETYPE && confirmed)) {
                    val signedContainer = openDocument(document, confirmed)
                    sharedContainerViewModel.setSignedContainer(signedContainer)

                    handleSendToSigningViewWithSiva(true)
                }
            }
        }

        fun handleSendToSigningViewWithSiva(sendToSigningView: Boolean) {
            _sendToSigningViewWithSiva.postValue(sendToSigningView)
        }

        suspend fun handleError(
            logTag: String,
            ex: Exception,
        ) {
            errorLog(logTag, "Unable to open container from recent documents", ex)

            var errorMessage = R.string.signature_update_mobile_id_error_general_client

            withContext(Main) {
                if (ex is NoInternetConnectionException) {
                    errorMessage = R.string.no_internet_connection
                }

                _errorState.postValue(errorMessage)
            }
        }
    }
