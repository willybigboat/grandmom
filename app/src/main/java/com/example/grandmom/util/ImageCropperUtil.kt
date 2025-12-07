package com.example.grandmom.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.yalantis.ucrop.UCrop
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
 * 使用UCrop庫實現的圖片裁剪工具
 */
object ImageCropperUtil {
    private const val TAG = "ImageCropperUtil"
    private const val TEMP_CROP_PREFIX = "temp_crop_"
    private const val DEST_CROP_PREFIX = "crop_"

    /**
     * 啟動UCrop裁剪
     * 
     * @param context 上下文
     * @param sourceUri 要裁剪的圖片Uri
     * @param cropLauncher 啟動UCrop活動的Launcher
     * @return 裁剪後圖片的目標Uri (如果出錯則返回null)
     */
    suspend fun startCrop(
        context: Context,
        sourceUri: Uri,
        cropLauncher: ActivityResultLauncher<Intent>
    ): Uri? {
        try {
            // 創建輸入和輸出文件
            val (inputFile, outputFile) = createCropFiles(context)
            
            // 構建UCrop選項
            val options = UCrop.Options().apply {
                setCompressionQuality(100) // 使用最高質量，我們會在後續處理中自行壓縮
                setToolbarTitle("裁剪圖片")
                setStatusBarColor(context.getColor(androidx.appcompat.R.color.primary_dark_material_dark))
                setToolbarColor(context.getColor(androidx.appcompat.R.color.primary_material_dark))
                setActiveControlsWidgetColor(context.getColor(androidx.appcompat.R.color.accent_material_dark))
                setFreeStyleCropEnabled(false) // 將此設置為true以允許自由形狀裁剪
                setHideBottomControls(false)
            }
            
            // 從源URI複製到臨時輸入文件
            val inputUri = copyUriToFile(context, sourceUri, inputFile)
            if (inputUri == null) {
                Log.e(TAG, "複製源圖片到臨時文件失敗")
                return null
            }
            
            // 創建輸出Uri
            val outputUri = Uri.fromFile(outputFile)
            
            // 啟動UCrop
            val uCropIntent = UCrop.of(inputUri, outputUri)
                .withOptions(options)
                .withAspectRatio(1f, 1f) // 1:1比例，可以更改
                .withMaxResultSize(2400, 2400) // 較大的輸出大小，後續會再調整
                .getIntent(context)
                
            cropLauncher.launch(uCropIntent)
            
            return outputUri
        } catch (e: Exception) {
            Log.e(TAG, "啟動裁剪時出錯: ${e.message}")
            return null
        }
    }
    
    /**
     * 創建裁剪所需的臨時文件
     */
    private fun createCropFiles(context: Context): Pair<File, File> {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val random = UUID.randomUUID().toString().substring(0, 8)
        
        // 創建臨時目錄
        val tempDir = File(context.filesDir, "image_crops")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        
        // 輸入和輸出文件
        val inputFile = File(tempDir, "${TEMP_CROP_PREFIX}${timestamp}_${random}.jpg")
        val outputFile = File(tempDir, "${DEST_CROP_PREFIX}${timestamp}_${random}.jpg")
        
        return Pair(inputFile, outputFile)
    }
    
    /**
     * 將URI指向的內容複製到文件中
     */
    private suspend fun copyUriToFile(context: Context, uri: Uri, destFile: File): Uri? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            return@withContext Uri.fromFile(destFile)
        } catch (e: IOException) {
            Log.e(TAG, "複製URI到文件失敗: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * 清理臨時文件
     */
    fun cleanupTempFiles(context: Context) {
        try {
            val tempDir = File(context.filesDir, "image_crops")
            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith(TEMP_CROP_PREFIX)) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "清理臨時文件失敗: ${e.message}")
        }
    }
    
    /**
     * 從Uri獲取實際路徑，用於數據庫存儲
     */
    fun getActualPath(uri: Uri): String {
        return uri.toString()
    }
}