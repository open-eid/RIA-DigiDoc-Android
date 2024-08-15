@file:Suppress("PackageName")

package ee.ria.DigiDoc.manager

import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData

interface ActivityManager {
    val shouldRecreateActivity: LiveData<Boolean>

    fun setShouldRecreateActivity(shouldRecreate: Boolean)

    fun recreateActivity(mainActivity: ComponentActivity)
}
