// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.monitoring

import com.google.android.gms.tasks.Task

interface CrashDetector {
    fun didAppCrashOnPreviousExecution(): Boolean

    fun checkForUnsentReports(): Task<Boolean>

    fun sendUnsentReports()

    fun deleteUnsentReports()
}
