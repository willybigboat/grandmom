package com.example.grandmom.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.grandmom.data.model.MediaItem
import java.io.File
import java.io.IOException

/**
 * 用於播放音訊的工具類
 */
object AudioPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null

    /**
     * 播放媒體項目的音訊
     * 
     * @param context 上下文
     * @param mediaItem 媒體項目，包含音訊URL和本地路徑
     */
    fun playAudio(context: Context, mediaItem: MediaItem) {
        try {
            // 釋放現有播放器
            releaseMediaPlayer()
            
            // 決定使用本地路徑還是遠端URL
            val audioUri = when {
                mediaItem.localAudioPath.isNotEmpty() -> {
                    val file = File(mediaItem.localAudioPath)
                    if (file.exists() && file.length() > 0) {
                        Log.d("AudioPlayerHelper", "使用本地音訊: ${mediaItem.localAudioPath}, 檔案大小: ${file.length() / 1024}KB")
                        Uri.fromFile(file)
                    } else if (mediaItem.audioUrl.isNotEmpty()) {
                        Log.d("AudioPlayerHelper", "本地檔案不存在或為空，使用遠端音訊URL: ${mediaItem.audioUrl}")
                        Uri.parse(mediaItem.audioUrl)
                    } else {
                        Log.e("AudioPlayerHelper", "本地檔案不存在且沒有遠端URL")
                        return
                    }
                }
                mediaItem.audioUrl.isNotEmpty() -> {
                    Log.d("AudioPlayerHelper", "使用遠端音訊URL: ${mediaItem.audioUrl}")
                    Uri.parse(mediaItem.audioUrl)
                }
                else -> {
                    Log.e("AudioPlayerHelper", "沒有可用的音訊來源")
                    return
                }
            }
            
            // 創建並配置MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, audioUri)
                prepare() // 同步準備
                start()
                
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerHelper", "播放音訊時發生錯誤: ${e.message}", e)
            releaseMediaPlayer()
        }
    }

    /**
     * 釋放MediaPlayer資源
     */
    private fun releaseMediaPlayer() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }
    
    /**
     * 停止正在播放的音訊
     */
    fun stopAudio() {
        releaseMediaPlayer()
    }
}