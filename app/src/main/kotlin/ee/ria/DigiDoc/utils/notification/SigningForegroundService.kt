// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import ee.ria.DigiDoc.MainActivity
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.utils.locale.LocaleUtilImpl
import ee.ria.DigiDoc.utilsLib.locale.LocaleUtil.getLocale
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog

class SigningForegroundService : Service() {
    private val logTag = javaClass.simpleName

    override fun attachBaseContext(newBase: Context?) {
        val localeUtilImpl = LocaleUtilImpl()
        val language = localeUtilImpl.getPreferredLanguage(newBase)
        val localizedContext = newBase?.let { localeUtilImpl.updateLocale(it, getLocale(language)) }
        super.attachBaseContext(localizedContext)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(),
                FOREGROUND_SERVICE_TYPE_SHORT_SERVICE,
            )
        } catch (e: Exception) {
            errorLog(logTag, "Unable to start signing foreground service", e)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onTimeout(startId: Int) {
        debugLog(logTag, "Signing foreground service reached its platform time limit")
        stopSelf()
    }

    override fun onTimeout(
        startId: Int,
        fgsType: Int,
    ) {
        onTimeout(startId)
    }

    private fun buildNotification(): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL,
                getString(R.string.signing_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )

        val contentIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        return NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.signing_in_progress))
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_CHANNEL = "SIGNING_SESSION_CHANNEL"
        private const val NOTIFICATION_ID = 2

        fun start(context: Context) {
            try {
                val appContext = context.applicationContext
                appContext.startService(Intent(appContext, SigningForegroundService::class.java))
            } catch (e: Exception) {
                errorLog(
                    SigningForegroundService::class.java.simpleName,
                    "Unable to keep the process alive for signing",
                    e,
                )
            }
        }

        fun stop(context: Context) {
            try {
                val appContext = context.applicationContext
                appContext.stopService(Intent(appContext, SigningForegroundService::class.java))
            } catch (e: Exception) {
                errorLog(
                    SigningForegroundService::class.java.simpleName,
                    "Unable to stop signing foreground service",
                    e,
                )
            }
        }
    }
}
