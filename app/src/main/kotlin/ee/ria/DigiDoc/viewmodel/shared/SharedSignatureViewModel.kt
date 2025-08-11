@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import javax.inject.Inject

@HiltViewModel
class SharedSignatureViewModel
    @Inject
    constructor() : ViewModel() {
        private val _signature = MutableLiveData<SignatureInterface?>()
        val signature: LiveData<SignatureInterface?> = _signature

        private val _isTimestamp = MutableLiveData(false)
        val isTimestamp: LiveData<Boolean> = _isTimestamp

        fun setSignature(signature: SignatureInterface) {
            _signature.postValue(signature)
        }

        fun resetSignature() {
            _signature.postValue(null)
        }

        fun setIsTimestamp(isTimestamp: Boolean) {
            _isTimestamp.postValue(isTimestamp)
        }
    }
