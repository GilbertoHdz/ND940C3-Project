package com.gilbertohdz

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.gilbertohdz.utils.DownloadStatus
import com.gilbertohdz.utils.DownloadsObserver
import com.gilbertohdz.utils.NotificationUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var filesDownloadedId = mutableSetOf<Long>()
    private lateinit var downloadManager: DownloadManager

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannel(
                this@MainActivity,
                NotificationUtils.channelInfo(this@MainActivity)
            )
        }

        radioSelectionListener()
    }

    private fun radioSelectionListener() {
        custom_button.setOnClickListener {
            if (custom_button.getState() != ButtonState.Completed) return@setOnClickListener

            when (contentMainRadioGroup.checkedRadioButtonId) {
                R.id.rbGlide -> {
                    download(URL_GLIDE)
                    custom_button.progress(0, ButtonState.Loading)
                }
                R.id.rbLoadApp -> {
                    download(URL)
                    custom_button.progress(0, ButtonState.Loading)
                }
                R.id.rbRetrofit -> {
                    download(URL_RETROFIT)
                    custom_button.progress(0, ButtonState.Loading)
                }
                else -> {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.no_selection),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (filesDownloadedId.remove(id)) {

                val extras = intent!!.extras
                val query = DownloadManager
                    .Query()
                    .setFilterById(extras!!.getLong(DownloadManager.EXTRA_DOWNLOAD_ID))

                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            NotificationUtils.sendNotification(
                                applicationContext = this@MainActivity,
                                downloadId = 4,
                                status = DownloadStatus.SUCCESS,
                                fileName = fileName
                            )
                            custom_button.progress(100, ButtonState.Completed)
                        }
                        DownloadManager.STATUS_FAILED -> {
                            custom_button.progress(100, ButtonState.Completed)
                            NotificationUtils.sendNotification(
                                applicationContext = this@MainActivity,
                                downloadId = 4,
                                status = DownloadStatus.FAIL,
                                fileName = fileName
                            )
                        }
                    }
                }
            }
        }
    }

    private fun percentDownload(downloadManager: DownloadManager) {
        var finishDownload = false
        while (!finishDownload) {
            val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))

            if (cursor.moveToFirst()) {
                val status: Int =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                        custom_button.progress(100, ButtonState.Completed)
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        custom_button.progress(0, ButtonState.Loading)
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        finishDownload = true
                        custom_button.progress(100, ButtonState.Completed)
                    }
                }
            }
        }
    }

    private fun download(urlStr: String) {
        val request =
            DownloadManager.Request(Uri.parse(urlStr))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        filesDownloadedId.add(downloadID)
        //percentDownload(downloadManager)
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_GLIDE = "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_RETROFIT = "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

}
