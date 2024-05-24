@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.io.ByteStreams
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

@HiltViewModel
class SharedContainerViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val contentResolver: ContentResolver,
        private val fileOpeningRepository: FileOpeningRepository,
    ) : ViewModel() {
        private val _signedContainer = MutableLiveData<SignedContainer?>()
        val signedContainer: LiveData<SignedContainer?> = _signedContainer

        private val _signedMidStatus = MutableLiveData<MobileCreateSignatureProcessStatus?>(null)
        val signedMidStatus: LiveData<MobileCreateSignatureProcessStatus?> = _signedMidStatus

        private val _signedSidStatus = MutableLiveData<SessionStatusResponseProcessStatus?>(null)
        val signedSidStatus: LiveData<SessionStatusResponseProcessStatus?> = _signedSidStatus

        fun setSignedSidStatus(signedStatus: SessionStatusResponseProcessStatus?) {
            _signedSidStatus.postValue(signedStatus)
        }

        fun setSignedMidStatus(signedStatus: MobileCreateSignatureProcessStatus?) {
            _signedMidStatus.postValue(signedStatus)
        }

        fun setSignedContainer(signedContainer: SignedContainer?) {
            _signedContainer.postValue(signedContainer)
        }

        fun resetSignedContainer() {
            SignedContainer.cleanup()
            _signedContainer.postValue(null)
        }

        fun getContainerDataFile(
            signedContainer: SignedContainer?,
            dataFile: DataFileInterface,
        ): File? {
            return signedContainer?.getDataFile(
                dataFile,
                signedContainer.getContainerFile()?.let {
                    ContainerUtil.getContainerDataFilesDir(
                        context,
                        it,
                    )
                },
            )
        }

        fun removeContainerDataFile(
            signedContainer: SignedContainer?,
            dataFile: DataFileInterface?,
        ) {
            val container = dataFile?.let { signedContainer?.removeDataFile(it) }
            _signedContainer.postValue(container)
        }

        fun saveContainerFile(
            documentFile: File,
            activityResult: ActivityResult,
        ) {
            FileInputStream(documentFile).use { inputStream ->
                activityResult.data?.data?.let {
                    contentResolver
                        .openOutputStream(it).use { outputStream ->
                            if (outputStream != null) {
                                ByteStreams.copy(inputStream, outputStream)
                            }
                        }
                }
            }
        }

        fun removeSignature(
            signedContainer: SignedContainer?,
            signature: SignatureInterface?,
        ) {
            val container = signature?.let { signedContainer?.removeSignature(it) }
            _signedContainer.postValue(container)
        }
    }
