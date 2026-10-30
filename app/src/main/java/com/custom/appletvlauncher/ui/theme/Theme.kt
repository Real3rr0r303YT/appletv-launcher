package com.custom.appletvlauncher.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

object TvOsColors {
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF1C1C1E)
    val Label = Color(0xFFFFFFFF)
    val SecondaryLabel = Color(0xFFAEAEB2)
}

object TvOsType {
    val ShelfOverline = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.6.sp,
        color = TvOsColors.SecondaryLabel,
    )
    val ShelfTitle = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, color = TvOsColors.Label)
    val TileLabel = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TvOsColors.Label)
    val Message = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, color = TvOsColors.SecondaryLabel)
}

/** tvOS app icons are landscape rounded rectangles. */
val TileShape = RoundedCornerShape(14.dp)

@Composable
fun TvOsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = TvOsColors.Background,
            onBackground = TvOsColors.Label,
            surface = TvOsColors.Surface,
            onSurface = TvOsColors.Label,
        ),
        content = content,
    )
}
