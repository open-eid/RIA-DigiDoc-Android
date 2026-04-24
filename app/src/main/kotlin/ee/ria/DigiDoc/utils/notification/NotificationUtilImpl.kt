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
