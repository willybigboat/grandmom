package com.example.grandmom.test

import android.util.Log
import com.example.grandmom.data.model.MediaItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firebase 測試工具類，用於測試和驗證 Firebase 同步功能
 */
object FirebaseTestHelper {
    
    private const val TAG = "FirebaseTestHelper"
    private val firestore = FirebaseFirestore.getInstance()
    
    /**
     * 創建測試數據到 Firestore
     */
    suspend fun createTestData() {
        val testItems = listOf(
            MediaItem(
                id = "1",
                title = "奶奶的老照片",
                imageUrl = "https://example.com/images/grandma1.jpg",
                audioUrl = "https://example.com/audios/grandma1.mp3",
                createDate = System.currentTimeMillis() - 86400000 // 1天前
            ),
            MediaItem(
                id = "2",
                title = "家庭聚餐回憶",
                imageUrl = "https://example.com/images/family1.jpg",
                audioUrl = "https://example.com/audios/family1.mp3",
                createDate = System.currentTimeMillis() - 172800000 // 2天前
            ),
            MediaItem(
                id = "3",
                title = "童年故事",
                imageUrl = "https://example.com/images/childhood1.jpg",
                audioUrl = "https://example.com/audios/childhood1.mp3",
                createDate = System.currentTimeMillis() - 259200000 // 3天前
            )
        )
        
        testItems.forEach { item ->
            try {
                firestore.collection("mediaItems")
                    .document(item.id)
                    .set(item)
                    .await()
                Log.d(TAG, "Test item created: ${item.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating test item: ${item.title}", e)
            }
        }
        
        Log.d(TAG, "Test data creation completed")
    }
    
    /**
     * 清理測試數據
     */
    suspend fun clearTestData() {
        try {
            val snapshot = firestore.collection("mediaItems").get().await()
            snapshot.documents.forEach { document ->
                document.reference.delete().await()
                Log.d(TAG, "Deleted test document: ${document.id}")
            }
            Log.d(TAG, "Test data cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing test data", e)
        }
    }
    
    /**
     * 列出 Firestore 中的所有項目
     */
    suspend fun listFirestoreItems() {
        try {
            val snapshot = firestore.collection("mediaItems").get().await()
            Log.d(TAG, "Firestore items count: ${snapshot.documents.size}")
            
            snapshot.documents.forEach { document ->
                val mediaItem = document.toObject(MediaItem::class.java)
                Log.d(TAG, "Firestore item: ${document.id} - ${mediaItem?.title}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing Firestore items", e)
        }
    }
    
    /**
     * 手動觸發同步 (在實際使用中應該通過 Application 中的 FirebaseManager 調用)
     */
    suspend fun triggerManualSync() {
        Log.d(TAG, "Manual sync would be triggered via FirebaseManager.initialize()")
        Log.d(TAG, "This should be called from your Application class or main activity")
    }
}