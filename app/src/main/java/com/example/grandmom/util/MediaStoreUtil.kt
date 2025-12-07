package com.example.grandmom.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 處理媒體檔案的工具類，提供檔案保存、獲取和復製的功能。
 */
object MediaStoreUtil {
    private const val TAG = "MediaStoreUtil"
    
    /**
     * 從URI複製內容到應用內部儲存空間
     */
    suspend fun copyUriToInternalStorage(
        context: Context,
        sourceUri: Uri,
        destinationFileName: String,
        subFolder: String
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // 建立目標目錄
            val destinationDir = File(context.filesDir, subFolder)
            if (!destinationDir.exists()) {
                destinationDir.mkdirs()
            }
            
            // 建立目標檔案
            val destinationFile = File(destinationDir, destinationFileName)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            
            // 開始複製操作
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            
            return@withContext Uri.fromFile(destinationFile)
        } catch (e: IOException) {
            Log.e(TAG, "複製檔案失敗: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * 為裁剪操作建立一個臨時檔案
     */
    fun createTempFileForCrop(context: Context): Pair<Uri, File> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "CROP_${timeStamp}_${UUID.randomUUID()}.jpg"
        
        val storageDir = File(context.filesDir, "temp_crops")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        val tempFile = File(storageDir, fileName)
        val fileUri = Uri.fromFile(tempFile)
        
        return Pair(fileUri, tempFile)
    }
    
    /**
     * 清理暫存資料夾中的檔案
     */
    fun cleanupTempFiles(context: Context) {
        try {
            val tempDir = File(context.filesDir, "temp_crops")
            if (tempDir.exists()) {
                val files = tempDir.listFiles()
                files?.forEach { file ->
                    // 刪除超過24小時的暫存檔案
                    val lastModified = file.lastModified()
                    val dayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                    if (lastModified < dayAgo) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "清理暫存檔案失敗: ${e.message}")
        }
    }
}