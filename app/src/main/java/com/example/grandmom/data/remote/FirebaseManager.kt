package com.example.grandmom.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.grandmom.data.model.MediaItem
import com.example.grandmom.data.repository.MediaRepository
import com.example.grandmom.util.DownloadUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class FirebaseManager(
    private val context: Context,
    private val mediaRepository: MediaRepository,
    private val coroutineScope: CoroutineScope
) {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    companion object {
        private const val TAG = "FirebaseManager"
        private const val MEDIA_ITEMS_COLLECTION = "mediaItem"
    }

    fun initialize() {
        Log.d(TAG, "初始化 Firebase Manager...")
        try {
            // 確認 Firebase 應用已初始化
            val app = com.google.firebase.FirebaseApp.getInstance()
            Log.d(TAG, "Firebase 應用已初始化: ${app.name}")
            
            if (isNetworkAvailable()) {
                Log.d(TAG, "網路可用，正在進行匿名登入和同步...")
                coroutineScope.launch {
                    try {
                        signInAnonymouslyAndSync()
                    } catch (e: Exception) {
                        Log.e(TAG, "匿名登入過程中發生錯誤: ${e.message}", e)
                    }
                }
                
                // 添加直接檢查的模式，方便除錯
                Log.d(TAG, "立即檢查 Firestore 中的媒體項目")
                firestore.collection(MEDIA_ITEMS_COLLECTION)
                    .get()
                    .addOnSuccessListener { documents ->
                        Log.d(TAG, "直接查詢結果: 文檔數量 = ${documents.size()}")
                        for (document in documents) {
                            val data = document.data
                            Log.d(TAG, "ID: ${document.id}, 標題: ${data["title"]}, 圖片URL: ${data["imageUrl"]}, 音訊URL: ${data["audioUrl"]}")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "直接查詢失敗: ${e.message}")
                    }
            } else {
                Log.d(TAG, "沒有網路連接，跳過 Firebase 同步。")
            }
        } catch (e: Exception) {
            Log.e(TAG, "初始化 Firebase 時發生錯誤: ${e.message}", e)
        }
    }

    private suspend fun signInAnonymouslyAndSync() {
        try {
            Log.d(TAG, "開始匿名登入過程...")
            
            // 檢查是否已登入
            val user = auth.currentUser
            if (user == null) {
                Log.d(TAG, "沒有當前用戶，嘗試匿名登入")
                try {
                    val result = auth.signInAnonymously().await()
                    if (result.user != null) {
                        Log.d(TAG, "匿名登入成功，UID: ${result.user?.uid}")
                    } else {
                        Log.e(TAG, "匿名登入成功但用戶為空")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "匿名登入失敗: ${e.message}", e)
                    // 繼續執行，嘗試離線操作
                    return
                }
            } else {
                Log.d(TAG, "已經登入，用戶 UID: ${user.uid}")
            }
            
            // 只有當登入成功後才同步數據
            if (auth.currentUser != null) {
                synchronizeData()
            } else {
                Log.w(TAG, "無法同步數據，因為未登入")
            }
        } catch (e: Exception) {
            Log.e(TAG, "匿名登入或同步數據時出錯: ${e.message}", e)
        }
    }

    private suspend fun synchronizeData() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Get all document IDs from Firestore
                val firestoreDocuments = firestore.collection(MEDIA_ITEMS_COLLECTION)
                    .get()
                    .await()
                    .documents
                
                val firestoreIds = firestoreDocuments.map { it.id }.toSet()
                Log.d(TAG, "Firestore IDs: $firestoreIds")

                // 2. Get all local IDs from Room
                val localIds = mediaRepository.getAllMediaItemIds().toSet()
                Log.d(TAG, "Local IDs: $localIds")

                // 3. 只計算 Firebase 中有但本地沒有的新項目
                val newIds = firestoreIds.minus(localIds)
                
                // 不再計算需要從本地刪除的項目，保留所有本地資料
                Log.d(TAG, "新的項目 IDs: $newIds")
                Log.d(TAG, "注意：不會刪除本機端獨有的資料項目，即使它們不在 Firebase 中")
                
                // 移除刪除本地資料的步驟，保留所有本地資料

                // 5. 處理 Firestore 文件
                if (firestoreDocuments.isNotEmpty()) {
                    processFirestoreDocuments(firestoreDocuments, newIds)
                }
                
                // 不再清理媒體檔案，避免刪除用戶自行添加的內容
                // cleanupUnusedMediaFiles() - 已禁用，保留所有本地檔案

                Log.d(TAG, "Synchronization completed successfully.")

            } catch (e: Exception) {
                Log.e(TAG, "Error during data synchronization.", e)
            }
        }
    }

    private suspend fun processFirestoreDocuments(documents: List<DocumentSnapshot>, newIds: Set<String>) {
        withContext(Dispatchers.IO) {
            try {
                val mediaItemsToInsert = mutableListOf<MediaItem>()
                val mediaItemsToUpdate = mutableListOf<MediaItem>()
                
                for (document in documents) {
                    val mediaItem = document.toObject(MediaItem::class.java)?.copy(id = document.id)
                        ?: continue
                    
                    // 記錄項目詳細資訊
                    Log.d(TAG, "處理媒體項目: ${mediaItem.id}")
                    Log.d(TAG, "  標題: ${mediaItem.title}")
                    Log.d(TAG, "  圖片URL: ${mediaItem.imageUrl}")
                    Log.d(TAG, "  音訊URL: ${mediaItem.audioUrl}")
                    
                    if (newIds.contains(mediaItem.id)) {
                        // 這是新項目，需要下載媒體檔案
                        downloadMediaFiles(mediaItem)
                        mediaItemsToInsert.add(mediaItem)
                    } else {
                        // 這是現有項目，僅檢查並下載缺少的媒體檔案
                        val existingItem = mediaRepository.getMediaItemById(mediaItem.id)
                        
                        if (existingItem != null) {
                            // 檢查本地路徑是否有效
                            val needUpdate = checkAndDownloadMissingFiles(mediaItem, existingItem)
                            
                            if (needUpdate) {
                                mediaItemsToUpdate.add(mediaItem)
                            }
                        }
                    }
                }
                
                // 儲存新項目
                if (mediaItemsToInsert.isNotEmpty()) {
                    mediaRepository.insertMediaItems(mediaItemsToInsert)
                    Log.d(TAG, "插入了 ${mediaItemsToInsert.size} 個新媒體項目")
                }
                
                // 更新現有項目
                for (item in mediaItemsToUpdate) {
                    mediaRepository.updateMediaItem(item)
                    Log.d(TAG, "更新了媒體項目: ${item.id}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "處理 Firestore 文件時發生錯誤: ${e.message}", e)
            }
        }
    }

    private suspend fun checkAndDownloadMissingFiles(newItem: MediaItem, existingItem: MediaItem): Boolean {
        var needUpdate = false
        
        // 檢查圖片檔案
        if (existingItem.localImagePath.isEmpty() && newItem.imageUrl.isNotEmpty()) {
            // 本地沒有圖片，但有 URL，下載它
            Log.d(TAG, "下載缺少的圖片: ${newItem.imageUrl}")
            val localImagePath = DownloadUtil.downloadImageToInternalStorage(context, newItem.imageUrl)
            if (localImagePath != null) {
                newItem.localImagePath = localImagePath
                needUpdate = true
                Log.d(TAG, "圖片已下載到本地: $localImagePath")
            }
        } else {
            // 保留現有的本地路徑
            newItem.localImagePath = existingItem.localImagePath
        }
        
        // 檢查音訊檔案
        if (existingItem.localAudioPath.isEmpty() && newItem.audioUrl.isNotEmpty()) {
            // 本地沒有音訊，但有 URL，下載它
            Log.d(TAG, "下載缺少的音訊: ${newItem.audioUrl}")
            val localAudioPath = DownloadUtil.downloadAudioToInternalStorage(context, newItem.audioUrl)
            if (localAudioPath != null) {
                newItem.localAudioPath = localAudioPath
                needUpdate = true
                Log.d(TAG, "音訊已下載到本地: $localAudioPath")
            }
        } else {
            // 保留現有的本地路徑
            newItem.localAudioPath = existingItem.localAudioPath
        }
        
        return needUpdate
    }
    
    private suspend fun downloadMediaFiles(mediaItem: MediaItem) {
        // 下載圖片
        if (mediaItem.imageUrl.isNotEmpty()) {
            Log.d(TAG, "下載圖片: ${mediaItem.imageUrl}")
            val localImagePath = DownloadUtil.downloadImageToInternalStorage(context, mediaItem.imageUrl)
            if (localImagePath != null) {
                mediaItem.localImagePath = localImagePath
                Log.d(TAG, "圖片已下載到本地: $localImagePath")
            } else {
                Log.e(TAG, "圖片下載失敗: ${mediaItem.imageUrl}")
            }
        }
        
        // 下載音訊
        if (mediaItem.audioUrl.isNotEmpty()) {
            Log.d(TAG, "下載音訊: ${mediaItem.audioUrl}")
            val localAudioPath = DownloadUtil.downloadAudioToInternalStorage(context, mediaItem.audioUrl)
            if (localAudioPath != null) {
                mediaItem.localAudioPath = localAudioPath
                Log.d(TAG, "音訊已下載到本地: $localAudioPath")
            } else {
                Log.e(TAG, "音訊下載失敗: ${mediaItem.audioUrl}")
            }
        }
    }

    /**
     * 刪除媒體項目的相關檔案
     */
    private fun deleteMediaFiles(mediaItems: List<MediaItem>) {
        try {
            var imageFilesDeleted = 0
            var audioFilesDeleted = 0
            
            for (item in mediaItems) {
                // 刪除圖片檔案
                if (item.localImagePath.isNotEmpty()) {
                    val imageFile = File(item.localImagePath)
                    if (imageFile.exists()) {
                        val deleted = imageFile.delete()
                        if (deleted) {
                            imageFilesDeleted++
                            Log.d(TAG, "已刪除圖片檔案: ${item.localImagePath}")
                        } else {
                            Log.e(TAG, "無法刪除圖片檔案: ${item.localImagePath}")
                        }
                    }
                }
                
                // 刪除音訊檔案
                if (item.localAudioPath.isNotEmpty()) {
                    val audioFile = File(item.localAudioPath)
                    if (audioFile.exists()) {
                        val deleted = audioFile.delete()
                        if (deleted) {
                            audioFilesDeleted++
                            Log.d(TAG, "已刪除音訊檔案: ${item.localAudioPath}")
                        } else {
                            Log.e(TAG, "無法刪除音訊檔案: ${item.localAudioPath}")
                        }
                    }
                }
            }
            
            Log.d(TAG, "總共刪除了 $imageFilesDeleted 個圖片檔案和 $audioFilesDeleted 個音訊檔案")
        } catch (e: Exception) {
            Log.e(TAG, "刪除媒體檔案時發生錯誤: ${e.message}", e)
        }
    }
    
    /**
     * 清理未使用的媒體檔案（不再在資料庫中的本地檔案）
     */
    private suspend fun cleanupUnusedMediaFiles() {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "開始清理未使用的媒體檔案...")
                
                // 獲取所有資料庫中的媒體項目路徑
                val allItems = mediaRepository.allMediaItems.first()
                val validImagePaths = allItems.mapNotNull { item -> 
                    if (item.localImagePath.isNotEmpty()) item.localImagePath else null 
                }.toSet()
                val validAudioPaths = allItems.mapNotNull { item -> 
                    if (item.localAudioPath.isNotEmpty()) item.localAudioPath else null 
                }.toSet()
                
                // 獲取內部存儲中的所有媒體檔案
                val filesDir = context.filesDir
                val allFiles = filesDir.listFiles() ?: return@withContext
                
                var unusedFilesDeleted = 0
                
                for (file in allFiles) {
                    val path = file.absolutePath
                    
                    // 檢查是否為圖片或音訊檔案
                    val isMediaFile = path.contains("img_") || path.contains("audio_")
                    
                    if (isMediaFile && !validImagePaths.contains(path) && !validAudioPaths.contains(path)) {
                        // 這個檔案不在資料庫中，可以刪除它
                        val deleted = file.delete()
                        if (deleted) {
                            unusedFilesDeleted++
                            Log.d(TAG, "已刪除未使用的媒體檔案: $path")
                        } else {
                            Log.e(TAG, "無法刪除未使用的媒體檔案: $path")
                        }
                    }
                }
                
                Log.d(TAG, "總共刪除了 $unusedFilesDeleted 個未使用的媒體檔案")
                
            } catch (e: Exception) {
                Log.e(TAG, "清理未使用的媒體檔案時發生錯誤: ${e.message}", e)
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}