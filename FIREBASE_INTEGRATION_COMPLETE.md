# Firebase 集成完成 - 測試指南

## 🎉 實施完成

Firebase 與 App 的集成已成功完成，實現了您要求的所有功能：

### ✅ 已實現的功能

1. **應用啟動時 Firebase 連接**
   - App 啟動時自動連接 Firebase
   - 執行匿名登入
   - 連接失敗時優雅關閉，不重複嘗試

2. **Firestore 資料同步**
   - 獲取 Firestore 中 `mediaItems` 集合的所有文件 ID
   - 與本地 Room 資料庫進行比對
   - 自動下載缺少的項目
   - 自動刪除多餘的本地項目

3. **批次處理**
   - 使用 `whereIn` 查詢，每次最多處理 30 個 ID
   - 支援 `OnConflictStrategy.REPLACE` 策略

4. **離線支援**
   - 網路斷開時仍可使用本地 Room 資料
   - 重新連網時自動觸發同步

### 📁 新增的檔案

1. **FirebaseManager.kt** - 主要的 Firebase 管理類
2. **FirebaseTestHelper.kt** - 測試工具類
3. **FirebaseDebugTool.kt** - Debug 工具類

### 🔧 修改的檔案

1. **MediaItemDao.kt** - 新增批次操作方法
2. **MediaRepository.kt** - 新增同步相關方法
3. **MediaItem.kt** - 新增 Firestore 支援
4. **GrandmomApp.kt** - 初始化 FirebaseManager
5. **AndroidManifest.xml** - 新增網路權限
6. **build.gradle.kts** - 新增 Firebase 依賴項
7. **libs.versions.toml** - 新增 Firebase 版本配置

## 🧪 如何測試

### 方法 1: 使用 Debug 工具 (推薦)

1. 在您的任何 Activity 中加入 Debug 工具：

```kotlin
import com.example.grandmom.debug.FirebaseDebugTool

class MainActivity : ComponentActivity() {
    private lateinit var debugTool: FirebaseDebugTool
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as GrandmomApp
        debugTool = FirebaseDebugTool(this, app.repository, lifecycleScope)
        
        // 執行完整測試
        debugTool.runFullTest()
    }
}
```

### 方法 2: 查看日誌

1. 執行應用程式
2. 在 Android Studio 的 Logcat 中搜尋以下標籤：
   - `FirebaseManager`
   - `FirebaseTestHelper`
   - `FirebaseDebugTool`

### 測試步驟

1. **首次測試**:
   ```kotlin
   debugTool.createTestDataOnly() // 創建測試數據到 Firestore
   ```

2. **重啟應用** - 這會觸發自動同步

3. **檢查結果**:
   ```kotlin
   debugTool.showCurrentStatus() // 查看本地和雲端數據
   ```

## 📊 預期的同步行為

1. **App 啟動** → Firebase 匿名登入
2. **獲取 Firestore IDs** → 例如: ["1", "2", "3"]
3. **獲取本地 IDs** → 例如: [1, 4, 5]
4. **計算差異**:
   - 需要下載: ["2", "3"] (Firestore 有但本地沒有)
   - 需要刪除: [4, 5] (本地有但 Firestore 沒有)
5. **執行同步**:
   - 刪除本地項目 4, 5
   - 從 Firestore 下載項目 2, 3
6. **結果**: 本地資料庫現在包含項目 [1, 2, 3]

## 🔧 客製化選項

如果需要修改同步行為，請編輯 `FirebaseManager.kt`：

- 修改集合名稱: 更改 `MEDIA_ITEMS_COLLECTION`
- 調整批次大小: 更改 `chunked(30)` 中的數字
- 修改同步邏輯: 編輯 `synchronizeData()` 方法

## 🚨 注意事項

1. **確保已設置 Firebase 專案** 並將 `google-services.json` 放在正確位置
2. **Firestore 安全規則** 需要允許匿名讀寫訪問
3. **網路權限** 已在 AndroidManifest.xml 中添加
4. **title 字段** 將作為應用中的顯示標題

## 📱 生產環境部署

在部署到生產環境前，請：

1. 移除或禁用測試工具類
2. 設置適當的 Firestore 安全規則
3. 考慮添加錯誤回報機制
4. 實施適當的用戶反饋（載入指示器等）

## 🎯 測試完成指標

✅ Firebase 匿名登入成功  
✅ Firestore 資料讀取成功  
✅ 本地資料庫同步成功  
✅ 離線模式正常運作  
✅ 應用重啟後自動同步  

---

**恭喜！Firebase 集成已完成並可以開始測試！** 🎉