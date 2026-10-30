package com.custom.appletvlauncher.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import com.custom.appletvlauncher.R
import com.custom.appletvlauncher.data.AppInfo
import com.custom.appletvlauncher.ui.components.AppTile
import com.custom.appletvlauncher.ui.components.TopShelf
import com.custom.appletvlauncher.ui.focus.rememberGridFocusController
import com.custom.appletvlauncher.ui.theme.TvOsColors
import com.custom.appletvlauncher.ui.theme.TvOsType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val SHELF_HEIGHT_FRACTION = 0.58f
private val ShelfOverlap = 40.dp
private const val SHELF_SPACER_KEY = "top-shelf-spacer"

@Composable
fun HomeRoute(viewModel: HomeViewModel) {
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val recentKeys by viewModel.recentKeys.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // A launcher must never close on BACK; it returns to the first tile instead
    BackHandler { viewModel.scrollToTop() }

    HomeScreen(
        apps = apps,
        recentKeys = recentKeys,
        events = viewModel.events,
        onLaunch = { viewModel.launch(context, it) },
    )
}

@Composable
fun HomeScreen(
    apps: List<AppInfo>?,
    recentKeys: List<String>,
    events: Flow<HomeEvent>,
    onLaunch: (AppInfo) -> Unit,
) {
    val gridState = rememberLazyGridState()
    val focus = rememberGridFocusController(gridState, columns = GRID_COLUMNS, headerItems = 1)
    val scope = rememberCoroutineScope()
    var focusedKey by remember { mutableStateOf<String?>(null) }
    var focusedIndex by remember { mutableIntStateOf(0) }
    var shelfKey by remember { mutableStateOf<String?>(null) }
    val currentApps by rememberUpdatedState(apps)

    // Initial focus, and re-focus when the list changes under the focused tile (app removed or reordered)
    LaunchedEffect(apps) {
        if (apps.isNullOrEmpty()) return@LaunchedEffect
        val index = apps.indexOfFirst { it.key == focusedKey }
        if (focus.hasFocus && index == focusedIndex) return@LaunchedEffect
        val target = if (index >= 0) index else focusedIndex.coerceIn(0, apps.lastIndex)
        focus.focusItem(target, apps[target].key)
    }

    LaunchedEffect(events) {
        events.collect { event ->
            val list = currentApps
            if (list.isNullOrEmpty()) return@collect
            when (event) {
                HomeEvent.ScrollToTop -> focus.focusItem(0, list[0].key)
                HomeEvent.RecoverFocus -> if (!focus.hasFocus) {
                    val target = focusedIndex.coerceIn(0, list.lastIndex)
                    focus.focusItem(target, list[target].key)
                }
            }
        }
    }

    val shelfApp = apps?.let { list -> list.firstOrNull { it.key == shelfKey } ?: list.firstOrNull() }

    BoxWithConstraints(Modifier.fillMaxSize().background(TvOsColors.Background)) {
        val shelfHeight = maxHeight * SHELF_HEIGHT_FRACTION
        val shelfHeightPx = with(LocalDensity.current) { shelfHeight.toPx() }

        // Drawn behind the grid and moved with its scroll (parallax + fade), so it can stay full-bleed
        TopShelf(
            app = shelfApp,
            isRecent = shelfApp != null && shelfApp.key in recentKeys,
            modifier = Modifier
                .fillMaxWidth()
                .height(shelfHeight)
                .graphicsLayer {
                    val scrolled = if (gridState.firstVisibleItemIndex == 0) {
                        gridState.firstVisibleItemScrollOffset.toFloat()
                    } else {
                        shelfHeightPx
                    }
                    translationY = -scrolled * 0.5f
                    alpha = (1f - scrolled / shelfHeightPx).coerceIn(0f, 1f)
                },
        )

        when {
            apps == null -> Unit
            apps.isEmpty() -> Text(
                text = stringResource(R.string.no_apps),
                style = TvOsType.Message,
                modifier = Modifier.align(Alignment.Center),
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_COLUMNS),
                state = gridState,
                contentPadding = PaddingValues(start = 56.dp, end = 56.dp, bottom = 72.dp),
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .onFocusChanged(focus::onFocusChanged),
            ) {
                item(key = SHELF_SPACER_KEY, span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(shelfHeight - ShelfOverlap))
                }
                itemsIndexed(apps, key = { _, app -> app.key }) { index, app ->
                    AppTile(
                        app = app,
                        onClick = { onLaunch(app) },
                        onFocused = {
                            focusedKey = app.key
                            focusedIndex = index
                            if (index < GRID_COLUMNS) {
                                // First row drives the Top Shelf and always shows it completely
                                shelfKey = app.key
                                if (gridState.firstVisibleItemIndex != 0 || gridState.firstVisibleItemScrollOffset != 0) {
                                    scope.launch { gridState.animateScrollToItem(0) }
                                }
                            }
                        },
                        modifier = focus.anchor(app.key),
                    )
                }
            }
        }
    }
}
