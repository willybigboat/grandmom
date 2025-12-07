package com.example.grandmom.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.grandmom.data.model.MediaItem
import com.example.grandmom.data.repository.MediaRepository
import com.example.grandmom.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 預設媒體資料的初始化工具類
 */
object DefaultMediaLoader {
    private const val TAG = "DefaultMediaLoader"

    // 預設媒體文件名稱列表（不含副檔名）
    private val DEFAULT_MEDIA_FILES = listOf(
        "ahui_gugu",
        "amei_gugu",
        "azhen_gugu",
        "azhu_gugu",
        "chingjui",
        "chunmay",
        "hawjay",
        "juiway",
        "paywen",
        "qmei",
        "renyi",
        "rouqi",
        "tsai_family"
    )

    /**
     * 從應用程式獲取 Repository 並加載預設媒體
     * 
     * @param context 上下文
     */
    suspend fun loadDefaultMedia(context: Context) {
        Log.d(TAG, "開始從 MainActivity 加載預設媒體")
        val database = AppDatabase.getDatabase(context)
        val repository = MediaRepository(database.mediaItemDao())
        loadDefaultMedia(context, repository)
        Log.d(TAG, "預設媒體載入完成")
    }

    /**
     * 檢查並加載預設媒體文件到資料庫
     *
     * @param context 上下文
     * @param repository 媒體資料庫操作接口
     */
    suspend fun loadDefaultMediaIfNeeded(context: Context, repository: MediaRepository) {
        // 檢查是否是首次啟動
        val prefs = context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(Constants.PREF_FIRST_LAUNCH, true)

        if (isFirstLaunch) {
            Log.d(TAG, "首次啟動，準備加載預設媒體...")
            
            // 加載預設媒體
            loadDefaultMedia(context, repository)
            
            // 標記已非首次啟動
            prefs.edit().putBoolean(Constants.PREF_FIRST_LAUNCH, false).apply()
            Log.d(TAG, "預設媒體加載完成")
        } else {
            Log.d(TAG, "非首次啟動，跳過加載預設媒體")
        }
    }

    /**
     * 加載預設媒體文件到資料庫
     */
    internal suspend fun loadDefaultMedia(context: Context, repository: MediaRepository) = withContext(Dispatchers.IO) {
        Log.d(TAG, "開始加載 ${DEFAULT_MEDIA_FILES.size} 個預設媒體項目")
        
        for (fileName in DEFAULT_MEDIA_FILES) {
            try {
                // 獲取圖片資源ID
                val imageResId = context.resources.getIdentifier(fileName, "drawable", context.packageName)
                if (imageResId == 0) {
                    Log.e(TAG, "找不到圖片資源: $fileName")
                    continue
                }

                // 獲取音頻資源ID
                val audioResId = context.resources.getIdentifier(fileName, "raw", context.packageName)
                if (audioResId == 0) {
                    Log.e(TAG, "找不到音頻資源: $fileName")
                    continue
                }

                // 將資源文件複製到應用內部儲存空間
                val imageUri = copyDrawableToInternalStorage(context, imageResId, "$fileName.jpg")
                val audioUri = copyRawToInternalStorage(context, audioResId, "$fileName.m4a")

                if (imageUri != null && audioUri != null) {
                    // 創建媒體項目
                    val mediaItem = MediaItem(
                        id = "",
                        title = fileName.replace("_", " ").capitalize(),
                        imageUrl = imageUri.toString(),
                        audioUrl = audioUri.toString(),
                        createDate = System.currentTimeMillis()
                    )
                    
                    // 插入到資料庫
                    repository.insertMediaItem(mediaItem)
                    Log.d(TAG, "已加載預設媒體: $fileName")
                } else {
                    Log.e(TAG, "無法複製媒體文件: $fileName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "加載預設媒體失敗 $fileName: ${e.message}")
            }
        }
    }

    /**
     * 將drawable資源複製到應用內部儲存空間
     */
    private suspend fun copyDrawableToInternalStorage(
        context: Context,
        resourceId: Int,
        fileName: String
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // 創建內部儲存目錄
            val storageDir = File(context.filesDir, "images")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            // 創建目標文件
            val outputFile = File(storageDir, fileName)
            
            // 複製資源到文件
            context.resources.openRawResource(resourceId).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            
            return@withContext Uri.fromFile(outputFile)
        } catch (e: IOException) {
            Log.e(TAG, "複製圖片資源失敗: ${e.message}")
            return@withContext null
        }
    }

    /**
     * 將raw資源複製到應用內部儲存空間
     */
    private suspend fun copyRawToInternalStorage(
        context: Context,
        resourceId: Int,
        fileName: String
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // 創建內部儲存目錄
            val storageDir = File(context.filesDir, "audios")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            // 創建目標文件
            val outputFile = File(storageDir, fileName)
            
            // 複製資源到文件
            context.resources.openRawResource(resourceId).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }
            
            return@withContext Uri.fromFile(outputFile)
        } catch (e: IOException) {
            Log.e(TAG, "複製音頻資源失敗: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * 將字符串首字母大寫
     */
    private fun String.capitalize(): String {
        return if (isNotEmpty()) {
            this[0].uppercase() + substring(1)
        } else {
            this
        }
    }
}