package com.jinvita.testdownload

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jinvita.testdownload.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val folder = "교육영상"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.button.setOnClickListener {
            val fileUrl = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_10mb.mp4"
            val fileName = "video1.mp4"
            binding.resultTextView.text = "$fileName 파일 다운로드 시작"
            downloadFile(fileUrl, fileName, folder)
        }

        binding.button2.setOnClickListener {
            binding.resultTextView.text = "비디오 재생 중"
            playVideo()
        }
    }

    // 외부 저장소의 [다운로드] 폴더 내 [abc] 폴더에 파일 저장
    private fun downloadFile(fileUrl: String, fileName: String, directory: String) {
        // [다운로드] 폴더
        val externalStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // [directory] 폴더를 만들기 위한 File 객체 생성
        val newFolder = File(externalStorageDirectory, directory)

        // [abc] 폴더가 없다면 생성
        if (!newFolder.exists()) newFolder.mkdirs()

        // 다운로드 받을 파일의 이름 및 경로 설정
        val targetFile = File(newFolder, fileName)

        // 파일 다운로드는 main thread 에서 작동할 수 없음.
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection()
                val totalSize = connection.contentLength.toLong()

                if (targetFile.exists()) {
                    AppData.debug(TAG, "이미 $fileName 파일이 존재함")
                    binding.resultTextView.text = "이미 $fileName 파일이 존재함"
                    if (totalSize == targetFile.length()) {
                        AppData.debug(TAG, "이미 $fileName 파일이 다운받아져 있음")
                        binding.resultTextView.text = "이미 $fileName 파일이 다운받아져 있음"
                        lifecycleScope.launch {
                            AppData.showToast(this@MainActivity, "이미 $fileName 파일이 다운받아져 있음")
                        }
                        return@launch
                    } else {
                        AppData.debug(TAG, "미완료 $fileName 파일이 있어 삭제 후 다운로드 중...")
                        binding.resultTextView.text = "미완료 $fileName 파일이 있어 삭제 후 다운로드 중..."
                    }
                } else {
                    AppData.debug(TAG, "$fileName 파일 생성 후 다운로드 시작")
                    binding.resultTextView.text = "$fileName 파일 생성 후 다운로드 중..."
                }
                lifecycleScope.launch {
                    AppData.showToast(this@MainActivity, "$fileName 파일 다운로드 중...")
                }
                connection.connect()

                // 파일 다운로드
                val input: InputStream = connection.getInputStream()
                val output: OutputStream = FileOutputStream(targetFile)
                val buffer = ByteArray(1024)
                var bytesRead: Int

                var totalBytesRead: Long = 0
                var lastProgress = 0

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)

                    totalBytesRead += bytesRead

                    // 진행률 계산
                    val progress = ((totalBytesRead * 100) / totalSize).toInt()

                    // 진행률이 변경될 때마다 로그로 출력
                    if (progress != lastProgress) {
                        lastProgress = progress
                        AppData.error(TAG, "다운로드 진행률: $progress%")
                        binding.progressTextView.text = "다운로드 진행률: $progress%"

                        // 다운로드 완료 시
                        if (progress == 100) {
                            binding.progressTextView.text = ""
                            binding.resultTextView.text = "$fileName 파일 다운로드 완료"
                            lifecycleScope.launch {
                                AppData.showToast(this@MainActivity, "$fileName 파일 다운로드 완료")
                            }
                        }
                    }
                }

                output.flush()
                output.close()
                input.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playVideo() = with(binding.videoView) {
        val videoPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/$folder/video1.mp4"
        setVideoPath(videoPath)
        start()
    }
}