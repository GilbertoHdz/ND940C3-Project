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
            when (contentMainRadioGroup.checkedRadioButtonId) {
                R.id.rbGlide -> {
                    download(URL_GLIDE)
                }
                R.id.rbLoadApp -> {
                    download(URL)
                }
                R.id.rbRetrofit -> {
                    download(URL_RETROFIT)
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
                        }
                        DownloadManager.STATUS_FAILED -> {
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
        var progress: Int = 0
        while (!finishDownload) {
            val cursor: Cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
            if (cursor.moveToFirst()) {
                val status: Int =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        val total: Long =
                            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (total >= 0) {
                            val downloaded: Long =
                                cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            progress = (downloaded * 100L / total).toInt()
                            // if you use downloadmanger in async task, here you can use like this to display progress.
                            // Don't forget to do the division in long to get more digits rather than double.
                            //  publishProgress((int) ((downloaded * 100L) / total));
                        }
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        progress = 100
                        // if you use aysnc task
                        // publishProgress(100);
                        finishDownload = true
                    }
                }

                Log.i("MANAGER", "progress: $progress")
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
        percentDownload(downloadManager)
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL_GLIDE = "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL_RETROFIT = "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

}
