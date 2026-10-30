package com.custom.appletvlauncher.ui.components

import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.custom.appletvlauncher.R
import com.custom.appletvlauncher.data.AppInfo
import com.custom.appletvlauncher.ui.theme.TileShape
import com.custom.appletvlauncher.ui.theme.TvOsColors
import com.custom.appletvlauncher.ui.theme.TvOsType

/** Immersive hero area for the app focused in the first row, cross-fading whenever that app changes. */
@Composable
fun TopShelf(app: AppInfo?, isRecent: Boolean, modifier: Modifier = Modifier) {
    Crossfade(
        targetState = app,
        animationSpec = tween(650),
        label = "topShelf",
        modifier = modifier,
    ) { shown ->
        Box(Modifier.fillMaxSize()) {
            ShelfBackdrop(shown)
            if (shown != null) {
                ShelfDetails(
                    app = shown,
                    isRecent = isRecent,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 56.dp, bottom = 72.dp),
                )
            }
        }
    }
}

@Composable
private fun ShelfBackdrop(app: AppInfo?) {
    val accent = app?.accent ?: TvOsColors.Surface
    val accentDark = app?.accentDark ?: TvOsColors.Background
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(accent, accentDark, TvOsColors.Background),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            ),
    )
    app?.banner?.let { banner ->
        // Blur needs Android 12; older TVs get a fainter, unblurred banner over the gradient instead
        val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        Image(
            bitmap = banner,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = if (canBlur) 0.85f else 0.35f,
            modifier = Modifier.fillMaxSize().blur(40.dp),
        )
    }
    // Readability scrims and the fade into the black grid background
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.horizontalGradient(0f to Color.Black.copy(alpha = 0.75f), 0.65f to Color.Transparent)),
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.6f to Color.Black.copy(alpha = 0.25f),
                    1f to TvOsColors.Background,
                ),
            ),
    )
}

@Composable
private fun ShelfDetails(app: AppInfo, isRecent: Boolean, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        AppArtwork(
            app = app,
            modifier = Modifier
                .width(280.dp)
                .aspectRatio(16f / 9f)
                .shadow(24.dp, TileShape, clip = true),
        )
        Spacer(Modifier.width(32.dp))
        Column {
            Text(
                text = stringResource(if (isRecent) R.string.recently_used else R.string.app_category).uppercase(),
                style = TvOsType.ShelfOverline,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = app.label,
                style = TvOsType.ShelfTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
