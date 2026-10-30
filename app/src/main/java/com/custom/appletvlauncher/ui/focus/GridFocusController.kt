package com.custom.appletvlauncher.ui.focus

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester

/**
 * Moves focus to any tile of a lazy grid, even one that is currently scrolled off screen and therefore
 * not composed. Only the target tile carries the [FocusRequester], so there is never a requester
 * attached to a disposed item.
 */
@Stable
class GridFocusController(
    private val gridState: LazyGridState,
    private val columns: Int,
    private val headerItems: Int,
) {
    private val requester = FocusRequester()
    private var anchorKey by mutableStateOf<Any?>(null)

    /** True while any tile inside the grid holds focus. */
    var hasFocus by mutableStateOf(false)
        private set

    fun onFocusChanged(state: FocusState) {
        hasFocus = state.hasFocus
    }

    /** Attach to every tile; it only resolves to a [focusRequester] for the current target. */
    fun anchor(key: Any): Modifier =
        if (key == anchorKey) Modifier.focusRequester(requester) else Modifier

    suspend fun focusItem(index: Int, key: Any) {
        anchorKey = key
        // First row: scroll fully up so the Top Shelf is visible as well
        gridState.scrollToItem(if (index < columns) 0 else index + headerItems)
        // The target needs a frame to compose and attach the requester
        repeat(MAX_ATTEMPTS) {
            withFrameNanos { }
            if (runCatching { requester.requestFocus() }.isSuccess) return
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}

@Composable
fun rememberGridFocusController(gridState: LazyGridState, columns: Int, headerItems: Int): GridFocusController =
    remember(gridState, columns, headerItems) { GridFocusController(gridState, columns, headerItems) }
