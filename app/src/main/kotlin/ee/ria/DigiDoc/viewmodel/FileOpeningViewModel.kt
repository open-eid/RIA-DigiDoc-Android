@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.getFilesWithValidSize
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.parseUris
import ee.ria.DigiDoc.utilsLib.file.FileStream
import ee.ria.DigiDoc.utilsLib.file.FileUtil.normalizeString
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.DigiDoc.utilsLib.toast.ToastUtil.showMessage
import javax.inject.Inject

@HiltViewModel
class FileOpeningViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val contentResolver: ContentResolver,
        private val fileOpeningRepository: FileOpeningRepository,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName

        private val _signedContainer = MutableLiveData<SignedContainer?>(null)
        val signedContainer: LiveData<SignedContainer?> = _signedContainer

        private val _errorState = MutableLiveData<String?>(null)
        val errorState: LiveData<String?> = _errorState

        var launchFilePicker = mutableStateOf(true)

        suspend fun showFileChooser(fileChooser: ActivityResultLauncher<String>) {
            fileOpeningRepository.showFileChooser(fileChooser, "*/*")
            // Do not launch file picker again after user presses the back button
            launchFilePicker.value = false
        }

        suspend fun handleFiles(
            uris: List<Uri>,
            existingSignedContainer: SignedContainer?,
        ) {
            if (existingSignedContainer != null) {
                val validFiles: List<FileStream> =
                    getFilesWithValidSize(
                        parseUris(contentResolver, uris),
                    )
                if (ContainerUtil.isEmptyFileInList(validFiles)) {
                    Handler(Looper.getMainLooper()).post {
                        showMessage(context, R.string.empty_file_error)
                    }
                }
                val filesNotInContainer: List<FileStream> =
                    getFilesNotInContainer(
                        validFiles,
                    )
                if (filesNotInContainer.isEmpty()) {
                    Handler(Looper.getMainLooper()).post {
                        showMessage(context, R.string.signature_update_documents_add_error_exists)
                    }
                }

                try {
                    val container =
                        fileOpeningRepository.addFilesToContainer(
                            context,
                            existingSignedContainer,
                            filesNotInContainer,
                        )
                    _signedContainer.postValue(container)
                } catch (e: Exception) {
                    _signedContainer.postValue(existingSignedContainer)
                    errorLog(logTag, "Unable to add file to container", e)
                }
            } else {
                try {
                    val container =
                        fileOpeningRepository.openOrCreateContainer(
                            context,
                            contentResolver,
                            uris,
                        )
                    _signedContainer.postValue(container)
                } catch (e: Exception) {
                    _signedContainer.postValue(null)
                    errorLog(logTag, "Unable to open or create container", e)
                    if (e is EmptyFileException || e is NoSuchElementException ||
                        e is NoInternetConnectionException
                    ) {
                        _errorState.postValue(e.message)
                    } else {
                        _errorState.postValue(context.getString(R.string.container_open_file_error))
                    }
                }
            }
        }

        @Throws(Exception::class)
        private fun getFilesNotInContainer(validFiles: List<FileStream>): List<FileStream> {
            val filesNotInContainer: MutableList<FileStream> = ArrayList()
            val containerDataFileNames: MutableList<String> = ArrayList()
            if (validFiles.isNotEmpty()) {
                val signedContainer = SignedContainer.container()
                val dataFiles: List<DataFileInterface> =
                    signedContainer.getDataFiles()
                for (dataFile in dataFiles) {
                    containerDataFileNames.add(normalizeString(dataFile.fileName))
                }

                for (validFile in validFiles) {
                    if (!containerDataFileNames.contains(normalizeString(validFile.displayName))) {
                        filesNotInContainer.add(validFile)
                    }
                }

                return filesNotInContainer
            }

            return validFiles
        }
    }
