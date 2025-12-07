package com.example.grandmom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
//import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.launch
import com.example.grandmom.data.model.MediaItem
import com.example.grandmom.ui.components.MediaItemCard
import com.example.grandmom.ui.viewmodel.MediaViewModel
import com.example.grandmom.util.AudioPlayerUtil

/**
 * 主屏幕，顯示所有媒體項目的列表。
 */
@Composable
fun HomeScreen(
    mediaViewModel: MediaViewModel,
    navigateToAddEdit: () -> Unit,
    navigateToDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val mediaItems by mediaViewModel.allMediaItems.collectAsState(initial = emptyList())
    
    // 在這裡添加日誌輸出，檢查媒體項目數量
    android.util.Log.d("HomeScreen", "當前媒體項目數量: ${mediaItems.size}")
    
    Scaffold(
        // 移除FAB，因為我們已將新增按鈕移至右上角
    ) { paddingValues ->
        if (mediaItems.isEmpty()) {
            // 空狀態
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "沒有項目",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "點擊右下角的加號按鈕來添加新的圖片和音頻\n長按項目可以編輯",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // 使用固定卡片顯示媒體項目，用按鈕切換
            val currentIndex = remember { mutableStateOf(0) }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 顯示當前選中的媒體項目
                if (mediaItems.isNotEmpty()) {
                    // 確保索引在有效範圍內
                    if (currentIndex.value >= mediaItems.size) {
                        currentIndex.value = 0
                    }
                    val currentItem = mediaItems[currentIndex.value]
                    // 輸出当前項目詳細信息
                    android.util.Log.d("HomeScreen", "顯示媒體項目 ID: ${currentItem.id}")
                    android.util.Log.d("HomeScreen", "  標題: ${currentItem.title}")
                    android.util.Log.d("HomeScreen", "  圖片URL: ${currentItem.imageUrl}")
                    android.util.Log.d("HomeScreen", "  音訊URL: ${currentItem.audioUrl}")
                    android.util.Log.d("HomeScreen", "  本地圖片路徑: ${currentItem.localImagePath}")
                    android.util.Log.d("HomeScreen", "  本地音訊路徑: ${currentItem.localAudioPath}")
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        MediaItemCard(
                            mediaItem = currentItem,
                            onPlayAudio = { mediaItem ->
                                android.util.Log.d("HomeScreen", "嘗試播放音訊: ${mediaItem.audioUrl} / ${mediaItem.localAudioPath}")
                                com.example.grandmom.util.AudioPlayerHelper.playAudio(context, mediaItem)
                            },
                            onItemClick = {},
                            /* onItemClick = { mediaItem ->
                                // 點擊刪除
                                android.util.Log.d("HomeScreen", "刪除媒體項目: ${mediaItem.id}")
                                
                                // 顯示確認對話框
                                val builder = android.app.AlertDialog.Builder(context)
                                builder.setTitle("確認刪除")
                                    .setMessage("確定要刪除這個媒體項目嗎？")
                                    .setPositiveButton("確定") { _, _ ->
                                        // 先保存當前索引和項目總數
                                        val currentItemIndex = currentIndex.value
                                        val totalItems = mediaItems.size
                                        
                                        // 異步刪除項目
                                        mediaViewModel.deleteMediaItem(mediaItem)
                                        
                                        // 如果刪除的是最後一個項目，更新索引到前一個項目
                                        if (currentItemIndex == totalItems - 1 && currentItemIndex > 0) {
                                            currentIndex.value = currentItemIndex - 1
                                        }
                                        // 其他情況下保持索引不變（除非出界，會在顯示時處理）
                                    }
                                    .setNegativeButton("取消", null)
                                    .show()
                            }, */
                            onItemLongClick = { mediaItem ->
                                // 長按導航到詳情頁進行編輯
                                navigateToDetail(mediaItem.id)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                // 新增按鈕 - 右上角
                IconButton(
                    onClick = navigateToAddEdit,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加新項目",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // 下方切換按鈕
                android.util.Log.d("HomeScreen", "切換按鈕條件檢查：媒體項目數量=${mediaItems.size}, 條件=${mediaItems.size > 0}")
                
                if (mediaItems.size > 0) { // 修改為只要有媒體項目就顯示按鈕
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 上一項按鈕
                        FloatingActionButton(
                            onClick = {
                                currentIndex.value = if (currentIndex.value > 0) 
                                    currentIndex.value - 1 
                                else 
                                    mediaItems.size - 1
                            },
                            modifier = Modifier.size(80.dp),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "上一項",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        
                        // 下一項按鈕
                        FloatingActionButton(
                            onClick = {
                                currentIndex.value = if (currentIndex.value < mediaItems.size - 1) 
                                    currentIndex.value + 1 
                                else 
                                    0
                            },
                            modifier = Modifier.size(80.dp),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = "下一項",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}