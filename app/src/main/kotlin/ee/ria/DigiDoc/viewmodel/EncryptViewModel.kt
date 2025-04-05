@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.UNSIGNABLE_CONTAINER_MIMETYPES
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.createContainerAction
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EncryptViewModel
    @Inject
    constructor(
        private val sivaRepository: SivaRepository,
        private val mimeTypeResolver: MimeTypeResolver,
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
            return cryptoContainer?.dataFiles?.any { (it?.length() ?: 0L) == 0L } ?: false
        }

        fun isContainerWithoutRecipients(cryptoContainer: CryptoContainer?): Boolean {
            return cryptoContainer?.hasRecipients() == false
        }

        fun isSignButtonShown(cryptoContainer: CryptoContainer?): Boolean =
            cryptoContainer != null &&
                (
                    !UNSIGNABLE_CONTAINER_MIMETYPES.contains(
                        cryptoContainer.file?.let { getMimetype(it) },
                    )
                ) &&
                (!isEmptyFileInContainer(cryptoContainer))

        fun isDecryptButtonShown(cryptoContainer: CryptoContainer?): Boolean =
            (isEncryptedContainer(cryptoContainer) || !isDecryptedContainer(cryptoContainer))

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
    }
