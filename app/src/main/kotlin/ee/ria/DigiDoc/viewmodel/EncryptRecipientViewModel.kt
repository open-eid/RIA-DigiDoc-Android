@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.cryptolib.Addressee
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
        private val mimeTypeResolver: MimeTypeResolver,
    ) : ViewModel() {
        private val _errorState = MutableLiveData<Int?>(null)
        val errorState: LiveData<Int?> = _errorState
        private val _searchText = MutableStateFlow("")
        val searchText = _searchText.asStateFlow()

        private val _recipientList = MutableStateFlow(getRecipientList())
        val recipientList = filterRecipients()

        private val _isRecipientAdded = MutableLiveData(false)
        val isRecipientAdded: LiveData<Boolean> = _isRecipientAdded

        fun handleIsRecipientAdded(isRecipientAdded: Boolean) {
            _isRecipientAdded.postValue(isRecipientAdded)
        }

        private fun filterRecipients() =
            searchText
                .combine(_recipientList) { text, recipients ->
                    recipients.filter { recipient ->
                        recipient.identifier.uppercase().contains(text.trim().uppercase()) ||
                            recipient.surname?.uppercase()?.contains(text.trim().uppercase()) == true ||
                            recipient.givenName?.uppercase()?.contains(text.trim().uppercase()) == true
                    }
                }.stateIn(
                    scope = viewModelScope,
                    // It will allow the StateFlow survive 5 seconds before it been canceled
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = _recipientList.value,
                )

        fun getRecipientList(): List<Addressee> {
            return listOf() // TODO: Implement recipient list retrieval and search
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
            cryptoContainer?.addRecipients(listOf(recipient))
            handleIsRecipientAdded(true)
        }

        fun getMimetype(file: File): String? = mimeTypeResolver.mimeType(file)

        fun onSearchTextChange(text: String) {
            _searchText.value = text
            _recipientList.value = getRecipientList()
        }
    }
