package com.vtopu.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vtopu.app.data.AppSettings

class VtopKeepAliveReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        if (AppSettings(context.applicationContext).backgroundKeepAliveEnabled) {
            VtopKeepAliveScheduler.schedule(context.applicationContext)
            runCatching {
                VtopKeepAliveService.start(context.applicationContext)
            }
        }
    }
}
