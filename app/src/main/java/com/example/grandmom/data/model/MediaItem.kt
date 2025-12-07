package com.example.grandmom.data.model

import android.R
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 代表一張圖片及其關聯的音檔。
 *
 * @property id 項目的唯一識別碼
 * @property title 項目的標題（可選）
 * @property imagePath 圖片在設備上的儲存路徑
 * @property audioPath 音檔在設備上的儲存路徑
 * @property createDate 創建日期的時間戳
 */
@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey
    val id: String,
    val title: String = "",
    val createDate: Long = System.currentTimeMillis(),
    val imageUrl: String = "", // Firebase Firestore URL
    val audioUrl: String = "",  // Firebase Firestore URL
    var localImagePath: String = "", // 本地圖片路徑
    var localAudioPath: String = ""  // 本地音訊路徑
) {
    // Firebase Firestore 需要無參數構造函數
    constructor() : this("", "", System.currentTimeMillis(), "", "", "", "")
}