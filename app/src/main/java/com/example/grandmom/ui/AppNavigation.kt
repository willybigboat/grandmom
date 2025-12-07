package com.example.grandmom.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.grandmom.ui.screens.AddEditScreen
import com.example.grandmom.ui.screens.DetailScreen
import com.example.grandmom.ui.screens.HomeScreen
import com.example.grandmom.ui.viewmodel.MediaViewModel
import kotlinx.coroutines.launch

/**
 * 應用程式的主要導航組件。
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val mediaViewModel: MediaViewModel = viewModel()
    val scope = rememberCoroutineScope()
    
    // 定義導航路線，並實際使用這些常量
    val homeRoute = remember { "home" }
    val addEditRoute = remember { "add_edit?itemId={itemId}" }
    val detailRoute = remember { "detail/{itemId}" }
    
    NavHost(navController = navController, startDestination = homeRoute) {
        // 主屏幕
        composable(homeRoute) {
            HomeScreen(
                mediaViewModel = mediaViewModel,
                navigateToAddEdit = {
                    navController.navigate(addEditRoute.replace("{itemId}", ""))
                },
                navigateToDetail = { itemId ->
                    // 確保 itemId 不為空，且使用定義好的路徑常數
                    if (itemId.isNotEmpty()) {
                        navController.navigate(detailRoute.replace("{itemId}", itemId))
                    }
                }
            )
        }
        
        // 添加/編輯屏幕
        composable(
            route = addEditRoute,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            
            AddEditScreen(
                onSave = { mediaItem ->
                    scope.launch {
                        mediaViewModel.insertMediaItem(mediaItem)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                mediaItemToEdit = if (itemId.isNotEmpty()) {
                    // 假設獲取項目是同步操作，實際上應該處理異步情況
                    scope.launch {
                        mediaViewModel.getMediaItemById(itemId)
                    }
                    null // 臨時返回null，實際應該在UI中處理加載狀態
                } else null
            )
        }
        
        // 詳情屏幕
        composable(
            route = detailRoute,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            
            DetailScreen(
                mediaItemId = itemId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditItem = { id ->
                    navController.navigate(addEditRoute.replace("{itemId}", id))
                },
                onDeleteItem = { mediaItem ->
                    scope.launch {
                        mediaViewModel.deleteMediaItem(mediaItem)
                        // 確保刪除操作完成後再導航
                        navController.popBackStack()
                    }
                },
                getMediaItem = { id ->
                    mediaViewModel.getMediaItemById(id)
                }
            )
        }
    }
}
