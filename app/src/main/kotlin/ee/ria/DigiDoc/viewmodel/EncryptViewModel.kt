/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.createContainerAction
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
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
        private val logTag = javaClass.simpleName

        private val _shouldResetCryptoContainer = MutableLiveData(false)
        val shouldResetCryptoContainer: LiveData<Boolean?> = _shouldResetCryptoContainer

        fun handleBackButton() {
            _shouldResetCryptoContainer.postValue(true)
        }

        fun isEncryptedContainer(cryptoContainer: CryptoContainer?): Boolean = cryptoContainer?.encrypted == true

        fun isDecryptedContainer(cryptoContainer: CryptoContainer?): Boolean = cryptoContainer?.decrypted == true

        fun isEmptyFileInContainer(cryptoContainer: CryptoContainer?): Boolean =
            cryptoContainer?.dataFiles?.any {
                it.length() == 0L
            } == true

        fun isContainerWithoutRecipients(cryptoContainer: CryptoContainer?): Boolean =
            cryptoContainer?.hasRecipients() == false

        fun isCDOC1Container(cryptoContainer: CryptoContainer?): Boolean =
            cryptoContainer?.file?.extension == CDOC1_EXTENSION

        fun isDataFilesInContainer(cryptoContainer: CryptoContainer?): Boolean =
            cryptoContainer?.dataFiles?.isEmpty() == false

        fun shouldShowDataFiles(cryptoContainer: CryptoContainer?): Boolean =
            (
                (isEncryptedContainer(cryptoContainer) && isCDOC1Container(cryptoContainer)) ||
                    !isEncryptedContainer(cryptoContainer)
            ) &&
                isDataFilesInContainer(cryptoContainer)

        fun isSignButtonShown(
            cryptoContainer: CryptoContainer?,
            isNestedContainer: Boolean,
        ): Boolean = isEncryptedContainer(cryptoContainer) && !isNestedContainer

        fun isDecryptButtonShown(
            cryptoContainer: CryptoContainer?,
            isNestedContainer: Boolean,
        ): Boolean {
            val base = isEncryptedContainer(cryptoContainer) && !isNestedContainer
            if (!base) return false

            val now = Date()
            val allExpired =
                cryptoContainer
                    ?.recipients
                    ?.all { recipient ->
                        recipient.validTo?.before(now) == true
                    } ?: false

            return !allExpired
        }

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

        fun isContainerUnlocked(cryptoContainer: CryptoContainer?): Boolean =
            !isEncryptedContainer(cryptoContainer) &&
                !isContainerWithoutRecipients(cryptoContainer)

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
            if (sivaRepository.isTimestampedContainer(signedContainer) &&
                !signedContainer.isXades()
            ) {
                return sivaRepository.getTimestampedContainer(context, signedContainer, isSivaConfirmed)
            }

            return signedContainer
        }

        @Throws(Exception::class)
        suspend fun openSignedContainer(
            context: Context,
            container: CryptoContainer?,
            sharedContainerViewModel: SharedContainerViewModel,
        ) {
            if (container == null) return

            val uris = mutableListOf<Uri>()

            val containerFile = container.file
            val isEncrypted = isEncryptedContainer(container)

            if (isEncrypted) {
                uris += containerFile.toUri()
            } else {
                val dataFiles = container.getDataFiles()
                val dataFilesDir =
                    containerFile.let {
                        ContainerUtil.getContainerDataFilesDir(context, it)
                    }

                dataFiles.mapTo(uris) { dataFile ->
                    container.getDataFile(dataFile, dataFilesDir).toUri()
                }
            }

            try {
                val signedContainer =
                    fileOpeningRepository.openOrCreateContainer(
                        context = context,
                        contentResolver = contentResolver,
                        uris = uris,
                        isSivaConfirmed = true,
                    )

                withContext(Main) {
                    sharedContainerViewModel.clearContainers()
                    sharedContainerViewModel.resetContainerNotifications()
                    sharedContainerViewModel.resetIsSivaConfirmed()
                    sharedContainerViewModel.resetCryptoContainer()
                    sharedContainerViewModel.resetSignedContainer()
                    sharedContainerViewModel.setSignedContainer(signedContainer)
                }
            } catch (e: Exception) {
                errorLog(logTag, "Unable to open or create signed container", e)
                return
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
