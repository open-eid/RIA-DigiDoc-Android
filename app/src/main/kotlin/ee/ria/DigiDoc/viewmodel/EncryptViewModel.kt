@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.CDOC1_EXTENSION
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepository
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.createContainerAction
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EncryptViewModel
    @Inject
    constructor(
        private val sivaRepository: SivaRepository,
        private val mimeTypeResolver: MimeTypeResolver,
        private val contentResolver: ContentResolver,
        private val fileOpeningRepository: FileOpeningRepository,
        private val cdoc2Settings: CDOC2Settings,
    ) : ViewModel() {
        private val _shouldResetCryptoContainer = MutableLiveData(false)
        val shouldResetCryptoContainer: LiveData<Boolean?> = _shouldResetCryptoContainer

        fun handleBackButton() {
            _shouldResetCryptoContainer.postValue(true)
        }

        fun isEncryptedContainer(cryptoContainer: CryptoContainer?): Boolean {
            return cryptoContainer?.encrypted == true
        }

        fun isDecryptedContainer(cryptoContainer: CryptoContainer?): Boolean {
            return cryptoContainer?.decrypted == true
        }

        fun isEmptyFileInContainer(cryptoContainer: CryptoContainer?): Boolean {
            return cryptoContainer?.dataFiles?.any { (it?.length() ?: 0L) == 0L } == true
        }

        fun isContainerWithoutRecipients(cryptoContainer: CryptoContainer?): Boolean {
            return cryptoContainer?.hasRecipients() == false
        }

        fun isCDOC1Container(cryptoContainer: CryptoContainer?): Boolean {
            return cryptoContainer?.file?.extension == CDOC1_EXTENSION
        }

        fun isDataFilesInContainer(cryptoContainer: CryptoContainer?): Boolean {
            return cryptoContainer?.dataFiles?.isEmpty() == false
        }

        fun shouldShowDataFiles(cryptoContainer: CryptoContainer?): Boolean {
            return (
                (isEncryptedContainer(cryptoContainer) && isCDOC1Container(cryptoContainer)) ||
                    !isEncryptedContainer(cryptoContainer)
            ) && isDataFilesInContainer(cryptoContainer)
        }

        fun isSignButtonShown(
            cryptoContainer: CryptoContainer?,
            isNestedContainer: Boolean,
        ): Boolean = isEncryptedContainer(cryptoContainer) && !isNestedContainer

        fun isDecryptButtonShown(
            cryptoContainer: CryptoContainer?,
            isNestedContainer: Boolean,
        ): Boolean = isEncryptedContainer(cryptoContainer) && !isNestedContainer

        fun isEncryptButtonShown(
            cryptoContainer: CryptoContainer?,
            isNestedContainer: Boolean,
        ): Boolean {
            if (isNestedContainer || isDecryptedContainer(cryptoContainer)) {
                return false
            }

            return (
                !isEncryptedContainer(cryptoContainer) &&
                    !isContainerWithoutRecipients(cryptoContainer)
            )
        }

        fun isShareButtonShown(cryptoContainer: CryptoContainer?): Boolean =
            isEncryptedContainer(cryptoContainer) || isDecryptedContainer(cryptoContainer)

        fun isContainerUnlocked(cryptoContainer: CryptoContainer?): Boolean {
            return !isEncryptedContainer(cryptoContainer) &&
                !isContainerWithoutRecipients(cryptoContainer)
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

        @Throws(Exception::class)
        suspend fun openSignedContainer(
            context: Context,
            signedFile: File?,
            sharedContainerViewModel: SharedContainerViewModel,
        ) {
            if (signedFile != null) {
                val uri = signedFile.toUri()
                val signedContainer =
                    fileOpeningRepository.openOrCreateContainer(
                        context,
                        contentResolver,
                        listOf(uri),
                        true,
                    )

                sharedContainerViewModel.setSignedContainer(signedContainer)
            }
        }

        @Throws(Exception::class)
        suspend fun openNestedContainer(
            context: Context,
            nestedFile: File?,
            sharedContainerViewModel: SharedContainerViewModel,
        ) {
            if (nestedFile != null) {
                val nestedContainer =
                    CryptoContainer.openOrCreate(
                        context,
                        nestedFile,
                        listOf(nestedFile),
                        cdoc2Settings,
                    )

                sharedContainerViewModel.setCryptoContainer(nestedContainer, false)
            }
        }
    }
