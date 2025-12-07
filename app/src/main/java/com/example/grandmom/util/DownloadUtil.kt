package com.example.grandmom.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * 負責從網路下載媒體檔案並儲存到本地的工具類
 */
object DownloadUtil {
    private const val TAG = "DownloadUtil"
    private const val CONNECT_TIMEOUT = 15000
    private const val READ_TIMEOUT = 30000
    
    /**
     * 下載圖片到內部儲存空間
     * 
     * @param context 上下文
     * @param imageUrl 圖片的URL
     * @return 保存的本地檔案路徑，如果下載失敗則返回null
     */
    suspend fun downloadImageToInternalStorage(context: Context, imageUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (imageUrl.isEmpty()) {
                    Log.w(TAG, "Empty image URL provided, skipping download")
                    return@withContext null
                }
                
                val fileName = "img_${UUID.randomUUID()}.jpg"
                val file = File(context.filesDir, fileName)
                
                val savedPath = downloadFile(imageUrl, file)
                Log.d(TAG, "Image downloaded to $savedPath")
                savedPath
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download image: ${e.message}")
                null
            }
        }
    }
    
    /**
     * 下載音訊到內部儲存空間
     * 
     * @param context 上下文
     * @param audioUrl 音訊的URL
     * @return 保存的本地檔案路徑，如果下載失敗則返回null
     */
    suspend fun downloadAudioToInternalStorage(context: Context, audioUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                if (audioUrl.isEmpty()) {
                    Log.w(TAG, "Empty audio URL provided, skipping download")
                    return@withContext null
                }
                
                val fileName = "audio_${UUID.randomUUID()}.mp3"
                val file = File(context.filesDir, fileName)
                
                val savedPath = downloadFile(audioUrl, file)
                Log.d(TAG, "Audio downloaded to $savedPath")
                savedPath
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download audio: ${e.message}")
                null
            }
        }
    }
    
    /**
     * 下載檔案並保存到指定位置
     * 
     * @param fileUrl 要下載的檔案URL
     * @param destinationFile 目標檔案
     * @return 保存的檔案路徑，如果下載失敗則返回null
     */
    private suspend fun downloadFile(fileUrl: String, destinationFile: File): String? {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                Log.d(TAG, "開始下載檔案: $fileUrl")
                val url = URL(fileUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                    requestMethod = "GET"
                }
                
                Log.d(TAG, "已建立 HTTP 連接，等待回應...")
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "伺服器回應代碼錯誤: $responseCode, URL: $fileUrl")
                    return@withContext null
                }
                
                val contentLength = connection.contentLength
                Log.d(TAG, "檔案大小: ${contentLength / 1024} KB")
                
                connection.inputStream.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        Log.d(TAG, "開始寫入到檔案: ${destinationFile.absolutePath}")
                        val buffer = ByteArray(4 * 1024) // 4K buffer
                        var bytesRead: Int
                        var totalRead = 0L
                        
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                        }
                        output.flush()
                        
                        Log.d(TAG, "檔案寫入完成，總共寫入: ${totalRead / 1024} KB")
                    }
                }
                
                // 驗證檔案是否存在
                if (destinationFile.exists() && destinationFile.length() > 0) {
                    Log.d(TAG, "檔案下載成功: ${destinationFile.absolutePath}, 大小: ${destinationFile.length() / 1024} KB")
                    destinationFile.absolutePath
                } else {
                    Log.e(TAG, "檔案下載失敗，沒有寫入資料或檔案不存在: ${destinationFile.absolutePath}")
                    null
                }
            } catch (e: IOException) {
                Log.e(TAG, "下載檔案錯誤: ${e.message}, URL: $fileUrl", e)
                e.printStackTrace()
                null
            } finally {
                connection?.disconnect()
            }
        }
    }
}