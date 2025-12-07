package com.example.grandmom.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.grandmom.data.model.MediaItem
import java.io.File

/**
 * 顯示帶有圖片和音頻播放按鈕的媒體項目卡片。
 */
@Composable
fun MediaItemCard(
    mediaItem: MediaItem,
    onPlayAudio: (MediaItem) -> Unit,
    onItemClick: (MediaItem) -> Unit,
    onItemLongClick: (MediaItem) -> Unit = onItemClick, // 預設與點擊相同的行為
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .pointerInput(mediaItem) {
                detectTapGestures(
                    //onTap = { onItemClick(mediaItem) },
                    onLongPress = { onItemLongClick(mediaItem) }
                )
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column {
            // 圖片
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(640.dp)
            ) {
                val imageData = if (mediaItem.localImagePath.isNotEmpty()) {
                    // 使用本地圖片路徑
                    android.util.Log.d("MediaItemCard", "使用本地圖片路徑: ${mediaItem.localImagePath}")
                    val file = File(mediaItem.localImagePath)
                    
                    // 檢查本地檔案是否存在且大小 > 0
                    if (file.exists() && file.length() > 0) {
                        android.util.Log.d("MediaItemCard", "本地檔案存在且有效，大小: ${file.length() / 1024}KB")
                        file
                    } else {
                        // 本地檔案不存在或為空，使用 URL
                        android.util.Log.d("MediaItemCard", "本地檔案不存在或為空，使用遙端URL: ${mediaItem.imageUrl}")
                        mediaItem.imageUrl
                    }
                } else {
                    // 如果沒有本地路徑，使用 URL
                    android.util.Log.d("MediaItemCard", "沒有本地路徑，使用遙端圖片URL: ${mediaItem.imageUrl}")
                    mediaItem.imageUrl
                }
                
                // 嘗試載入圖片，添加日誌以追蹤載入狀態
                android.util.Log.d("MediaItemCard", "嘗試載入圖片資料: $imageData")
                
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(imageData)
                            .listener(
                                onStart = { request ->
                                    android.util.Log.d("MediaItemCard", "開始載入圖片: ${request.data}")
                                },
                                onError = { request, error -> 
                                    android.util.Log.e("MediaItemCard", "圖片載入失敗: ${error.throwable.message}")
                                },
                                onSuccess = { request, result ->
                                    android.util.Log.d("MediaItemCard", "圖片載入成功")
                                }
                            )
                            .build()
                    ),
                    contentDescription = "圖片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 標題（如果有）
            if (mediaItem.title.isNotEmpty()) {
                Text(
                    text = mediaItem.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            // 音頻播放按鈕
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { onPlayAudio(mediaItem) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "播放音頻",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}