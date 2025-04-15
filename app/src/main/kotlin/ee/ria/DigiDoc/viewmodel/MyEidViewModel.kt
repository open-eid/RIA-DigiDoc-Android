@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.common.Constant
import ee.ria.DigiDoc.domain.model.pin.PinChoice
import javax.inject.Inject

@HiltViewModel
class MyEidViewModel
    @Inject
    constructor() : ViewModel() {
        private val logTag = "MyEidViewModel"

        fun isPinCodeLengthValid(
            pinChoice: PinChoice,
            pinCode: String,
        ): Boolean {
            return when (pinChoice) {
                PinChoice.PIN1 ->
                    pinCode.length in
                        Constant.MyEID.PIN1_MINIMUM_LENGTH..Constant.MyEID.PIN_MAXIMUM_LENGTH
                PinChoice.PIN2 ->
                    pinCode.length in
                        Constant.MyEID.PIN2_MINIMUM_LENGTH..Constant.MyEID.PIN_MAXIMUM_LENGTH
                PinChoice.PUK ->
                    pinCode.length in
                        Constant.MyEID.PUK_MINIMUM_LENGTH..Constant.MyEID.PIN_MAXIMUM_LENGTH
            }
        }

        fun getPinCodeMinimumLength(pinChoice: PinChoice): Int {
            return when (pinChoice) {
                PinChoice.PIN1 -> Constant.MyEID.PIN1_MINIMUM_LENGTH
                PinChoice.PIN2 -> Constant.MyEID.PIN2_MINIMUM_LENGTH
                PinChoice.PUK -> Constant.MyEID.PUK_MINIMUM_LENGTH
            }
        }
    }
