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
import ee.ria.DigiDoc.common.exception.NoInternetConnectionException
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.cryptolib.exception.DataFilesEmptyException
import ee.ria.DigiDoc.cryptolib.exception.RecipientsEmptyException
import ee.ria.DigiDoc.cryptolib.repository.RecipientRepository
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EncryptRecipientViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val mimeTypeResolver: MimeTypeResolver,
        private val recipientRepository: RecipientRepository,
        private val cdoc2Settings: CDOC2Settings,
    ) : ViewModel() {
        private val _errorState = MutableLiveData<Int?>(null)
        val errorState: LiveData<Int?> = _errorState

        private val _queryText = MutableStateFlow("")
        val queryText = _queryText.asStateFlow()

        private val _searchText = MutableStateFlow("")
        val searchText = _searchText.asStateFlow()

        private val _recipientList = MutableStateFlow(getRecipientList())
        val recipientList = filterRecipients()

        private val _isRecipientAdded = MutableLiveData(false)
        val isRecipientAdded: LiveData<Boolean> = _isRecipientAdded

        private val _isContainerEncrypted = MutableLiveData(false)
        val isContainerEncrypted: LiveData<Boolean> = _isContainerEncrypted

        fun handleIsRecipientAdded(isRecipientAdded: Boolean) {
            _isRecipientAdded.postValue(isRecipientAdded)
        }

        fun handleIsContainerEncrypted(isContainerEncrypted: Boolean) {
            _isContainerEncrypted.postValue(isContainerEncrypted)
        }

        private fun filterRecipients() =
            queryText
                .combine(_recipientList) { text, recipients ->
                    if (!text.isEmpty()) {
                        var filteredRecipients = listOf<Addressee>()
                        try {
                            filteredRecipients = recipientRepository.find(context, text)
                        } catch (_: NoInternetConnectionException) {
                            _errorState.postValue(R.string.no_internet_connection)
                        }
                        filteredRecipients
                    } else {
                        listOf()
                    }
                }.stateIn(
                    scope = viewModelScope,
                    // It will allow the StateFlow survive 5 seconds before it been canceled
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = _recipientList.value,
                )

        fun getRecipientList(): List<Addressee> {
            return listOf<Addressee>()
        }

        fun getContainerRecipientList(sharedContainerViewModel: SharedContainerViewModel): List<Addressee> {
            val cryptoContainer = sharedContainerViewModel.cryptoContainer.value
            return cryptoContainer?.getRecipients() ?: listOf()
        }

        fun addRecipientToContainer(
            recipient: Addressee,
            sharedContainerViewModel: SharedContainerViewModel,
        ) {
            val cryptoContainer = sharedContainerViewModel.cryptoContainer.value

            if (cryptoContainer?.getRecipients()?.contains(recipient) == true) {
                _errorState.postValue(R.string.crypto_recipients_error_exists)
                handleIsRecipientAdded(false)
                return
            } else {
                cryptoContainer?.addRecipients(listOf(recipient))
            }

            sharedContainerViewModel.setCryptoContainer(cryptoContainer)
            handleIsRecipientAdded(true)
        }

        suspend fun encryptContainer(sharedContainerViewModel: SharedContainerViewModel) {
            var cryptoContainer = sharedContainerViewModel.cryptoContainer.value
            if (cryptoContainer != null) {
                try {
                    cryptoContainer =
                        CryptoContainer.encrypt(
                            context = context,
                            file = cryptoContainer.file,
                            dataFiles = cryptoContainer.dataFiles,
                            recipients = cryptoContainer.recipients,
                            cdoc2Settings = cdoc2Settings,
                        )
                    sharedContainerViewModel.setCryptoContainer(cryptoContainer)
                    handleIsContainerEncrypted(true)
                } catch (_: DataFilesEmptyException) {
                    _errorState.postValue(R.string.crypto_encrypt_data_files_empty_error)
                } catch (_: RecipientsEmptyException) {
                    _errorState.postValue(R.string.crypto_encrypt_recipients_empty_error)
                } catch (_: Exception) {
                    _errorState.postValue(R.string.crypto_encrypt_error)
                }
            } else {
                _errorState.postValue(R.string.crypto_encrypt_error)
            }
        }

        fun getMimetype(file: File): String? = mimeTypeResolver.mimeType(file)

        fun onSearchTextChange(text: String) {
            _queryText.value = ""
            _searchText.value = text
        }

        fun onQueryTextChange(text: String) {
            _queryText.value = text
        }
    }
