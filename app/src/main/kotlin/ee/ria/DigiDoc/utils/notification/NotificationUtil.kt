@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.notification

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat.PRIORITY_HIGH

interface NotificationUtil {
    fun hasNotificationPermission(): Boolean

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun sendNotification(
        title: String,
        message: String,
        isSilent: Boolean = false,
        channelId: String,
        channelName: String,
        priority: Int = PRIORITY_HIGH,
    )

    fun cancelNotification(notificationId: Int)
}
