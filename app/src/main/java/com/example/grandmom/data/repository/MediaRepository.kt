package com.example.grandmom.data.repository

import com.example.grandmom.data.dao.MediaItemDao
import com.example.grandmom.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

/**
 * 媒體項目資料存取的儲存庫，作為資料庫操作和應用程式其他部分之間的中介。
 */
class MediaRepository(private val mediaItemDao: MediaItemDao) {
    
    /**
     * 獲取所有媒體項目。
     */
    val allMediaItems: Flow<List<MediaItem>> = mediaItemDao.getAllMediaItems()
    
    /**
     * 插入新的媒體項目。
     */
    suspend fun insertMediaItem(mediaItem: MediaItem) {
        mediaItemDao.insertMediaItem(mediaItem)
    }
    
    /**
     * 更新現有的媒體項目。
     */
    suspend fun updateMediaItem(mediaItem: MediaItem) {
        mediaItemDao.updateMediaItem(mediaItem)
    }
    
    /**
     * 刪除媒體項目。
     */
    suspend fun deleteMediaItem(mediaItem: MediaItem) {
        mediaItemDao.deleteMediaItem(mediaItem)
    }
    
    /**
     * 根據ID獲取媒體項目。
     */
    suspend fun getMediaItemById(id: String): MediaItem? {
        return mediaItemDao.getMediaItemById(id)
    }

    /**
     * 批量插入多個媒體項目。
     */
    suspend fun insertMediaItems(mediaItems: List<MediaItem>) {
        mediaItemDao.insertMediaItems(mediaItems)
    }

    /**
     * 獲取所有媒體項目的ID列表。
     */
    suspend fun getAllMediaItemIds(): List<String> {
        return mediaItemDao.getAllMediaItemIds()
    }

    /**
     * 根據ID列表刪除多個媒體項目。
     */
    suspend fun deleteMediaItemsByIds(ids: List<String>) {
        mediaItemDao.deleteMediaItemsByIds(ids)
    }
    
    /**
     * 獲取媒體項目的總數。
     */
    suspend fun getMediaItemCount(): Int {
        return mediaItemDao.getMediaItemCount()
    }
}