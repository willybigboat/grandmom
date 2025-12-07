package com.example.grandmom.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.grandmom.data.model.MediaItem
import com.example.grandmom.ui.components.ConfirmationDialog
import com.example.grandmom.ui.components.LargeButton
import com.example.grandmom.ui.components.LoadingIndicator
import com.example.grandmom.util.AudioPlayerUtil
import kotlinx.coroutines.delay

/**
 * 顯示單個媒體項目的詳細信息屏幕。
 */
@Composable
fun DetailScreen(
    mediaItemId: String,
    onNavigateBack: () -> Unit,
    onEditItem: (String) -> Unit,
    onDeleteItem: (MediaItem) -> Unit,
    getMediaItem: suspend (String) -> MediaItem?
) {
    val context = LocalContext.current
    
    var mediaItem by remember { mutableStateOf<MediaItem?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // 加載媒體項目
    LaunchedEffect(mediaItemId) {
        loading = true
        delay(300) // 給UI更新一個機會
        mediaItem = getMediaItem(mediaItemId)
        loading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = mediaItem?.title?.takeIf { it.isNotEmpty() } ?: "圖片和音頻",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        mediaItem?.let { onEditItem(it.id) } 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "編輯"
                        )
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "刪除"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            mediaItem?.let { item ->
                FloatingActionButton(
                    onClick = { AudioPlayerUtil.playAudio(context, item.audioUrl) },
                    modifier = Modifier.size(80.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "播放音頻",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (loading) {
                // 加載中
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            } else if (mediaItem == null) {
                // 項目不存在
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "找不到項目",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LargeButton(
                        text = "返回",
                        onClick = onNavigateBack
                    )
                }
            } else {
                // 顯示詳情
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 圖片
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(mediaItem!!.imageUrl)
                                .build()
                        ),
                        contentDescription = "圖片",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(640.dp)
                            .padding(bottom = 16.dp)
                    )
                    
                    // 標題（如果有）
                    if (!mediaItem!!.title.isNullOrEmpty()) {
                        Text(
                            text = mediaItem!!.title,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }
                    
                    // 播放按鈕說明
                    Text(
                        text = "點擊下方按鈕播放音頻",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp, bottom = 80.dp)
                    )
                }
            }
            
            // 刪除確認對話框
            if (showDeleteConfirmation) {
                mediaItem?.let { item ->
                    ConfirmationDialog(
                        title = "確認刪除",
                        message = "確定要刪除這個項目嗎？",
                        onConfirm = {
                            // 只需要調用 onDeleteItem，它會在刪除完成後自動導航回去
                            onDeleteItem(item)
                            // 移除重複的導航調用，防止多次導航造成問題
                        },
                        onDismiss = { showDeleteConfirmation = false }
                    )
                }
            }
        }
    }
}