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
