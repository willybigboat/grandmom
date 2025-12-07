package com.example.grandmom.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 處理圖片裁剪功能的工具類
 */
class PhotoCropUtil {
    companion object {
        private const val TAG = "PhotoCropUtil"

        /**
         * 啟動圖片裁剪活動
         *
         * @param activity 活動上下文
         * @param sourceUri 要裁剪的圖片Uri
         * @param callback 裁剪完成後的回調
         */
        suspend fun startCrop(
            context: Context,
            sourceUri: Uri,
            onComplete: (Uri?) -> Unit
        ) {
            try {
                // 1. 從來源Uri複製圖片到內部儲存
                val inputUri = copyToInternal(context, sourceUri)
                
                // 2. 建立目標文件
                val (outputUri, outputFile) = MediaStoreUtil.createTempFileForCrop(context)
                
                // 3. 建立裁剪意圖
                val cropIntent = Intent("com.android.camera.action.CROP").apply {
                    setDataAndType(inputUri, "image/*")
                    putExtra("crop", "true")
                    putExtra("aspectX", 1)
                    putExtra("aspectY", 1)
                    putExtra("outputX", 1200)
                    putExtra("outputY", 1200)
                    putExtra("scale", true)
                    putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
                    putExtra("return-data", false)
                    putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
                }
                
                // 4. 傳回結果
                onComplete(outputUri)
                
            } catch (e: Exception) {
                Log.e(TAG, "裁剪圖片失敗: ${e.message}")
                onComplete(null)
            }
        }
        
        /**
         * 將圖片複製到內部儲存空間
         */
        private suspend fun copyToInternal(context: Context, sourceUri: Uri): Uri = withContext(Dispatchers.IO) {
            try {
                // 建立臨時檔案
                val tempDir = File(context.filesDir, "temp_inputs")
                if (!tempDir.exists()) {
                    tempDir.mkdirs()
                }
                
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val tempFile = File(tempDir, "INPUT_$timeStamp.jpg")
                
                // 複製內容
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                        }
                        output.flush()
                    }
                }
                
                return@withContext Uri.fromFile(tempFile)
            } catch (e: IOException) {
                Log.e(TAG, "複製檔案失敗: ${e.message}")
                throw e
            }
        }
    }
}