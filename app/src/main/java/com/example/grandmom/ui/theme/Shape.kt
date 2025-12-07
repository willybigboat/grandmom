package com.example.grandmom.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// 為老年人設計的形狀，更大更圓的邊角，使界面更友好
val Shapes = Shapes(
    // 小組件的圓角，如卡片、按鈕等
    small = RoundedCornerShape(12.dp),
    
    // 中等大小組件的圓角
    medium = RoundedCornerShape(16.dp),
    
    // 大組件的圓角，如對話框、底部表單等
    large = RoundedCornerShape(24.dp)
)