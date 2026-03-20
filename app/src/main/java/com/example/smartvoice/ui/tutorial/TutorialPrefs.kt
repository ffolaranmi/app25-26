package com.example.smartvoice.ui.tutorial

import android.content.Context

object TutorialPrefs {
    private const val PREF_NAME = "tutorial_prefs"
    private const val KEY_PREFIX = "has_seen_tutorial_"

    private fun keyForUser(userId: Long): String = "$KEY_PREFIX$userId"

    fun hasSeen(context: Context, userId: Long): Boolean {
        if (userId == -1L) return false
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(keyForUser(userId), false)
    }

    fun markSeen(context: Context, userId: Long) {
        if (userId == -1L) return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(keyForUser(userId), true).apply()
    }

    fun clearSeen(context: Context, userId: Long) {
        if (userId == -1L) return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(keyForUser(userId)).apply()
    }
}