package com.custom.appletvlauncher.data

import android.content.Context
import androidx.core.content.edit

/** Persists the most recently launched app keys, newest first. */
class RecentAppsStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("launcher", Context.MODE_PRIVATE)

    fun load(): List<String> =
        prefs.getString(KEY_RECENT, null)
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            .orEmpty()

    fun record(key: String) {
        val updated = (listOf(key) + load().filterNot { it == key }).take(MAX_ENTRIES)
        prefs.edit { putString(KEY_RECENT, updated.joinToString(SEPARATOR)) }
    }

    private companion object {
        const val KEY_RECENT = "recent_apps"
        const val SEPARATOR = "\n"
        const val MAX_ENTRIES = 10
    }
}
