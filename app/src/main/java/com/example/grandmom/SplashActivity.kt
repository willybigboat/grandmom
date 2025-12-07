package com.example.grandmom

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.grandmom.ui.theme.GrandmomTheme
import com.example.grandmom.util.Constants
import kotlinx.coroutines.delay

/**
 * 啟動屏幕，用於展示應用Logo和名稱，並執行初始化操作。
 */
class SplashActivity : ComponentActivity() {
    private lateinit var prefs: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化SharedPreferences
        prefs = getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
        
        setContent {
            GrandmomTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SplashScreen(
                        onSplashFinished = {
                            // 啟動主活動
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish() // 結束啟動屏幕
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    var showError by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = true) {
        try {
            // 模擬初始化操作，例如檢查更新、預加載資源等
            delay(2000) // 顯示啟動屏幕2秒
            onSplashFinished()
        } catch (e: Exception) {
            e.printStackTrace()
            showError = true
            Toast.makeText(
                context,
                "初始化失敗，請重新啟動應用",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 應用名稱
            Text(
                text = "阿罵李賀!",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 應用描述
            Text(
                text = "BY 清濬 ",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 錯誤信息（如果有）
            if (showError) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "加載失敗，請重新啟動應用",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}