package com.example.smartvoice.data

import android.content.Context

object SessionPrefs {
    private const val PREFS_NAME = "smartvoice_session"
    private const val KEY_USERNAME = "logged_in_username"

    fun setLoggedInUsername(context: Context, username: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getLoggedInUsername(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USERNAME, null)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_USERNAME)
            .apply()
    }
}