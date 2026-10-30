package com.custom.appletvlauncher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import com.custom.appletvlauncher.data.AppInfo

/** The TV banner when the app has one; otherwise its icon centred on a gradient of its own colours. */
@Composable
fun AppArtwork(app: AppInfo, modifier: Modifier = Modifier) {
    val banner = app.banner
    if (banner != null) {
        Image(
            bitmap = banner,
            contentDescription = app.label,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.background(Brush.linearGradient(listOf(app.accent, app.accentDark))),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = app.icon,
                contentDescription = app.label,
                modifier = Modifier.fillMaxHeight(0.55f).aspectRatio(1f),
            )
        }
    }
}
