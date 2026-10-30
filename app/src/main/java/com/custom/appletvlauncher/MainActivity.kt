package com.custom.appletvlauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.*
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = Color(0xFF0D0D0F))) {
                AppleTvDashboard()
            }
        }
    }
}

data class AppModel(val label: String, val packageName: String, val icon: Any?)

@Composable
fun AppleTvDashboard() {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<AppModel>>(emptyList()) }
    var focusedApp by remember { mutableStateOf<AppModel?>(null) }

    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER) }
        val resolveInfos: List<ResolveInfo> = pm.queryIntentActivities(mainIntent, 0)
        installedApps = resolveInfos.map { AppModel(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm)) }
        if (installedApps.isNotEmpty()) focusedApp = installedApps.first()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0C))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(0.45f).padding(24.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF16161A)), contentAlignment = Alignment.Center) {
                Crossfade(targetState = focusedApp, label = "TopShelf") { app ->
                    if (app != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(model = app.icon, contentDescription = app.label, modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Fit)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = app.label, fontSize = 24.sp, color = Color.White)
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().weight(0.55f).padding(horizontal = 24.dp)) {
                TvLazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
                    items(installedApps) { app ->
                        AppCard(app = app, onFocus = { focusedApp = app }, onClick = {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            launchIntent?.let { context.startActivity(it) }
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppCard(app: AppModel, onFocus: () -> Unit, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(140.dp).shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = CardDefaults.shape(RoundedCornerShape(24.dp)),
        scale = CardDefaults.scale(focusedScale = 1.12f),
        colors = CardDefaults.colors(containerColor = Color(0xFF1C1C1E), focusedContainerColor = Color.White),
        onFocus = { onFocus() }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AsyncImage(model = app.icon, contentDescription = app.label, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)))
        }
    }
}
