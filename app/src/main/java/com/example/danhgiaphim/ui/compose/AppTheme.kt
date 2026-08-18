package com.example.danhgiaphim.ui.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AppPrimary = Color(0xFFD94F45)
val AppPrimaryDark = Color(0xFFAF352F)
val AppBackground = Color(0xFFFFF7F5)
val AppSurface = Color.White
val AppText = Color(0xFF261C1C)
val AppTextMuted = Color(0xFF7D6B6B)
val AppBorder = Color(0xFFF1D6D2)

@Composable
fun DanhGiaPhimTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AppPrimary,
            onPrimary = Color.White,
            secondary = AppPrimaryDark,
            background = AppBackground,
            onBackground = AppText,
            surface = AppSurface,
            onSurface = AppText,
            outline = AppBorder
        ),
        shapes = MaterialTheme.shapes.copy(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(12.dp)
        ),
        content = content
    )
}
