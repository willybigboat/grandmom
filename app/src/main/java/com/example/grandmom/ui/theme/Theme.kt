package com.example.grandmom.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 為老年人設計的深色主題配色方案，高對比度和舒適的色彩
private val DarkColorScheme = darkColorScheme(
    primary = SeniorBlue80,
    secondary = SeniorOrange80,
    tertiary = SeniorGreen80,
    background = DarkBackground,
    surface = DarkBackground,
    onPrimary = DarkText,
    onSecondary = DarkText,
    onTertiary = DarkText,
    onBackground = LightText,
    onSurface = LightText
)

// 為老年人設計的淺色主題配色方案，高對比度和舒適的色彩
private val LightColorScheme = lightColorScheme(
    primary = SeniorBlue40,
    secondary = SeniorOrange40,
    tertiary = SeniorGreen40,
    background = LightBackground,
    surface = LightBackground,
    onPrimary = LightText,
    onSecondary = LightText,
    onTertiary = LightText,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun GrandmomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 對於老人用戶，我們不使用動態顏色，因為固定的高對比度顏色方案更適合
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 更新狀態欄顏色以匹配主題
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}