package com.example.grandmom.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.grandmom.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

/**
 * 資料訪問對象(DAO)，用於與媒體項目資料表進行交互。
 */
@Dao
interface MediaItemDao {
    
    /**
     * 插入新的媒體項目到資料庫中。
     * @param mediaItem 要儲存的媒體項目
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(mediaItem: MediaItem)
    
    /**
     * 更新現有的媒體項目。
     * @param mediaItem 要更新的媒體項目
     */
    @Update
    suspend fun updateMediaItem(mediaItem: MediaItem)
    
    /**
     * 刪除指定的媒體項目。
     * @param mediaItem 要刪除的媒體項目
     */
    @Delete
    suspend fun deleteMediaItem(mediaItem: MediaItem)
    
    /**
     * 根據ID獲取單個媒體項目。
     * @param id 媒體項目的ID
     * @return 對應ID的媒體項目
     */
    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaItemById(id: String): MediaItem?
    
    /**
     * 獲取所有媒體項目，按創建日期降序排列。
     * @return 媒體項目流
     */
    @Query("SELECT * FROM media_items ORDER BY createDate DESC")
    fun getAllMediaItems(): Flow<List<MediaItem>>

    /**
     * 批量插入多個媒體項目。
     * @param mediaItems 要插入的媒體項目列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItems(mediaItems: List<MediaItem>)

    /**
     * 獲取所有媒體項目的ID列表。
     * @return 所有媒體項目的ID列表
     */
    @Query("SELECT id FROM media_items")
    suspend fun getAllMediaItemIds(): List<String>

    /**
     * 根據ID列表刪除多個媒體項目。
     * @param ids 要刪除的媒體項目ID列表
     */
    @Query("DELETE FROM media_items WHERE id IN (:ids)")
    suspend fun deleteMediaItemsByIds(ids: List<String>)
    
    /**
     * 獲取媒體項目的總數。
     * @return 媒體項目的總數
     */
    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getMediaItemCount(): Int
}