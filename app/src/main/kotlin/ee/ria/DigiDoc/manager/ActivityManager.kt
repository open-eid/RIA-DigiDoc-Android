// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.manager

import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData

interface ActivityManager {
    val shouldRecreateActivity: LiveData<Boolean>
    val shouldResetLogging: LiveData<Boolean>

    fun setShouldRecreateActivity(shouldRecreate: Boolean)

    fun setShouldResetLogging(shouldResetLogging: Boolean)

    fun recreateActivity(mainActivity: ComponentActivity)
}
