package com.gilbertohdz.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.gilbertohdz.DetailActivity
import com.gilbertohdz.R

private const val CHANNEL_ID_DOWNLOADS = "notification_downloads"
private const val CHANNEL_NAME_DOWNLOADS = "Downloads"
private const val REQUEST_CODE_DOWNLOADS = 937

enum class DownloadStatus {
    SUCCESS, FAIL
}

object NotificationUtils {

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(context: Context, ChannelInfo: ChannelInfo) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationChannel(
            ChannelInfo.id,
            ChannelInfo.name,
            ChannelInfo.importance
        ).apply {
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
            lightColor = context.getColor(R.color.colorAccent)
            description = ChannelInfo.description
            lockscreenVisibility = ChannelInfo.visibility
            notificationManager.createNotificationChannel(this)
        }
    }

    fun sendNotification(
        applicationContext: Context,
        downloadId: Int,
        fileName: String,
        status: DownloadStatus
    ) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification Intent
        val notifyIntent = Intent(applicationContext, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtras(DetailActivity.withExtras(downloadId, status, fileName))
        }

        // Common values
        val downloadsChannel = channelInfo(applicationContext)

        // Pending Intent
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE_DOWNLOADS,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Message body result
        val messageBody = when (status) {
            DownloadStatus.SUCCESS -> R.string.download_success.toStringResources(applicationContext)
            DownloadStatus.FAIL -> R.string.download_fail.toStringResources(applicationContext)
        }

        // Action to open intent
        val actionToIntent = NotificationCompat.Action.Builder(
            0,
            R.string.view_details.toStringResources(applicationContext),
            contentPendingIntent
        ).build()

        val notificationBuilder =
            NotificationCompat.Builder(applicationContext, downloadsChannel.id)
                .setSmallIcon(R.drawable.ic_assistant_black_24dp)
                .setContentTitle(fileName)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setColor(applicationContext.getColor(R.color.colorAccent))
                .setLights(applicationContext.getColor(R.color.colorAccent), 1000, 3000)
                .setVisibility(downloadsChannel.visibility)
                .setPriority(downloadsChannel.priority)
                .addAction(actionToIntent)

        notificationManager.notify(downloadId, notificationBuilder.build())
    }

    fun channelInfo(context: Context): ChannelInfo {
        return ChannelInfo(
            CHANNEL_ID_DOWNLOADS,
            CHANNEL_NAME_DOWNLOADS,
            context.getString(R.string.download_files),
            NotificationManager.IMPORTANCE_HIGH,
            NotificationCompat.PRIORITY_HIGH,
            NotificationCompat.VISIBILITY_PUBLIC
        )
    }

    private fun Int.toStringResources(context: Context): CharSequence {
        return context.getString(this)
    }
}