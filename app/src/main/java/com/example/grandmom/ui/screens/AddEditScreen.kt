package com.example.grandmom.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import com.yalantis.ucrop.UCrop
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.grandmom.data.model.MediaItem
import com.example.grandmom.ui.components.LargeButton
import com.example.grandmom.ui.components.LoadingIndicator
import com.example.grandmom.util.FileUtil
import com.example.grandmom.util.ImageCropperUtil
import com.example.grandmom.util.ImageResizeUtil
import com.example.grandmom.util.MediaStoreUtil
import kotlinx.coroutines.launch

/**
 * 添加或編輯媒體項目的屏幕。
 */
@Composable
fun AddEditScreen(
    onSave: (MediaItem) -> Unit,
    onNavigateBack: () -> Unit,
    mediaItemToEdit: MediaItem? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
var title by remember { mutableStateOf(mediaItemToEdit?.title ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(mediaItemToEdit?.imageUrl?.let { Uri.parse(it) }) }
    var audioUri by remember { mutableStateOf<Uri?>(mediaItemToEdit?.audioUrl?.let { Uri.parse(it) }) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var cropOutputUri by remember { mutableStateOf<Uri?>(null) }
    
    // 選擇圖片時顯示加載狀態
    var isLoading by remember { mutableStateOf(false) }
    
    // 初始清理臨時文件
    MediaStoreUtil.cleanupTempFiles(context)
    
    // UCrop裁剪結果處理
    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == -1) { // RESULT_OK
            // UCrop成功返回
            scope.launch {
                try {
                    // 從裁剪結果獲取URI
                    val resultUri = cropOutputUri ?: result.data?.let { intent ->
                        UCrop.getOutput(intent)
                    }

                    // 如果獲取到裁剪後的URI，調整大小並儲存
                    if (resultUri != null) {
                        val finalUri = ImageResizeUtil.processAndSaveCroppedImage(context, resultUri)
                        if (finalUri != null) {
                            imageUri = finalUri
                        } else {
                            snackbarHostState.showSnackbar("處理裁剪後的圖片失敗")
                        }
                    } else {
                        snackbarHostState.showSnackbar("無法獲取裁剪後的圖片")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("處理裁剪後的圖片時發生錯誤: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            // UCrop返回錯誤
            isLoading = false
            result.data?.let { intent ->
                val error = UCrop.getError(intent)
                scope.launch {
                    snackbarHostState.showSnackbar("裁剪圖片時發生錯誤: ${error?.message}")
                }
            }
        } else {
            // 用戶取消操作
            isLoading = false
        }
    }
    
    // 選擇圖片後啟動裁剪
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            isLoading = true
            selectedImageUri = uri
            
            // 啟動裁剪流程
            scope.launch {
                try {
                    // 啟動UCrop裁剪
                    cropOutputUri = ImageCropperUtil.startCrop(
                        context,
                        uri,
                        cropImageLauncher
                    )
                    
                    // 如果無法啟動裁剪，直接調整大小並保存原始圖片
                    if (cropOutputUri == null) {
                        // 調整圖片大小並保存到應用內部儲存空間
                        val savedUri = ImageResizeUtil.resizeAndSaveImage(
                            context,
                            uri
                        )
                        
                        // 更新UI
                        isLoading = false
                        if (savedUri != null) {
                            imageUri = savedUri
                            scope.launch {
                                snackbarHostState.showSnackbar("已直接調整圖片大小並保存")
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("無法處理圖片，請再試一次")
                            }
                        }
                    }
                } catch (e: Exception) {
                    // 捕獲任何異常
                    isLoading = false
                    scope.launch {
                        snackbarHostState.showSnackbar("處理圖片時發生錯誤: ${e.message}")
                    }
                }
            }
        }
    }    // 選擇音頻
    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { audioUri = it }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (mediaItemToEdit == null) "添加新項目" else "編輯項目",
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
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // 顯示加載指示器
        if (isLoading) {
            LoadingIndicator(message = "正在處理圖片...")
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 標題輸入
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("標題 (可選)") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 圖片選擇
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { imagePicker.launch("image/*") }
            ) {
                if (imageUri != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(imageUri)
                                    .build()
                            ),
                            contentDescription = "已裁剪的圖片",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(640.dp)
                        )
                        Text(
                            text = "點擊重新選擇並裁剪圖片",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "選擇圖片",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "點擊選擇並裁剪圖片",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 音頻選擇
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { audioPicker.launch("audio/*") }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "選擇音頻",
                        modifier = Modifier.size(60.dp),
                        tint = if (audioUri != null) 
                               MaterialTheme.colorScheme.secondary 
                               else Color.Gray
                    )
                    Text(
                        text = if (audioUri != null) "已選擇音頻" else "點擊選擇音頻",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 保存按鈕
            LargeButton(
                text = "保存",
                onClick = {
                    if (imageUri == null || audioUri == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("請選擇圖片和音頻")
                        }
                        return@LargeButton
                    }
                    
                    // 保存媒體文件
                    val savedImagePath = saveMediaFile(context, imageUri!!, isImage = true)
                    val savedAudioPath = saveMediaFile(context, audioUri!!, isImage = false)
                    
                    if (savedImagePath != null && savedAudioPath != null) {
                        val mediaItem = MediaItem(
                            // 如果是編輯現有項目，使用原 ID；如果是新增項目，生成新的唯一 ID
                            id = mediaItemToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                            title = title.trim(),
                            imageUrl = savedImagePath,
                            audioUrl = savedAudioPath,
                            createDate = mediaItemToEdit?.createDate ?: System.currentTimeMillis()
                        )
                        // 添加日誌以檢查正在保存的項目
                        android.util.Log.d("AddEditScreen", "正在保存媒體項目，ID: ${mediaItem.id}, 是新項目: ${mediaItemToEdit == null}")
                        onSave(mediaItem)
                        onNavigateBack()
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("保存媒體文件失敗")
                        }
                    }
                },
                enabled = imageUri != null && audioUri != null
            )
        }
    }
}

/**
 * 保存媒體文件到內部存儲。
 */
private fun saveMediaFile(context: Context, uri: Uri, isImage: Boolean): String? {
    // 檢查URI是否已經是應用內部存儲的路徑
    val uriString = uri.toString()
    if (uriString.startsWith("file:///data/user/") || 
        uriString.startsWith("file:///storage/emulated/0/Android/data/${context.packageName}") ||
        uriString.startsWith("file://" + context.filesDir.absolutePath)) {
        return uriString
    }
    
    // 如果不是內部存儲路徑，則進行保存
    return if (isImage) {
        FileUtil.saveImageToInternalStorage(context, uri)
    } else {
        FileUtil.saveAudioToInternalStorage(context, uri)
    }
}