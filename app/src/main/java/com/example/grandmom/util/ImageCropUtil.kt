package com.example.grandmom.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 處理圖片裁剪的工具類。
 * 使用更現代的方式來處理裁剪，避免在Android 10及以上的Scoped Storage問題。
 */
object ImageCropUtil {
    private const val TAG = "ImageCropUtil"

    /**
     * 創建裁剪圖片的Intent
     * @param context 上下文
     * @param sourceUri 原始圖片的Uri
     * @param aspectX X軸比例
     * @param aspectY Y軸比例
     * @param outputX 輸出寬度
     * @param outputY 輸出高度
     * @return 裁剪Intent和目標Uri的配對
     */
    fun createCropIntent(
        context: Context,
        sourceUri: Uri,
        aspectX: Int = 1,
        aspectY: Int = 1,
        outputX: Int = 1200,
        outputY: Int = 1200
    ): Pair<Intent, Uri>? {
        // 創建目標Uri
        val destinationUri = createTempImageUri(context) ?: return null

        // 設置裁剪Intent
        val cropIntent = Intent("com.android.camera.action.CROP").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            
            setDataAndType(sourceUri, "image/*")
            putExtra("crop", "true")
            putExtra("aspectX", aspectX)
            putExtra("aspectY", aspectY)
            putExtra("outputX", outputX)
            putExtra("outputY", outputY)
            putExtra("scale", true)
            putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            putExtra("return-data", false)
            putExtra("noFaceDetection", true)
            putExtra(android.provider.MediaStore.EXTRA_OUTPUT, destinationUri)
        }
        
        // 特別授予權限給源URI和目標URI
        try {
            val resInfoList = context.packageManager
                .queryIntentActivities(cropIntent, 0)
                
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    sourceUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                context.grantUriPermission(
                    packageName,
                    destinationUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "授予權限時出錯: ${e.message}")
            return null
        }
        
        return Pair(cropIntent, destinationUri)
    }
    
    /**
     * 創建一個臨時文件用於存儲裁剪後的圖片
     * @param context 上下文
     * @return 臨時文件的Uri
     */
    fun createTempImageUri(context: Context): Uri? {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir("cropped_images")
            if (!storageDir?.exists()!!) {
                storageDir.mkdirs()
            }
            val tempFile = File(storageDir, "CROP_${timeStamp}.jpg")
            
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
        } catch (e: IOException) {
            Log.e(TAG, "創建臨時文件失敗: ${e.message}")
            return null
        }
    }
    
    /**
     * 清理所有裁剪臨時文件
     * @param context 上下文
     */
    fun cleanupTempFiles(context: Context) {
        try {
            val storageDir = context.getExternalFilesDir("cropped_images")
            storageDir?.listFiles()?.forEach { file ->
                file.delete()
            }
            Log.d(TAG, "臨時文件已清理")
        } catch (e: Exception) {
            Log.e(TAG, "清理臨時文件失敗: ${e.message}")
        }
    }
}