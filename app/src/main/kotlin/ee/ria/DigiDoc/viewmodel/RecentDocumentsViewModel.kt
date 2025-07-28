@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DDOC_MIMETYPE
import ee.ria.DigiDoc.common.exception.NoInternetConnectionException
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.extensions.isCades
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RecentDocumentsViewModel
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val sivaRepository: SivaRepository,
        private val mimeTypeResolver: MimeTypeResolver,
        private val cdoc2Settings: CDOC2Settings,
    ) : ViewModel() {
        private val _sendToSigningViewWithSiva = MutableLiveData(false)
        val sendToSigningViewWithSiva: LiveData<Boolean> = _sendToSigningViewWithSiva

        private val _errorState = MutableLiveData<Int?>(null)
        val errorState: LiveData<Int?> = _errorState
        private val _searchText = MutableStateFlow("")
        val searchText = _searchText.asStateFlow()

        private val _documentList = MutableStateFlow(getRecentDocumentList())
        val documentList = filterDocuments()

        private fun filterDocuments() =
            searchText
                .combine(_documentList) { text, documents ->
                    documents.filter { document ->
                        document.name.uppercase().contains(text.trim().uppercase())
                    }
                }.stateIn(
                    scope = viewModelScope,
                    // It will allow the StateFlow survive 5 seconds before it been canceled
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = _documentList.value,
                )

        @Throws(Exception::class)
        suspend fun openDocument(
            document: File,
            isSivaConfirmed: Boolean,
        ): SignedContainer {
            val signedContainer = SignedContainer.openOrCreate(context, document, listOf(document), isSivaConfirmed)
            if (sivaRepository.isTimestampedContainer(signedContainer, isSivaConfirmed) &&
                !signedContainer.isXades()
            ) {
                return sivaRepository.getTimestampedContainer(context, signedContainer)
            }

            return SignedContainer.openOrCreate(context, document, listOf(document), isSivaConfirmed)
        }

        @Throws(Exception::class)
        suspend fun openCryptoDocument(document: File): CryptoContainer =
            CryptoContainer.openOrCreate(context, document, listOf(document), cdoc2Settings)

        fun getRecentDocumentList(): List<File> = ContainerUtil.findRecentContainerFiles(context)

        suspend fun handleDocument(
            document: File,
            mimeType: String,
            confirmed: Boolean,
            sharedContainerViewModel: SharedContainerViewModel,
        ) {
            val isCades = document.isCades(context)
            val isAsicsOrConfirmedDdoc = mimeType == ASICS_MIMETYPE || (mimeType == DDOC_MIMETYPE && confirmed)

            if (isAsicsOrConfirmedDdoc || isCades) {
                val signedContainer = openDocument(document, confirmed)
                sharedContainerViewModel.setSignedContainer(signedContainer)

                handleSendToSigningViewWithSiva(true)
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

            var errorMessage: Int? = R.string.error_general_client

            withContext(Main) {
                val exceptionMessage = ex.message ?: ""
                if (ex is IOException &&
                    exceptionMessage.isNotEmpty() &&
                    exceptionMessage.contains("Online validation disabled")
                ) {
                    errorMessage = null
                    return@withContext
                }

                if (ex is NoInternetConnectionException) {
                    errorMessage = R.string.no_internet_connection
                }

                _errorState.postValue(errorMessage)
            }
        }

        fun getMimetype(file: File): String? = mimeTypeResolver.mimeType(file)

        fun onSearchTextChange(text: String) {
            _searchText.value = text
            _documentList.value = getRecentDocumentList()
        }
    }
