package com.example.grandmom.util

/**
 * 常數值定義
 */
object Constants {
    // 共享偏好設置名稱
    const val PREFERENCES_NAME = "grandmom_preferences"
    
    // 首次啟動標識
    const val PREF_FIRST_LAUNCH = "first_launch"
    
    // 用於顯示提示
    const val PREF_SHOW_HINT = "show_hint"
    
    // 媒體項目詳情頁面的參數名稱
    const val PARAM_ITEM_ID = "itemId"
    
    // 許可權請求碼
    const val REQUEST_PERMISSIONS_CODE = 100
    
    // 錯誤消息
    object ErrorMessages {
        const val ERROR_LOADING_MEDIA = "無法載入媒體"
        const val ERROR_SAVING_MEDIA = "無法保存媒體"
        const val ERROR_PLAYING_AUDIO = "無法播放音頻"
        const val ERROR_PERMISSION_DENIED = "需要權限才能使用此功能"
    }
    
    // 按鈕文字
    object ButtonLabels {
        const val ADD_NEW = "添加新項目"
        const val SAVE = "保存"
        const val DELETE = "刪除"
        const val EDIT = "編輯"
        const val CANCEL = "取消"
        const val CONFIRM = "確認"
        const val BACK = "返回"
        const val PLAY_AUDIO = "播放音頻"
    }
    
    // 頁面標題
    object ScreenTitles {
        const val HOME = "圖片與聲音"
        const val ADD = "添加新項目"
        const val EDIT = "編輯項目"
        const val DETAILS = "項目詳情"
    }
}