package com.example.grandmom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.grandmom.ui.AppNavigation
import com.example.grandmom.ui.components.UsageHintDialog
import com.example.grandmom.ui.theme.GrandmomTheme
import com.example.grandmom.util.AudioPlayerUtil
import com.example.grandmom.util.Constants
import com.example.grandmom.util.DefaultMediaLoader
import com.example.grandmom.util.PermissionUtil
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 取得SharedPreferences來儲存使用提示狀態及首次啟動狀態
        val prefs = getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
        
        // 檢查是否首次啟動應用程式
        val isFirstLaunch = prefs.getBoolean(Constants.PREF_FIRST_LAUNCH, true)
        
        // 若是首次啟動，則載入預設媒體
        if (isFirstLaunch) {
            lifecycleScope.launch {
                DefaultMediaLoader.loadDefaultMedia(this@MainActivity)
                // 設置標記，表示已經載入預設媒體
                prefs.edit().putBoolean(Constants.PREF_FIRST_LAUNCH, false).apply()
            }
        }
        
        setContent {
            GrandmomTheme {
                // 檢查是否顯示提示，默認為true
                var showHint by remember { mutableStateOf(prefs.getBoolean(Constants.PREF_SHOW_HINT, true)) }
                
                if (showHint) {
                    UsageHintDialog(onDismiss = { 
                        showHint = false
                        // 保存狀態，下次不再顯示提示
                        prefs.edit().putBoolean(Constants.PREF_SHOW_HINT, false).apply()
                    })
                }
                
                PermissionHandler {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioPlayerUtil.stopAudio()
    }
}

@Composable
fun PermissionHandler(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var hasPermissions by remember { mutableStateOf(PermissionUtil.hasRequiredPermissions(context)) }

    // 1) 在組合過程中「註冊」launcher（正確時機）
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermissions = result.values.all { it }
    }

    if (hasPermissions) {
        content()
    } else {
        // 2) 僅在「沒有權限」時，以一次性副作用觸發請求
        LaunchedEffect(Unit) {
            permissionLauncher.launch(PermissionUtil.getRequiredPermissions())
        }

        // 等待授權的簡單 UI（避免空白畫面）
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    text = "等待權限授予…",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}
