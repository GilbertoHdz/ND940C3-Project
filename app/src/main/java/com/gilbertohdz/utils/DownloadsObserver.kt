package com.gilbertohdz.utils

import android.os.FileObserver
import android.util.Log

class DownloadsObserver(path: String) : FileObserver(path, flags) {

    override fun onEvent(event: Int, path: String?) {
        Log.d(LOG_TAG, "onEvent($event, $path)")
        if (path == null) {
            return
        }
        when (event) {
            CLOSE_WRITE -> {
                // Download complete, or paused when wifi is disconnected. Possibly reported more than once in a row.
                // Useful for noticing when a download has been paused. For completions, register a receiver for
                // DownloadManager.ACTION_DOWNLOAD_COMPLETE.
            }
            OPEN -> {
                // Called for both read and write modes.
                // Useful for noticing a download has been started or resumed.
            }
            DELETE, MOVED_FROM -> {
                // These might come in handy for obvious reasons.
            }
            MODIFY -> {
                // Called very frequently while a download is ongoing (~1 per ms).
                // This could be used to trigger a progress update, but that should probably be done less often than this.
            }
        }
    }

    companion object {
        private val LOG_TAG = DownloadsObserver::class.java.simpleName

        private const val flags = (CLOSE_WRITE
                or OPEN
                or MODIFY
                or DELETE
                or MOVED_FROM)
    }
}