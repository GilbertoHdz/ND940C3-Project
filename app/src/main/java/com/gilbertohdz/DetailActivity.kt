package com.gilbertohdz

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.gilbertohdz.utils.DownloadStatus
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        // Clean pending notification
        val notificationDownloadId = intent.extras!!.getInt(EXTRA_DOWNLOAD_ID)
        // NotificationUtils.clearNotification(this, notificationDownloadId)

        val fileName: String = intent.extras!!.getString(EXTRA_FILE_NAME)!!
        val downloadStatus: DownloadStatus = DownloadStatus.values()[intent.extras!!.getInt(EXTRA_DOWNLOAD_STATUS)]

        contentFileValue.text = fileName
        contentStatusValue.text = when (downloadStatus) {
            DownloadStatus.SUCCESS -> getString(R.string.download_success)
            DownloadStatus.FAIL -> getString(R.string.download_fail)
        }

        Handler().postDelayed({
            constraintMotionLayout.transitionToEnd()
        }, 1000)

        btn_back.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        constraintMotionLayout.transitionToStart()
    }

    companion object {
        private const val EXTRA_DOWNLOAD_ID = "download_id"
        private const val EXTRA_DOWNLOAD_STATUS = "download_status"
        private const val EXTRA_FILE_NAME = "file_name"

        fun withExtras(
                downloadId: Int,
                downloadStatus: DownloadStatus,
                fileName: String
        ): Bundle {
            return Bundle().apply {
                putInt(EXTRA_DOWNLOAD_ID, downloadId)
                putInt(EXTRA_DOWNLOAD_STATUS, downloadStatus.ordinal)
                putString(EXTRA_FILE_NAME, fileName)
            }
        }
    }
}
