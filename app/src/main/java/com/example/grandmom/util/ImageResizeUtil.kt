package com.example.grandmom.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 用於處理圖片尺寸調整的工具類
 */
object ImageResizeUtil {
    private const val TAG = "ImageResizeUtil"
    
    // 設定固定的目標尺寸
    private const val TARGET_WIDTH = 800
    private const val TARGET_HEIGHT = 800
    private const val QUALITY = 90
    
    /**
     * 調整圖片大小並儲存到指定的檔案
     * 
     * @param context 上下文
     * @param sourceUri 來源圖片URI
     * @param fileName 可選的檔案名稱，如果為空則自動生成
     * @return 調整大小後的圖片的URI
     */
    suspend fun resizeAndSaveImage(
        context: Context, 
        sourceUri: Uri,
        fileName: String? = null
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // 從URI讀取圖片
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) {
                Log.e(TAG, "無法從URI解析圖片: $sourceUri")
                return@withContext null
            }
            
            // 計算新尺寸，保持縱橫比
            val (newWidth, newHeight) = calculateTargetSize(
                originalBitmap.width, 
                originalBitmap.height
            )
            
            // 調整大小
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap, 
                newWidth, 
                newHeight, 
                true
            )
            
            // 創建輸出檔案
            val outputFile = createOutputFile(context, fileName)
            
            // 寫入調整大小後的圖片
            val outputStream = FileOutputStream(outputFile)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()
            
            // 釋放資源
            if (originalBitmap != resizedBitmap) {
                originalBitmap.recycle()
            }
            resizedBitmap.recycle()
            
            // 返回URI
            return@withContext Uri.fromFile(outputFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "調整圖片大小時出錯: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * 處理UCrop輸出的圖片，調整大小並儲存
     * 
     * @param context 上下文
     * @param sourceUri 裁剪後的圖片URI
     * @return 調整大小後的圖片URI
     */
    suspend fun processAndSaveCroppedImage(
        context: Context,
        sourceUri: Uri
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // 如果這是檔案URI，直接讀取
            val bitmap = if (sourceUri.scheme == "file") {
                BitmapFactory.decodeFile(sourceUri.path)
            } else {
                // 否則通過ContentResolver讀取
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                val result = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                result
            }
            
            if (bitmap == null) {
                Log.e(TAG, "無法從裁剪後的URI讀取圖片: $sourceUri")
                return@withContext null
            }
            
            // 圖片本身縱橫比應該已經符合要求，我們只需調整大小
            val (newWidth, newHeight) = calculateTargetSize(bitmap.width, bitmap.height)
            
            // 調整大小
            val resizedBitmap = if (bitmap.width != newWidth || bitmap.height != newHeight) {
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }
            
            // 創建永久儲存的文件
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_$timestamp.jpg"
            val storageDir = File(context.filesDir, "images")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val outputFile = File(storageDir, fileName)
            
            // 寫入調整大小後的圖片
            val outputStream = FileOutputStream(outputFile)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()
            
            // 釋放資源
            if (bitmap != resizedBitmap) {
                bitmap.recycle()
            }
            resizedBitmap.recycle()
            
            // 如果源文件是臨時文件，嘗試刪除它
            if (sourceUri.scheme == "file") {
                try {
                    val sourceFile = File(sourceUri.path!!)
                    if (sourceFile.exists() && sourceFile.absolutePath.contains("temp") || 
                        sourceFile.absolutePath.contains("crop")) {
                        sourceFile.delete()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "清理臨時文件失敗: ${e.message}")
                }
            }
            
            return@withContext Uri.fromFile(outputFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "處理裁剪圖片時出錯: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * 計算目標尺寸，保持縱橫比
     */
    private fun calculateTargetSize(originalWidth: Int, originalHeight: Int): Pair<Int, Int> {
        // 如果原始圖片已經小於目標尺寸，保持原樣
        if (originalWidth <= TARGET_WIDTH && originalHeight <= TARGET_HEIGHT) {
            return Pair(originalWidth, originalHeight)
        }
        
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        
        return if (aspectRatio > 1) {
            // 寬圖像
            val newHeight = (TARGET_WIDTH / aspectRatio).toInt()
            Pair(TARGET_WIDTH, newHeight)
        } else {
            // 高圖像
            val newWidth = (TARGET_HEIGHT * aspectRatio).toInt()
            Pair(newWidth, TARGET_HEIGHT)
        }
    }
    
    /**
     * 創建輸出文件
     */
    private fun createOutputFile(context: Context, fileName: String?): File {
        val storageDir = File(context.filesDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        val actualFileName = fileName ?: run {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            "IMG_$timestamp.jpg"
        }
        
        return File(storageDir, actualFileName)
    }
}