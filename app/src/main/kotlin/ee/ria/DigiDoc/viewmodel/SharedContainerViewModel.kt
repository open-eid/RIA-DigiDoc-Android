@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import javax.inject.Inject

@HiltViewModel
class SharedContainerViewModel
    @Inject
    constructor() : ViewModel() {
        private val _signedContainer = MutableLiveData<SignedContainer?>()
        val signedContainer: LiveData<SignedContainer?> = _signedContainer

        fun setSignedContainer(signedContainer: SignedContainer?) {
            _signedContainer.postValue(signedContainer)
        }

        fun resetSignedContainer() {
            _signedContainer.postValue(null)
        }
    }
