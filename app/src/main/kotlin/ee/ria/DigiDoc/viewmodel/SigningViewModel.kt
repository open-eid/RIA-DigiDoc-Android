@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.common.Constant.UNSIGNABLE_CONTAINER_EXTENSIONS
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil
import org.apache.commons.io.FilenameUtils
import java.text.ParseException
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SigningViewModel
    @Inject
    constructor() : ViewModel() {
        companion object {
            private const val LOG_TAG = "SigningViewModel"
        }

        private val _shouldResetSignedContainer = MutableLiveData(false)
        val shouldResetSignedContainer: LiveData<Boolean?> = _shouldResetSignedContainer

        fun handleBackButton() {
            _shouldResetSignedContainer.postValue(true)
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

        fun isEmptyFileInContainer(signedContainer: SignedContainer?): Boolean {
            return signedContainer?.getDataFiles()?.any { it.fileSize == 0L } ?: false
        }

        fun isSignButtonShown(signedContainer: SignedContainer?): Boolean {
            return signedContainer != null && (
                !UNSIGNABLE_CONTAINER_EXTENSIONS.contains(
                    FilenameUtils.getExtension(signedContainer.getName())
                        .lowercase(Locale.getDefault()),
                ) && !isEmptyFileInContainer(signedContainer)
            )
        }

        fun isEncryptButtonShown(signedContainer: SignedContainer?): Boolean {
            return isExistingContainer(signedContainer)
        }

        fun isShareButtonShown(signedContainer: SignedContainer?): Boolean {
            return isExistingContainer(signedContainer)
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
