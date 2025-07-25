@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class WebEidViewModel
@Inject
constructor() : ViewModel() {

    private val _authPayload = MutableStateFlow<AuthRequest?>(null)
    val authPayload: StateFlow<AuthRequest?> = _authPayload

    fun handleAuth(uri: Uri) {
        try {
            val fragment = uri.fragment ?: return
            val decoded = decodeBase64(fragment)
            val json = JSONObject(decoded)

            val challenge = json.getString("challenge")
            val loginUri = json.getString("login_uri")
            val getSigningCertificate = json.optBoolean("get_signing_certificate", false)

            _authPayload.value = AuthRequest(
                challenge = challenge,
                loginUri = loginUri,
                getSigningCertificate = getSigningCertificate
            )
        } catch (e: Exception) {
            _authPayload.value = null
        }
    }

    private fun decodeBase64(encoded: String): String {
        return String(Base64.getDecoder().decode(encoded))
    }

    data class AuthRequest(
        val challenge: String,
        val loginUri: String,
        val getSigningCertificate: Boolean = false
    )
}