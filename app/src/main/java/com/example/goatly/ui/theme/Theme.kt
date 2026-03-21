package com.example.goatly.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Background = Color(0xFFF3F2EF)
    val Surface = Color.White
    val PrimaryYellow = Color(0xFFF2B705)
    val DarkText = Color(0xFF1F2328)
    val GreyText = Color(0xFF5E6C84)
    val Border = Color(0xFFE6E6E6)
    val Success = Color(0xFF1A7F37)
    val Danger = Color(0xFFD1242F)
}

private val GoatlyColorScheme = lightColorScheme(
    primary = AppColors.PrimaryYellow,
    background = AppColors.Background,
    surface = AppColors.Surface,
    onPrimary = AppColors.DarkText,
    onBackground = AppColors.DarkText,
    onSurface = AppColors.DarkText
)

@Composable
fun GoatlyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GoatlyColorScheme,
        content = content
    )
}
