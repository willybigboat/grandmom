package com.example.grandmom.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
 * 處理圖片操作的工具類。
 */
object ImageUtil {
    private const val TAG = "ImageUtil"
    
    /**
     * 從Uri讀取圖片並保存到應用內部存儲
     * 
     * @param context 上下文
     * @param imageUri 圖片Uri
     * @return 保存後的Uri或null（如果失敗）
     */
    suspend fun saveImageFromUriToInternalStorage(context: Context, imageUri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            // 讀取原始圖片
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap == null) {
                Log.e(TAG, "無法從URI解碼圖片: $imageUri")
                return@withContext null
            }
            
            // 創建內部存儲文件
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "IMG_$timeStamp.jpg"
            val storageDir = File(context.filesDir, "images")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val imageFile = File(storageDir, fileName)
            
            // 保存圖片
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            outputStream.flush()
            outputStream.close()
            
            return@withContext Uri.fromFile(imageFile)
        } catch (e: IOException) {
            Log.e(TAG, "保存圖片失敗: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * 創建用於裁剪操作的臨時文件
     * 
     * @param context 上下文
     * @return 臨時文件的Uri
     */
    fun createTempImageFileUri(context: Context): Uri? {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir("temp_images")
            if (!storageDir?.exists()!!) {
                storageDir.mkdirs()
            }
            val tempFile = File(storageDir, "TEMP_$timeStamp.jpg")
            
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
        } catch (e: Exception) {
            Log.e(TAG, "創建臨時文件失敗: ${e.message}")
            return null
        }
    }
}
