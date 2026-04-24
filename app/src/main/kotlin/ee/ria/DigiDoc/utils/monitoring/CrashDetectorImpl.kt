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

package ee.ria.DigiDoc.utils.monitoring

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class CrashDetectorImpl
    @Inject
    constructor(
        private val crashlytics: FirebaseCrashlytics,
    ) : CrashDetector {
        override fun didAppCrashOnPreviousExecution(): Boolean = crashlytics.didCrashOnPreviousExecution()

        @Throws(Exception::class)
        override fun checkForUnsentReports(): Task<Boolean> =
            try {
                crashlytics.checkForUnsentReports()
            } catch (e: Exception) {
                Tasks.forResult(false)
            }

        override fun sendUnsentReports() {
            crashlytics.sendUnsentReports()
        }

        override fun deleteUnsentReports() {
            crashlytics.deleteUnsentReports()
        }
    }
