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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.utils.monitoring.CrashDetector
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
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
            _hasUnsentReports.postValue(Tasks.forResult(false))
            crashDetector.deleteUnsentReports()
        }

        suspend fun sendUnsentReports() {
            hasUnsentReports.value?.let { task ->
                try {
                    withContext(IO) {
                        val result = Tasks.await(task)
                        if (result) {
                            _hasUnsentReports.postValue(Tasks.forResult(false))
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
