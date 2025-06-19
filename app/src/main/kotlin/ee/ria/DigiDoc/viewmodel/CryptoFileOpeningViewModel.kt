@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

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
import ee.ria.DigiDoc.common.R.string.documents_add_error_exists
import ee.ria.DigiDoc.common.R.string.empty_file_error
import ee.ria.DigiDoc.common.exception.NoInternetConnectionException
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepository
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.exceptions.FileAlreadyExistsException
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.stream.Collectors
import javax.inject.Inject

@HiltViewModel
class CryptoFileOpeningViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val contentResolver: ContentResolver,
        private val fileOpeningRepository: FileOpeningRepository,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName

        private val _cryptoContainer = MutableLiveData<CryptoContainer?>(null)
        val cryptoContainer: LiveData<CryptoContainer?> = _cryptoContainer

        private val _errorState = MutableLiveData<Pair<Int, String?>?>(null)
        val errorState: LiveData<Pair<Int, String?>?> = _errorState

        private val _launchFilePicker = MutableLiveData(true)
        val launchFilePicker: LiveData<Boolean?> = _launchFilePicker

        private val _filesAdded = MutableLiveData<List<File>?>(null)
        val filesAdded: LiveData<List<File>?> = _filesAdded
        private val _filesAddedToContainer = MutableLiveData<List<File>?>(null)
        val filesAddedToContainer: LiveData<List<File>?> = _filesAddedToContainer

        fun resetContainer() {
            _cryptoContainer.postValue(null)
        }

        fun resetFilesAdded() {
            _filesAdded.postValue(null)
            _filesAddedToContainer.postValue(null)
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
                handleException(e)
                return false
            }
        }

        suspend fun handleFiles(
            context: Context,
            uris: List<Uri>,
            existingSignedContainer: CryptoContainer? = null,
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
                        _errorState.postValue(Pair(documents_add_error_exists, fileNames))
                    }

                    fileOpeningRepository.addFilesToContainer(
                        context,
                        existingSignedContainer,
                        validFiles,
                    )
                    _cryptoContainer.postValue(existingSignedContainer)
                    _filesAddedToContainer.postValue(validFiles)
                } catch (e: Exception) {
                    _cryptoContainer.postValue(existingSignedContainer)
                    handleException(e)
                    errorLog(logTag, "Unable to add file to container", e)
                }
            } else {
                try {
                    val cryptoContainer =
                        fileOpeningRepository.openOrCreateCryptoContainer(
                            context,
                            contentResolver,
                            uris,
                        )

                    _cryptoContainer.postValue(cryptoContainer)

                    _filesAdded.postValue(urisToFile(context, contentResolver, uris))
                } catch (e: Exception) {
                    _cryptoContainer.postValue(null)
                    _launchFilePicker.postValue(false)
                    errorLog(logTag, "Unable to open or create container: ", e)

                    handleException(e)
                }
            }
        }

        private fun handleException(e: Exception) {
            when (e) {
                is EmptyFileException -> {
                    _errorState.postValue(Pair(empty_file_error, null))
                }
                is NoSuchElementException -> {
                    _errorState.postValue(Pair(R.string.container_open_file_error, null))
                }
                is NoInternetConnectionException -> {
                    _errorState.postValue(Pair(R.string.no_internet_connection, null))
                }
                is IOException -> {
                    val message = e.message ?: ""
                    if (message.startsWith("Failed to create connection with host") ||
                        message.contains("StorageFileLoadException[connection_failure]")
                    ) {
                        _errorState.postValue(Pair(R.string.no_internet_connection, null))
                    } else if (message.contains("Online validation disabled")) {
                        debugLog(logTag, "Unable to open container. Sending to SiVa not allowed", e)
                        _errorState.postValue(null)
                        return
                    } else if (message.startsWith("Signature validation failed")) {
                        _errorState.postValue(Pair(R.string.container_load_error, null))
                    } else {
                        _errorState.postValue(Pair(R.string.container_open_file_error, null))
                    }
                }
                is FileAlreadyExistsException -> {
                    _errorState.postValue(
                        Pair(ee.ria.DigiDoc.common.R.string.document_add_error_exists, e.getFileName()),
                    )
                }
                else -> {
                    _errorState.postValue(Pair(R.string.container_open_file_error, null))
                }
            }
        }

        fun resetExternalFileState(sharedContainerViewModel: SharedContainerViewModel) {
            sharedContainerViewModel.setExternalFileUris(listOf())
        }
    }
