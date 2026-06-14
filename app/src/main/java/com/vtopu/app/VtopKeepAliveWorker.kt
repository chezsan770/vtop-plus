package com.vtopu.app

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vtopu.app.data.AppSettings
import com.vtopu.app.data.SessionKeepAliveResult
import com.vtopu.app.data.VtopRepository
import java.util.concurrent.TimeUnit

class VtopKeepAliveWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val settings = AppSettings(applicationContext)
        if (!settings.backgroundKeepAliveEnabled) return Result.success()

        return when (VtopRepository(applicationContext).keepSessionAlive()) {
            SessionKeepAliveResult.Active -> Result.success()
            SessionKeepAliveResult.Failed -> Result.retry()
            SessionKeepAliveResult.Expired -> {
                settings.backgroundKeepAliveEnabled = false
                VtopKeepAliveScheduler.cancel(applicationContext)
                Result.success()
            }
        }
    }
}

object VtopKeepAliveScheduler {
    private const val workName = "vtop_keep_alive_ping"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<VtopKeepAliveWorker>(
            15L,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(workName)
    }
}
