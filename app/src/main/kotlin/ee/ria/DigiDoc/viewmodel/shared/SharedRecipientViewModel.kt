@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.cryptolib.Addressee
import javax.inject.Inject

@HiltViewModel
class SharedRecipientViewModel
    @Inject
    constructor() : ViewModel() {
        private val _recipient = MutableLiveData<Addressee?>()
        val recipient: LiveData<Addressee?> = _recipient

        fun setRecipient(recipient: Addressee) {
            _recipient.postValue(recipient)
        }

        fun resetRecipient() {
            _recipient.postValue(null)
        }
    }
