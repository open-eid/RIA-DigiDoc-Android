@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.utils.monitoring.CrashDetector
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        val dataStore: DataStore,
        private var crashDetector: CrashDetector,
    ) : ViewModel() {
        private val logTag = "HomeViewModel"

        private val _hasUnsentReports = MutableLiveData<Task<Boolean>>()
        val hasUnsentReports: LiveData<Task<Boolean>> = _hasUnsentReports

        init {
            checkForUnsentReports()
        }

        fun didAppCrashOnPreviousExecution(): Boolean = crashDetector.didAppCrashOnPreviousExecution()

        fun isCrashSendingAlwaysEnabled(): Boolean = dataStore.getIsCrashSendingAlwaysEnabled()

        fun setCrashSendingAlwaysEnabled(isEnabled: Boolean) = dataStore.setIsCrashSendingAlwaysEnabled(isEnabled)

        fun deleteUnsentReports() {
            crashDetector.deleteUnsentReports()
        }

        suspend fun sendUnsentReports() {
            hasUnsentReports.value?.let { task ->
                try {
                    withContext(IO) {
                        val result = Tasks.await(task)
                        if (result) {
                            crashDetector.sendUnsentReports()
                        } else {
                            deleteUnsentReports()
                        }
                    }
                } catch (e: Exception) {
                    errorLog(logTag, "Unable to check unsent crash reports", e)
                }
            } ?: errorLog(logTag, "No task found to check unsent reports")
        }

        @Throws(Exception::class)
        private fun checkForUnsentReports() {
            try {
                _hasUnsentReports.postValue(crashDetector.checkForUnsentReports())
            } catch (e: Exception) {
                errorLog(logTag, "Unable to check for unsent crash reports", e)
                _hasUnsentReports.postValue(Tasks.forResult(false))
            }
        }
    }
