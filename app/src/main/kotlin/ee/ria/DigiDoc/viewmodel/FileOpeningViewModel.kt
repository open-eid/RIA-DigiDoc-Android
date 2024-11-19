@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.CONTAINER_MIME_TYPE
import ee.ria.DigiDoc.common.R.string.documents_add_error_exists
import ee.ria.DigiDoc.common.R.string.empty_file_error
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepository
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.exceptions.FileAlreadyExistsException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.stream.Collectors
import javax.inject.Inject

@HiltViewModel
class FileOpeningViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val contentResolver: ContentResolver,
        private val fileOpeningRepository: FileOpeningRepository,
        private val sivaRepository: SivaRepository,
        private val mimeTypeResolver: MimeTypeResolver,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName

        private val _signedContainer = MutableLiveData<SignedContainer?>(null)
        val signedContainer: LiveData<SignedContainer?> = _signedContainer

        private val _errorState = MutableLiveData<String?>(null)
        val errorState: LiveData<String?> = _errorState

        private val _launchFilePicker = MutableLiveData(true)
        val launchFilePicker: LiveData<Boolean?> = _launchFilePicker

        private val _filesAdded = MutableLiveData<List<File>?>(null)
        val filesAdded: LiveData<List<File>?> = _filesAdded

        fun resetContainer() {
            _signedContainer.postValue(null)
        }

        fun resetFilesAdded() {
            _filesAdded.postValue(null)
        }

        suspend fun showFileChooser(fileChooser: ActivityResultLauncher<String>) {
            fileOpeningRepository.showFileChooser(fileChooser, "*/*")
            // Do not launch file picker again after user presses the back button
            _launchFilePicker.postValue(false)
        }

        @Throws(FileNotFoundException::class, SecurityException::class)
        suspend fun urisToFile(
            context: Context,
            contentResolver: ContentResolver,
            uris: List<Uri>,
        ): List<File> {
            val files = mutableListOf<File>()
            for (uri in uris) {
                files.add(fileOpeningRepository.uriToFile(context, contentResolver, uri))
            }

            return files
        }

        suspend fun isSivaConfirmationNeeded(uris: List<Uri>): Boolean {
            try {
                val files = urisToFile(context, contentResolver, uris)
                return fileOpeningRepository.isSivaConfirmationNeeded(context, files)
            } catch (e: Exception) {
                errorLog(logTag, "Unable to check if SiVa confirmation is needed", e)
                handleException(context, e)
                return false
            }
        }

        suspend fun getFileMimetype(fileUris: List<Uri>): String? {
            try {
                val file =
                    urisToFile(context, contentResolver, fileUris)
                        .firstOrNull()
                if (file != null) {
                    return mimeTypeResolver.mimeType(file)
                }
            } catch (e: Exception) {
                errorLog(logTag, "Unable to get file mimetype", e)
                handleException(context, e)
            }
            return CONTAINER_MIME_TYPE
        }

        suspend fun handleFiles(
            context: Context,
            uris: List<Uri>,
            existingSignedContainer: SignedContainer? = null,
            isSivaConfirmed: Boolean,
        ) {
            if (existingSignedContainer != null) {
                try {
                    val files = urisToFile(context, contentResolver, uris)

                    if (files.size == 1) {
                        if (!fileOpeningRepository.isFileSizeValid(files.first())) {
                            throw EmptyFileException(context)
                        }
                        val isFileAlreadyInContainer =
                            fileOpeningRepository.isFileAlreadyInContainer(
                                files.first(),
                                existingSignedContainer,
                            )

                        if (isFileAlreadyInContainer) {
                            throw FileAlreadyExistsException(context, files.first().name)
                        }
                    }

                    val validFiles: List<File> =
                        fileOpeningRepository.getValidFiles(files, existingSignedContainer)

                    val filesAlreadyInContainer: List<File> =
                        files.stream()
                            .filter { file -> !validFiles.contains(file) }
                            .collect(Collectors.toList())

                    if (filesAlreadyInContainer.isNotEmpty()) {
                        val fileNames = filesAlreadyInContainer.joinToString(", ") { it.name }
                        _errorState.postValue(context.getString(documents_add_error_exists, fileNames))
                    }

                    fileOpeningRepository.addFilesToContainer(
                        context,
                        existingSignedContainer,
                        validFiles,
                    )
                    _signedContainer.postValue(existingSignedContainer)
                    _filesAdded.postValue(validFiles)
                } catch (e: Exception) {
                    _signedContainer.postValue(existingSignedContainer)
                    handleException(context, e)
                    errorLog(logTag, "Unable to add file to container", e)
                }
            } else {
                try {
                    val signedContainer =
                        fileOpeningRepository.openOrCreateContainer(
                            context,
                            contentResolver,
                            uris,
                            isSivaConfirmed,
                        )

                    if (sivaRepository.isTimestampedContainer(signedContainer, isSivaConfirmed) ||
                        signedContainer.isCades() && !signedContainer.isXades()
                    ) {
                        val nestedTimestampedContainer =
                            sivaRepository.getTimestampedContainer(
                                context,
                                signedContainer,
                            )
                        _signedContainer.postValue(nestedTimestampedContainer)
                    } else {
                        _signedContainer.postValue(signedContainer)
                    }

                    _filesAdded.postValue(urisToFile(context, contentResolver, uris))
                } catch (e: Exception) {
                    _signedContainer.postValue(null)
                    _launchFilePicker.postValue(false)
                    errorLog(logTag, "Unable to open or create container: ", e)

                    handleException(context, e)
                }
            }
        }

        private fun handleException(
            context: Context,
            e: Exception,
        ) {
            when (e) {
                is EmptyFileException -> {
                    _errorState.postValue(context.getString(empty_file_error))
                }
                is NoSuchElementException -> {
                    _errorState.postValue(context.getString(R.string.container_open_file_error))
                }
                is NoInternetConnectionException -> {
                    _errorState.postValue(context.getString(R.string.no_internet_connection))
                }
                is IOException -> {
                    val message = e.message ?: ""
                    if (message.startsWith("Failed to create connection with host")) {
                        _errorState.postValue(context.getString(R.string.no_internet_connection))
                    } else if (message.contains("Online validation disabled")) {
                        debugLog(logTag, "Unable to open container. Sending to SiVa not allowed", e)
                        _errorState.postValue("")
                        return
                    } else if (message.startsWith("Signature validation failed")) {
                        _errorState.postValue(context.getString(R.string.container_load_error))
                    } else {
                        _errorState.postValue(context.getString(R.string.container_open_file_error))
                    }
                }
                is FileAlreadyExistsException -> {
                    _errorState.postValue(e.localizedMessage)
                }
                else -> {
                    _errorState.postValue(context.getString(R.string.container_open_file_error))
                }
            }
        }

        fun handleCancelDdocMimeType(
            context: Context,
            isExternalFile: Boolean,
        ) {
            if (isExternalFile) {
                (context as? Activity)?.finish()
            }
        }

        suspend fun handleCancelAsicsMimeType(
            context: Context,
            fileUris: List<Uri>,
            signedContainer: SignedContainer?,
        ) {
            handleFiles(context, fileUris, signedContainer, false)
        }

        fun resetExternalFileState(sharedContainerViewModel: SharedContainerViewModel) {
            sharedContainerViewModel.setExternalFileUri(null)
        }
    }
