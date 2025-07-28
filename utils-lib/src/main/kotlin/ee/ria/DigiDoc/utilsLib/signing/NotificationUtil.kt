@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.signing

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object NotificationUtil {
    fun createNotificationChannel(
        context: Context,
        notificationChannel: String?,
        channelName: String?,
    ) {
        val channel =
            NotificationChannel(
                notificationChannel,
                channelName,
                NotificationManager.IMPORTANCE_HIGH,
            )
        val systemService =
            context.getSystemService(
                NotificationManager::class.java,
            )
        systemService?.createNotificationChannel(channel)
    }

    fun createNotification(
        context: Context?,
        notificationChannel: String?,
        smallIcon: Int,
        title: String?,
        text: String?,
        priority: Int,
        isSilent: Boolean,
    ): Notification? {
        val notification =
            context?.let {
                if (notificationChannel != null) {
                    NotificationCompat
                        .Builder(
                            it,
                            notificationChannel,
                        ).setSmallIcon(smallIcon)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setPriority(priority)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .setAutoCancel(true)
                        .setSilent(isSilent)
                } else {
                    null
                }
            }
        if (context?.let { PowerUtil.isPowerSavingMode(it) } == true) {
            notification?.setSound(null)?.setVibrate(null)?.setLights(0, 0, 0)
        }
        if (notification != null) {
            return notification.build()
        }
        return null
    }
}
