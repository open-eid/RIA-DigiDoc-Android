@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.pin.PinChangeVariant
import ee.ria.DigiDoc.domain.model.pin.PinChoice
import ee.ria.DigiDoc.ui.component.myeid.pinandcertificate.PinChangeContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SharedPinViewModel
    @Inject
    constructor() : ViewModel() {
        private val _screenContent = MutableStateFlow<PinChangeContent?>(null)
        val screenContent: StateFlow<PinChangeContent?> = _screenContent

        fun setScreenContent(pinVariant: PinChangeVariant) {
            _screenContent.value =
                when (pinVariant) {
                    PinChangeVariant.ChangePin1 ->
                        PinChangeContent(R.string.myeid_pin_change_title, PinChoice.PIN1)
                    PinChangeVariant.ChangePin2 ->
                        PinChangeContent(R.string.myeid_pin_change_title, PinChoice.PIN2)
                    PinChangeVariant.ChangePuk ->
                        PinChangeContent(R.string.myeid_pin_change_title, PinChoice.PUK)
                    PinChangeVariant.ForgotPin1 ->
                        PinChangeContent(R.string.myeid_pin_unblock_title, PinChoice.PIN1, true)
                    PinChangeVariant.ForgotPin2 ->
                        PinChangeContent(R.string.myeid_pin_unblock_title, PinChoice.PIN2, true)
                }
        }

        fun resetScreenContent() {
            _screenContent.value = null
        }
    }
