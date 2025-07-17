@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.secure

import android.app.Activity
import android.view.WindowManager
import ee.ria.DigiDoc.domain.preferences.DataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureUtil
    @Inject
    constructor(
        val dataStore: DataStore,
    ) {
        fun markAsSecure(activity: Activity?) {
            if (activity == null) {
                return
            }

            if (shouldMarkAsSecure()) {
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE,
                )
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }

        private fun shouldMarkAsSecure(): Boolean = !dataStore.getSettingsAllowScreenshots()
    }
