package com.custom.appletvlauncher

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.custom.appletvlauncher.ui.home.HomeRoute
import com.custom.appletvlauncher.ui.home.HomeViewModel
import com.custom.appletvlauncher.ui.theme.TvOsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()
    private var stoppedSinceResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvOsTheme {
                HomeRoute(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stoppedSinceResume = false
        viewModel.onResumed()
    }

    override fun onStop() {
        super.onStop()
        stoppedSinceResume = true
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // HOME pressed while the launcher was already on screen (not when returning from another app)
        if (!stoppedSinceResume && intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.scrollToTop()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val handled = super.dispatchKeyEvent(event)
        // Safety net against a "stuck" focus: an unhandled D-pad press lets the screen re-focus a tile
        if (!handled && event.action == KeyEvent.ACTION_DOWN && event.keyCode in NAVIGATION_KEYS) {
            viewModel.recoverFocus()
        }
        return handled
    }

    private companion object {
        val NAVIGATION_KEYS = setOf(
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
        )
    }
}
