package com.example.grandmom.debug

import android.content.Context
import android.util.Log
import com.example.grandmom.data.repository.MediaRepository
import com.example.grandmom.test.FirebaseTestHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Debug 工具，可以從 UI (例如在設置頁面) 調用來測試 Firebase 功能
 */
class FirebaseDebugTool(
    private val context: Context,
    private val mediaRepository: MediaRepository,
    private val coroutineScope: CoroutineScope
) {
    
    companion object {
        private const val TAG = "FirebaseDebugTool"
    }
    
    /**
     * 執行完整的 Firebase 測試流程
     */
    fun runFullTest() {
        coroutineScope.launch {
            Log.d(TAG, "=== Starting Firebase Full Test ===")
            
            try {
                // 1. 清理舊的測試數據
                Log.d(TAG, "Step 1: Clearing old test data...")
                FirebaseTestHelper.clearTestData()
                
                // 2. 創建新的測試數據
                Log.d(TAG, "Step 2: Creating test data in Firestore...")
                FirebaseTestHelper.createTestData()
                
                // 3. 列出 Firestore 中的項目
                Log.d(TAG, "Step 3: Listing Firestore items...")
                FirebaseTestHelper.listFirestoreItems()
                
                // 4. 列出本地數據庫中的項目
                Log.d(TAG, "Step 4: Listing local database items...")
                val localIds = mediaRepository.getAllMediaItemIds()
                Log.d(TAG, "Local database item IDs: $localIds")
                
                // 5. 提示用戶重新啟動應用程序以觸發同步
                Log.d(TAG, "Step 5: Please restart the app to trigger Firebase sync")
                Log.d(TAG, "The sync will happen automatically when the app starts")
                
                Log.d(TAG, "=== Firebase Full Test Completed ===")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during Firebase test", e)
            }
        }
    }
    
    /**
     * 只創建測試數據
     */
    fun createTestDataOnly() {
        coroutineScope.launch {
            Log.d(TAG, "Creating test data in Firestore...")
            try {
                FirebaseTestHelper.createTestData()
                Log.d(TAG, "Test data created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating test data", e)
            }
        }
    }
    
    /**
     * 只清理測試數據
     */
    fun clearTestDataOnly() {
        coroutineScope.launch {
            Log.d(TAG, "Clearing test data from Firestore...")
            try {
                FirebaseTestHelper.clearTestData()
                Log.d(TAG, "Test data cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing test data", e)
            }
        }
    }
    
    /**
     * 列出當前狀態
     */
    fun showCurrentStatus() {
        coroutineScope.launch {
            Log.d(TAG, "=== Current Status ===")
            try {
                // 列出 Firestore 項目
                FirebaseTestHelper.listFirestoreItems()
                
                // 列出本地項目
                val localIds = mediaRepository.getAllMediaItemIds()
                Log.d(TAG, "Local database items count: ${localIds.size}")
                Log.d(TAG, "Local database item IDs: $localIds")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error showing current status", e)
            }
        }
    }
}