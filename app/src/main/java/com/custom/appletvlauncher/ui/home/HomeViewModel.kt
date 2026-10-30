package com.custom.appletvlauncher.ui.home

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.custom.appletvlauncher.R
import com.custom.appletvlauncher.data.AppInfo
import com.custom.appletvlauncher.data.AppRepository
import com.custom.appletvlauncher.data.RecentAppsStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Number of tiles per grid row; the first row is the "Top Shelf row" filled with recently used apps. */
const val GRID_COLUMNS = 5

sealed interface HomeEvent {
    /** BACK, or HOME while the launcher is already visible: return to the first tile. */
    data object ScrollToTop : HomeEvent

    /** A D-pad key was not handled by anything, so focus was lost (e.g. the focused app was uninstalled). */
    data object RecoverFocus : HomeEvent
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val recents = RecentAppsStore(application)

    private val _recentKeys = MutableStateFlow(recents.load())
    val recentKeys: StateFlow<List<String>> = _recentKeys.asStateFlow()

    /** Null while the first load is running. */
    val apps: StateFlow<List<AppInfo>?> =
        combine(repository.observeApps(), _recentKeys) { all, recent -> orderForHome(all, recent) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    fun scrollToTop() {
        _events.tryEmit(HomeEvent.ScrollToTop)
    }

    fun recoverFocus() {
        _events.tryEmit(HomeEvent.RecoverFocus)
    }

    /** Apply the recent-apps order only once the launcher is visible again, so the grid doesn't reshuffle mid-launch. */
    fun onResumed() {
        _recentKeys.value = recents.load()
    }

    fun launch(context: Context, app: AppInfo) {
        val intent = Intent(app.launchIntent)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        try {
            context.startActivity(intent)
            recents.record(app.key)
        } catch (e: ActivityNotFoundException) {
            showLaunchError(context, app)
        } catch (e: SecurityException) {
            showLaunchError(context, app)
        }
    }

    private fun showLaunchError(context: Context, app: AppInfo) {
        Toast.makeText(context, context.getString(R.string.launch_failed, app.label), Toast.LENGTH_SHORT).show()
    }
}

/** Recently used apps fill the first row (like the tvOS Top Shelf row), everything else follows alphabetically. */
internal fun orderForHome(all: List<AppInfo>, recentKeys: List<String>): List<AppInfo> {
    val byKey = all.associateBy { it.key }
    val top = recentKeys.asSequence().mapNotNull { byKey[it] }.take(GRID_COLUMNS).toList()
    val topKeys = top.mapTo(HashSet()) { it.key }
    return top + all.filterNot { it.key in topKeys }
}
