@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.security.cert.X509Certificate
import javax.inject.Inject

@HiltViewModel
class SharedCertificateViewModel
    @Inject
    constructor() : ViewModel() {
        private val _certificate = MutableLiveData<X509Certificate?>()
        val certificate: LiveData<X509Certificate?> = _certificate

        fun setCertificate(certificate: X509Certificate) {
            _certificate.postValue(certificate)
        }

        fun resetCertificate() {
            _certificate.postValue(null)
        }
    }
