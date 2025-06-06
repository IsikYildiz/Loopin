package com.example.loopin

import android.content.SharedPreferences
import android.content.Context

object PreferenceManager {
    private const val PREF_NAME = "user_session"
    private const val KEY_USER_ID = "user_id"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Int? = prefs.getInt(KEY_USER_ID, -1)

    fun clear() {
        prefs.edit().clear().apply()
    }
}