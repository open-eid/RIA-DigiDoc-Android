@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
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
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
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

        suspend fun showFileChooser(fileChooser: ManagedActivityResultLauncher<String, List<Uri>>) {
            fileOpeningRepository.showFileChooser(fileChooser, "*/*")
            // Do not launch file picker again after user presses the back button
            launchFilePicker.value = false
        }

        suspend fun handleFiles(uris: List<Uri>) {
            try {
                val container = fileOpeningRepository.openOrCreateContainer(context, contentResolver, uris)
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
