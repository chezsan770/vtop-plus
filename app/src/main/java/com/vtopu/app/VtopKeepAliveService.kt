package com.vtopu.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.vtopu.app.data.AppSettings
import com.vtopu.app.data.SessionKeepAliveResult
import com.vtopu.app.data.VtopRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VtopKeepAliveService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var keepAliveJob: Job? = null
    private lateinit var repository: VtopRepository

    override fun onCreate() {
        super.onCreate()
        repository = VtopRepository(applicationContext)
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            AppSettings(applicationContext).backgroundKeepAliveEnabled = false
            VtopKeepAliveScheduler.cancel(applicationContext)
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            startForeground(notificationId, buildNotification("Keeping VTOP session active"))
        } catch (_: RuntimeException) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (keepAliveJob?.isActive != true) {
            VtopKeepAliveScheduler.schedule(applicationContext)
            keepAliveJob = scope.launch {
                runKeepAliveLoop()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        keepAliveJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (AppSettings(applicationContext).backgroundKeepAliveEnabled) {
            VtopKeepAliveScheduler.schedule(applicationContext)
            runCatching { start(applicationContext) }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun runKeepAliveLoop() {
        while (true) {
            when (repository.keepSessionAlive()) {
                SessionKeepAliveResult.Active -> Unit
                SessionKeepAliveResult.Failed -> Unit
                SessionKeepAliveResult.Expired -> {
                    AppSettings(applicationContext).backgroundKeepAliveEnabled = false
                    VtopKeepAliveScheduler.cancel(applicationContext)
                    stopSelf()
                    return
                }
            }
            delay(keepAliveIntervalMillis)
        }
    }

    private fun buildNotification(text: String): Notification {
        val launchIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, VtopKeepAliveService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_gamma_notification)
            .setContentTitle("VTOP session active")
            .setContentText(text)
            .setContentIntent(launchIntent)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(R.drawable.ic_gamma_notification, "Stop", stopIntent)
            .build()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            channelId,
            "VTOP Keep Alive",
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Keeps your VTOP session active in the background"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val ACTION_STOP = "com.vtopu.app.STOP_KEEP_ALIVE"
        private const val channelId = "vtop_keep_alive_minimized"
        private const val notificationId = 77
        private const val keepAliveIntervalMillis = 12L * 60L * 1000L

        fun start(context: Context) {
            val intent = Intent(context, VtopKeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            VtopKeepAliveScheduler.cancel(context.applicationContext)
            context.stopService(Intent(context, VtopKeepAliveService::class.java))
        }
    }
}
