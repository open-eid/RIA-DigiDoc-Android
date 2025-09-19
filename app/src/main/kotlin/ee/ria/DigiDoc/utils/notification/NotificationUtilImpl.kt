@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant
import ee.ria.DigiDoc.utilsLib.signing.PowerUtil
import javax.inject.Inject

class NotificationUtilImpl
    @Inject
    constructor(
        private val context: Context,
    ) : NotificationUtil {
        override fun hasNotificationPermission(): Boolean =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

        @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
        override fun sendNotification(
            title: String,
            message: String,
            isSilent: Boolean,
            channelId: String,
            channelName: String,
            priority: Int,
        ) {
            val channel =
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH,
                )
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val notification =
                NotificationCompat
                    .Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(priority)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setAutoCancel(true)
                    .setSilent(isSilent)
            if (PowerUtil.isPowerSavingMode(context)) {
                notification
                    .setSound(null)
                    .setVibrate(null)
                    .setLights(0, 0, 0)
            }

            NotificationManagerCompat
                .from(context)
                .notify(Constant.SmartIdConstants.NOTIFICATION_PERMISSION_CODE, notification.build())
        }

        override fun cancelNotification(notificationId: Int) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }
    }
