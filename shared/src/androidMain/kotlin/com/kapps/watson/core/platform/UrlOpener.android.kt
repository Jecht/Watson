package com.kapps.watson.core.platform

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.koin.mp.KoinPlatform.getKoin

actual fun openUrlInBrowser(url: String) {
    runCatching {
        val context = getKoin().get<Context>()
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}