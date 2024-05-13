@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil
import java.text.ParseException
import javax.inject.Inject

@HiltViewModel
class SigningViewModel
    @Inject
    constructor() : ViewModel() {
        companion object {
            private const val LOG_TAG = "SigningViewModel"
        }

        var shouldResetSignedContainer = mutableStateOf(false)

        fun handleBackButton() {
            shouldResetSignedContainer.value = true
        }

        fun isExistingContainerNoSignatures(signedContainer: SignedContainer?): Boolean {
            return isContainerWithoutSignatures(signedContainer) &&
                signedContainer?.isExistingContainer() == true
        }

        fun isExistingContainer(signedContainer: SignedContainer?): Boolean {
            return signedContainer?.isExistingContainer() == true
        }

        fun isContainerWithoutSignatures(signedContainer: SignedContainer?): Boolean {
            return signedContainer?.getSignatures()?.isEmpty() == true
        }

        fun getFormattedDate(signingTime: String): String {
            try {
                return DateUtil.signedDateTimeString(signingTime)
            } catch (pe: ParseException) {
                LoggingUtil.errorLog(LOG_TAG, "Error parsing date: $signingTime", pe)
                return ""
            }
        }
    }
