package com.jinvita.testdownload

import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.jinvita.testdownload.databinding.ActivityMainBinding
import java.io.File
import java.net.URI
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var downloadId = 0L

    private val controller by lazy {
        object : MediaController(this) {
            override fun show() = super.show(0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        with(binding) {
            button.setOnClickListener {
                binding.resultTextView.text = "비디오 다운로드 중..."
//                downloadFile("http://211.45.4.23:40009/public/app/BaroWiFi.apk")
                downloadFile("https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4")
            }
            button2.setOnClickListener { playVideo() }
        }
    }

    private fun downloadFile(fileUrl: String) {
        val downloadRequest = DownloadManager.Request(Uri.parse(fileUrl))
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "video1.mp4")
            .setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("최신 교육 영상")
            .setDescription("downloading...")

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)

        // 같은 이름이 있으면 삭제 후 설치
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            do {
                val localUri = cursor.getString(abs(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
                if (localUri != null && localUri.endsWith("/video1.mp4")) {
                    val file = File(URI(localUri))
                    if (file.exists()) {
                        file.delete()
                    }
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        downloadId = downloadManager.enqueue(downloadRequest)

        // 다운로드 완료를 처리하기 위한 BroadcastReceiver 등록
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    // 다운로드가 완료되었을 때 처리할 코드를 여기에 추가
                    // 예를 들어, 다운로드된 파일을 재생하거나 다른 작업을 수행할 수 있습니다.
                    AppData.showToast(this@MainActivity, "다운로드 완료!")
                    binding.resultTextView.text = "비디오 다운로드 완료"
                }
            }
        }
        registerReceiver(receiver, filter)
    }

    private fun ActivityMainBinding.playVideo() = with(videoView) {

        val videoPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/video1.mp4"
        setVideoPath(videoPath)
        setOnCompletionListener {
            controller.show(0)
            start()
        }
        setOnPreparedListener { controller.show(0) }
        start()

        resultTextView.text = "비디오 재생 중"
    }
}