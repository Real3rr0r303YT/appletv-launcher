package com.custom.appletvlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Text
import com.custom.appletvlauncher.data.AppInfo
import com.custom.appletvlauncher.ui.focus.FocusSheen
import com.custom.appletvlauncher.ui.focus.tvOsFocusEffect
import com.custom.appletvlauncher.ui.theme.TileShape
import com.custom.appletvlauncher.ui.theme.TvOsType

/** One app icon in the grid. OK / DPAD_CENTER launches it (handled by [clickable] for key input). */
@Composable
fun AppTile(
    app: AppInfo,
    onClick: () -> Unit,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val labelAlpha by animateFloatAsState(if (focused) 1f else 0f, tween(200), label = "labelAlpha")

    // zIndex keeps the enlarged tile and its shadow above its neighbours
    Column(
        modifier = modifier.zIndex(if (focused) 1f else 0f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .onFocusChanged { state ->
                    focused = state.isFocused
                    if (state.isFocused) onFocused()
                }
                .tvOsFocusEffect(focused, TileShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        ) {
            AppArtwork(app, Modifier.fillMaxSize())
            FocusSheen(focused, TileShape, Modifier.matchParentSize())
        }
        // Like tvOS, the name only appears under the focused icon; the space stays reserved so rows don't jump
        Text(
            text = app.label,
            style = TvOsType.TileLabel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 16.dp)
                .graphicsLayer { alpha = labelAlpha },
        )
    }
}
