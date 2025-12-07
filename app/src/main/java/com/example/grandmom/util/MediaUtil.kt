package com.example.grandmom.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 處理音頻播放的工具類。
 */
object AudioPlayerUtil {
    private var mediaPlayer: MediaPlayer? = null
    
    /**
     * 播放音頻文件。
     * @param context 上下文
     * @param audioPath 音頻文件的路徑
     */
    fun playAudio(context: Context, audioPath: String) {
        try {
            // 釋放先前的MediaPlayer實例（如果存在）
            mediaPlayer?.release()
            
            // 創建新的MediaPlayer實例
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(audioPath))
                prepare() // 同步準備，因為文件已在本地
                start()
                
                // 播放完成時釋放資源
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            }
        } catch (e: IOException) {
            Log.e("AudioPlayerUtil", "Error playing audio: ${e.message}")
            e.printStackTrace()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    
    /**
     * 停止當前正在播放的音頻。
     */
    fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }
}

/**
 * 處理文件存儲的工具類。
 */
object FileUtil {
    /**
     * 保存圖片到內部存儲。
     * @param context 上下文
     * @param uri 圖片的Uri
     * @return 保存的文件路徑
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            
            inputStream?.close()
            file.absolutePath
        } catch (e: IOException) {
            Log.e("FileUtil", "Error saving image: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 保存音頻到內部存儲。
     * @param context 上下文
     * @param uri 音頻的Uri
     * @return 保存的文件路徑
     */
    fun saveAudioToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "audio_${System.currentTimeMillis()}.mp3"
            val file = File(context.filesDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            
            inputStream?.close()
            file.absolutePath
        } catch (e: IOException) {
            Log.e("FileUtil", "Error saving audio: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 刪除文件。
     * @param filePath 要刪除的文件路徑
     * @return 是否成功刪除
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FileUtil", "Error deleting file: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}