package com.example.grandmom.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.grandmom.data.dao.MediaItemDao
import com.example.grandmom.data.model.MediaItem

/**
 * 應用程式的主要資料庫。
 */
@Database(entities = [MediaItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * 提供對MediaItemDao的存取。
     */
    abstract fun mediaItemDao(): MediaItemDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * 獲取數據庫實例，如果不存在則創建。
         * @param context 應用程式上下文
         * @return 數據庫實例
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "grandmom_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}