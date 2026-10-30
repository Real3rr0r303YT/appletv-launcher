package com.custom.appletvlauncher.data

import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

/** An app shown on the home screen. [key] is the flattened component name and stays stable across reloads. */
@Immutable
data class AppInfo(
    val key: String,
    val label: String,
    val launchIntent: Intent,
    /** Landscape TV banner (16:9), the closest match to a tvOS app icon. Null for phone-only apps. */
    val banner: ImageBitmap?,
    val icon: ImageBitmap,
    val accent: Color,
    val accentDark: Color,
)
