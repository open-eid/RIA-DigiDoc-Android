@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.UNSIGNABLE_CONTAINER_EXTENSIONS
import ee.ria.DigiDoc.common.Constant.UNSIGNABLE_CONTAINER_MIMETYPES
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.createContainerAction
import ee.ria.DigiDoc.utilsLib.date.DateUtil.getFormattedDateTime
import ee.ria.DigiDoc.utilsLib.date.DateUtil.signedDateTimeString
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
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
            return signedContainer?.isSigned() == false
        }

        fun isEmptyFileInContainer(signedContainer: SignedContainer?): Boolean {
            return signedContainer?.rawContainer()?.dataFiles()?.any { it.fileSize() == 0L } ?: false
        }

        fun isContainerWithTimestamps(signedContainer: SignedContainer?): Boolean =
            signedContainer?.getTimestamps()?.isNotEmpty() == true

        fun isSignButtonShown(
            context: Context,
            signedContainer: SignedContainer?,
            isNestedContainer: Boolean,
        ): Boolean =
            signedContainer != null &&
                (!UNSIGNABLE_CONTAINER_MIMETYPES.contains(signedContainer.getContainerFile()?.mimeType(context))) &&
                (
                    !UNSIGNABLE_CONTAINER_EXTENSIONS.contains(
                        FilenameUtils
                            .getExtension(signedContainer.getName())
                            .lowercase(Locale.getDefault()),
                    ) && !isEmptyFileInContainer(signedContainer)
                ) &&
                !isNestedContainer

        fun isEncryptButtonShown(
            signedContainer: SignedContainer?,
            isNestedContainer: Boolean,
        ): Boolean = isExistingContainer(signedContainer) && !isNestedContainer

        fun isShareButtonShown(
            signedContainer: SignedContainer?,
            isNestedContainer: Boolean,
        ): Boolean = isExistingContainer(signedContainer) && !isNestedContainer

        fun isRoleEmpty(signature: SignatureInterface): Boolean {
            return signature.signerRoles.isEmpty() && signature.city.isEmpty() &&
                signature.stateOrProvince.isEmpty() &&
                signature.countryName.isEmpty() && signature.postalCode.isEmpty()
        }

        fun getFormattedDate(signingTime: String): String {
            try {
                return signedDateTimeString(
                    signedDateString = getFormattedDateTime(signingTime, false),
                    inputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss Z", Locale.getDefault()),
                )
            } catch (pe: ParseException) {
                errorLog(LOG_TAG, "Error parsing date: $signingTime", pe)
                return ""
            }
        }

        @Throws(Exception::class)
        suspend fun openNestedContainer(
            context: Context,
            nestedFile: File,
            sharedContainerViewModel: SharedContainerViewModel,
            isSivaConfirmed: Boolean,
        ) {
            val nestedContainer =
                SignedContainer.openOrCreate(
                    context,
                    nestedFile,
                    listOf(nestedFile),
                    isSivaConfirmed,
                )

            sharedContainerViewModel.setSignedContainer(nestedContainer)
        }

        fun getViewIntent(
            context: Context,
            file: File,
        ): Intent =
            createContainerAction(
                context = context,
                fileProviderAuthority = context.getString(R.string.file_provider_authority),
                file = file,
                mimeType = file.mimeType(context),
                action = Intent.ACTION_VIEW,
            )
    }
