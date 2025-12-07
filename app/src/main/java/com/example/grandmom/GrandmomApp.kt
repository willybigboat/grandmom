package com.example.grandmom

import android.app.Application
import com.example.grandmom.data.AppDatabase
import com.example.grandmom.data.remote.FirebaseManager
import com.example.grandmom.data.repository.MediaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * 自定義的應用程式類，用於初始化全局資源。
 */
class GrandmomApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())
    
    // 透過lazy初始化資料庫
    private val database by lazy { AppDatabase.getDatabase(this) }
    
    // 初始化Repository
    val repository by lazy { MediaRepository(database.mediaItemDao()) }

    // 初始化 FirebaseManager
    private val firebaseManager by lazy {
        FirebaseManager(this, repository, applicationScope)
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // 確保 Firebase 初始化
            com.google.firebase.FirebaseApp.initializeApp(this)
            android.util.Log.d("GrandmomApp", "Firebase 已在 Application 中初始化")
            
            // 初始化 Firebase 管理器並觸發同步
            firebaseManager.initialize()
        } catch (e: Exception) {
            android.util.Log.e("GrandmomApp", "初始化 Firebase 時發生錯誤: ${e.message}", e)
        }
    }
}