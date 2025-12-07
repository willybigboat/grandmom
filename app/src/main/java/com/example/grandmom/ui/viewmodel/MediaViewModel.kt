package com.example.grandmom.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.viewModelScope
import com.example.grandmom.GrandmomApp
import com.example.grandmom.data.model.MediaItem
import com.example.grandmom.data.repository.MediaRepository
import kotlinx.coroutines.launch

/**
 * ViewModel用於管理媒體項目的UI相關數據。
 */
class MediaViewModel(application: Application) : AndroidViewModel(application) {
    
    // 取得儲存庫
    private val repository: MediaRepository = (application as GrandmomApp).repository
    
    // 所有媒體項目的LiveData
    val allMediaItems: Flow<List<MediaItem>> = repository.allMediaItems
    
    /**
     * 插入新的媒體項目。
     */
    fun insertMediaItem(mediaItem: MediaItem) = viewModelScope.launch {
        android.util.Log.d("MediaViewModel", "插入/更新媒體項目: ID=${mediaItem.id}, 標題=${mediaItem.title}")
        repository.insertMediaItem(mediaItem)
        // 插入後檢查總數
        val count = repository.getMediaItemCount()
        android.util.Log.d("MediaViewModel", "當前數據庫中的媒體項目數量: $count")
    }
    
    /**
     * 更新現有的媒體項目。
     */
    fun updateMediaItem(mediaItem: MediaItem) = viewModelScope.launch {
        repository.updateMediaItem(mediaItem)
    }
    
    /**
     * 刪除媒體項目。
     */
    fun deleteMediaItem(mediaItem: MediaItem) = viewModelScope.launch {
        repository.deleteMediaItem(mediaItem)
    }
    
    /**
     * 根據ID獲取媒體項目。
     */
    suspend fun getMediaItemById(id: String): MediaItem? {
        return repository.getMediaItemById(id)
    }
}