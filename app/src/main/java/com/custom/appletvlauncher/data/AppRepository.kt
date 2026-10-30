package com.custom.appletvlauncher.data

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.text.Collator
import android.graphics.Color as AndroidColor

/** Reads every launchable app on the TV and re-emits whenever a package is installed, updated or removed. */
class AppRepository(context: Context) {

    private val appContext = context.applicationContext
    private val pm: PackageManager = appContext.packageManager

    @OptIn(FlowPreview::class)
    fun observeApps(): Flow<List<AppInfo>> =
        packageChanges()
            .debounce(PACKAGE_CHANGE_DEBOUNCE_MS)
            .onStart { emit(Unit) }
            .map { loadApps() }
            .flowOn(Dispatchers.IO)

    @Suppress("DEPRECATION")
    private fun loadApps(): List<AppInfo> {
        // Keyed by package so an app with both a TV and a phone activity shows up once, preferring the TV one
        val entries = LinkedHashMap<String, Pair<ResolveInfo, Intent>>()

        fun collect(category: String) {
            val query = Intent(Intent.ACTION_MAIN).addCategory(category)
            for (info in pm.queryIntentActivities(query, 0)) {
                val activity = info.activityInfo
                if (activity.packageName == appContext.packageName) continue
                val launch = Intent(Intent.ACTION_MAIN)
                    .addCategory(category)
                    .setComponent(ComponentName(activity.packageName, activity.name))
                entries.putIfAbsent(activity.packageName, info to launch)
            }
        }
        collect(Intent.CATEGORY_LEANBACK_LAUNCHER)
        collect(Intent.CATEGORY_LAUNCHER)

        // TV settings usually only carry LEANBACK_SETTINGS, so add them explicitly like the tvOS Settings tile
        pm.resolveActivity(Intent(Settings.ACTION_SETTINGS), 0)
            ?.takeIf { it.activityInfo.packageName != "android" }
            ?.let { entries.putIfAbsent(it.activityInfo.packageName, it to Intent(Settings.ACTION_SETTINGS)) }

        val collator = Collator.getInstance()
        return entries.values
            .mapNotNull { (info, launch) -> runCatching { toAppInfo(info, launch) }.getOrNull() }
            .sortedWith(compareBy(collator) { it.label })
    }

    private fun toAppInfo(info: ResolveInfo, launchIntent: Intent): AppInfo {
        val activity = info.activityInfo
        val icon = info.loadIcon(pm).toBitmap(ICON_SIZE_PX, ICON_SIZE_PX)
        val banner = activity.loadBanner(pm)?.toBitmap(BANNER_WIDTH_PX, BANNER_HEIGHT_PX)
        val (accent, accentDark) = accentColors(banner ?: icon)
        return AppInfo(
            key = ComponentName(activity.packageName, activity.name).flattenToShortString(),
            label = info.loadLabel(pm).toString(),
            launchIntent = launchIntent,
            banner = banner?.asImageBitmap(),
            icon = icon.asImageBitmap(),
            accent = Color(accent),
            accentDark = Color(accentDark),
        )
    }

    private fun accentColors(bitmap: Bitmap): Pair<Int, Int> {
        val palette = Palette.from(bitmap).maximumColorCount(16).generate()
        val accent = palette.getVibrantColor(palette.getDominantColor(FALLBACK_ACCENT))
        val accentDark = palette.getDarkMutedColor(ColorUtils.blendARGB(accent, AndroidColor.BLACK, 0.6f))
        return accent to accentDark
    }

    private fun packageChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(appContext, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose { appContext.unregisterReceiver(receiver) }
    }

    private companion object {
        const val ICON_SIZE_PX = 192
        const val BANNER_WIDTH_PX = 320
        const val BANNER_HEIGHT_PX = 180
        const val PACKAGE_CHANGE_DEBOUNCE_MS = 400L
        const val FALLBACK_ACCENT = 0xFF2C2C2E.toInt()
    }
}
