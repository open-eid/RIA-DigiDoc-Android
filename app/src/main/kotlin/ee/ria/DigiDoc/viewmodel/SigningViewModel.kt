@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.UNSIGNABLE_CONTAINER_EXTENSIONS
import ee.ria.DigiDoc.common.Constant.UNSIGNABLE_CONTAINER_MIMETYPES
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.createContainerAction
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SigningViewModel
    @Inject
    constructor(
        private val sivaRepository: SivaRepository,
        private val mimeTypeResolver: MimeTypeResolver,
    ) : ViewModel() {
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
            signedContainer: SignedContainer?,
            isNestedContainer: Boolean,
            isXadesContainer: Boolean,
            isCadesContainer: Boolean,
        ): Boolean =
            signedContainer != null &&
                (
                    !UNSIGNABLE_CONTAINER_MIMETYPES.contains(
                        signedContainer.getContainerFile()?.let { getMimetype(it) },
                    )
                ) &&
                (
                    !UNSIGNABLE_CONTAINER_EXTENSIONS.contains(
                        FilenameUtils
                            .getExtension(signedContainer.getName())
                            .lowercase(Locale.getDefault()),
                    ) && !isEmptyFileInContainer(signedContainer)
                ) &&
                !isNestedContainer && !isXadesContainer && !isCadesContainer

        fun isEncryptButtonShown(
            signedContainer: SignedContainer?,
            isNestedContainer: Boolean,
        ): Boolean =
            (
                isExistingContainer(signedContainer) ||
                    !isContainerWithoutSignatures(signedContainer)
            ) &&
                !isNestedContainer

        fun isShareButtonShown(
            signedContainer: SignedContainer?,
            isNestedContainer: Boolean,
        ): Boolean =
            (
                isExistingContainer(signedContainer) ||
                    !isContainerWithoutSignatures(signedContainer)
            ) &&
                !isNestedContainer

        fun isRoleEmpty(signature: SignatureInterface): Boolean {
            return signature.signerRoles.isEmpty() && signature.city.isEmpty() &&
                signature.stateOrProvince.isEmpty() &&
                signature.countryName.isEmpty() && signature.postalCode.isEmpty()
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

            if (ASICS_MIMETYPE == nestedFile.mimeType(context)) {
                val timestampedNestedContainer = getTimestampedContainer(context, nestedContainer, isSivaConfirmed)
                sharedContainerViewModel.setSignedContainer(timestampedNestedContainer)
            } else {
                sharedContainerViewModel.setSignedContainer(nestedContainer)
            }
        }

        fun getViewIntent(
            context: Context,
            file: File,
        ): Intent =
            createContainerAction(
                context = context,
                fileProviderAuthority = context.getString(R.string.file_provider_authority),
                file = file,
                mimeType = getMimetype(file) ?: "",
                action = Intent.ACTION_VIEW,
            )

        fun getMimetype(file: File): String? = mimeTypeResolver.mimeType(file)

        suspend fun getTimestampedContainer(
            context: Context,
            signedContainer: SignedContainer,
            isSivaConfirmed: Boolean,
        ): SignedContainer {
            if (sivaRepository.isTimestampedContainer(signedContainer, isSivaConfirmed) &&
                !signedContainer.isXades()
            ) {
                return sivaRepository.getTimestampedContainer(context, signedContainer)
            }

            return signedContainer
        }

        fun showMessage(message: String) {
            viewModelScope.launch {
                SnackBarManager.showMessage(message)
            }
        }
    }
