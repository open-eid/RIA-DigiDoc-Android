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
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.UNSIGNABLE_CONTAINER_EXTENSIONS
import ee.ria.DigiDoc.common.Constant.UNSIGNABLE_CONTAINER_MIMETYPES
import ee.ria.DigiDoc.common.exception.NoInternetConnectionException
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepository
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.createContainerAction
import ee.ria.DigiDoc.utilsLib.extensions.containsDdoc
import ee.ria.DigiDoc.utilsLib.extensions.isCades
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.Locale
import javax.inject.Inject

sealed interface ExtendSignaturesResult {
    data class ExtendedInPlace(
        val signatures: List<SignatureInterface>,
    ) : ExtendSignaturesResult

    data class Wrapped(
        val signatures: List<SignatureInterface>,
        val file: File?,
        val needsSiva: Boolean,
    ) : ExtendSignaturesResult

    data object NoInternet : ExtendSignaturesResult

    data object Error : ExtendSignaturesResult
}

@HiltViewModel
class SigningViewModel
    @Inject
    constructor(
        private val sivaRepository: SivaRepository,
        private val mimeTypeResolver: MimeTypeResolver,
        private val fileOpeningRepository: FileOpeningRepository,
        private val contentResolver: ContentResolver,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName

        private val _shouldResetSignedContainer = MutableLiveData(false)
        val shouldResetSignedContainer: LiveData<Boolean?> = _shouldResetSignedContainer

        private val _hasEmptyFiles = MutableStateFlow(false)
        val hasEmptyFiles: StateFlow<Boolean> = _hasEmptyFiles

        fun handleBackButton() {
            _shouldResetSignedContainer.value = true
        }

        fun isExistingContainerNoSignatures(signedContainer: SignedContainer?): Boolean =
            isContainerWithoutSignatures(signedContainer) &&
                signedContainer?.isExistingContainer() == true

        fun isExistingContainer(signedContainer: SignedContainer?): Boolean =
            signedContainer?.isExistingContainer() == true

        fun isContainerWithoutSignatures(signedContainer: SignedContainer?): Boolean =
            signedContainer?.isSigned() == false

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
                    )
                ) &&
                !isNestedContainer &&
                !isXadesContainer &&
                !isCadesContainer

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

        fun isBottomContainerButtonShown(
            signedContainer: SignedContainer?,
            isNestedContainer: Boolean,
        ): Boolean = signedContainer?.isSigned() == false && !isNestedContainer

        fun isExtendSignaturesButtonShown(
            signedContainer: SignedContainer?,
            isNestedContainer: Boolean,
        ): Boolean = signedContainer?.isSigned() == true && !isNestedContainer

        fun isRoleEmpty(signature: SignatureInterface): Boolean =
            signature.signerRoles.isEmpty() &&
                signature.city.isEmpty() &&
                signature.stateOrProvince.isEmpty() &&
                signature.countryName.isEmpty() &&
                signature.postalCode.isEmpty()

        fun isEmptyFileInList(dataFiles: List<DataFileInterface>): Boolean =
            dataFiles.any { dataFile -> dataFile.fileSize == 0L }

        @Throws(Exception::class)
        suspend fun openCryptoContainer(
            context: Context,
            container: SignedContainer?,
            sharedContainerViewModel: SharedContainerViewModel,
        ) {
            if (container == null) return

            val uris = mutableListOf<Uri>()

            val containerFile = container.getContainerFile()
            val isSigned = container.isSigned()

            if (isSigned && containerFile != null) {
                uris += containerFile.toUri()
            } else {
                val dataFiles = container.getDataFiles()
                val dataFilesDir =
                    containerFile?.let {
                        ContainerUtil.getContainerDataFilesDir(context, it)
                    }

                dataFiles.mapTo(uris) { dataFile ->
                    container.getDataFile(dataFile, dataFilesDir).toUri()
                }
            }

            try {
                val cryptoContainer =
                    fileOpeningRepository.openOrCreateCryptoContainer(
                        context = context,
                        contentResolver = contentResolver,
                        uris = uris,
                        forceCreate = true,
                    )

                withContext(Main) {
                    sharedContainerViewModel.clearContainers()
                    sharedContainerViewModel.resetSignedContainer()
                    sharedContainerViewModel.resetCryptoContainer()
                    sharedContainerViewModel.resetContainerNotifications()
                    sharedContainerViewModel.resetIsSivaConfirmed()
                    sharedContainerViewModel.setCryptoContainer(cryptoContainer)
                }
            } catch (e: Exception) {
                errorLog(logTag, "Unable to open or create crypto container", e)
                return
            }
        }

        @Throws(Exception::class)
        suspend fun openNestedContainer(
            context: Context,
            nestedFile: File?,
            sharedContainerViewModel: SharedContainerViewModel,
            isSivaConfirmed: Boolean,
            overwriteContainer: Boolean = false,
        ) {
            if (nestedFile != null) {
                val nestedContainer =
                    SignedContainer.openOrCreate(
                        context,
                        nestedFile,
                        listOf(nestedFile),
                        isSivaConfirmed,
                    )

                if (ASICS_MIMETYPE == nestedFile.mimeType(context)) {
                    val timestampedNestedContainer =
                        getTimestampedContainer(context, nestedContainer, isSivaConfirmed)
                    sharedContainerViewModel.setSignedContainer(timestampedNestedContainer, overwriteContainer)
                } else {
                    sharedContainerViewModel.setSignedContainer(nestedContainer, overwriteContainer)
                }
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
            if (isTimestampedContainer(signedContainer)) {
                return sivaRepository.getTimestampedContainer(context, signedContainer, isSivaConfirmed)
            }

            return signedContainer
        }

        suspend fun isTimestampedContainer(signedContainer: SignedContainer): Boolean =
            sivaRepository.isTimestampedContainer(signedContainer) &&
                !signedContainer.isXades()

        suspend fun extendSignatures(
            context: Context,
            signedContainer: SignedContainer,
            sharedContainerViewModel: SharedContainerViewModel,
        ): ExtendSignaturesResult =
            withContext(IO) {
                try {
                    debugLog(logTag, "Extending signatures of '${signedContainer.getName()}' to LTA")
                    val extended = signedContainer.extendSignatures()
                    val isWrapped = extended !== signedContainer
                    if (isWrapped) {
                        withContext(Main) {
                            sharedContainerViewModel.setSignedContainer(extended, overwriteContainer = true)
                        }
                    }
                    val signatures = extended.getSignatures()
                    if (!isWrapped) {
                        debugLog(logTag, "Extended ${signatures.size} signature(s) in place")
                        return@withContext ExtendSignaturesResult.ExtendedInPlace(signatures)
                    }
                    val file = extended.getContainerFile()
                    val needsSiva = file != null && (file.containsDdoc() || file.isCades(context))
                    debugLog(logTag, "Wrapped into a new timestamped container '${file?.name}', with SiVa: $needsSiva")
                    ExtendSignaturesResult.Wrapped(signatures, file, needsSiva)
                } catch (e: NoInternetConnectionException) {
                    errorLog(logTag, "No internet connection while extending signatures", e)
                    ExtendSignaturesResult.NoInternet
                } catch (e: Exception) {
                    errorLog(logTag, "Failed to extend signatures: ${signedContainer.getName()}", e)
                    ExtendSignaturesResult.Error
                }
            }

        suspend fun createContainerForSignedPDF(
            context: Context,
            signedContainer: SignedContainer,
        ): SignedContainer? {
            val containerFile = signedContainer.getContainerFile() ?: return null

            return SignedContainer.openOrCreate(
                context = context,
                file = containerFile,
                dataFiles = listOf(containerFile),
                isSivaConfirmed = true,
                forceFirstDataFileContainer = true,
            )
        }
    }
