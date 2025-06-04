package com.codecrew.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object UserPreferences {

    private const val PREFS_NAME = "user_app_prefs"
    private const val KEY_REGISTERED_EMAIL = "registered_email"
    private const val KEY_HAS_COMPLETED_SIGN_UP = "has_completed_sign_up"


    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveRegisteredEmail(context: Context, email: String) {
        getPreferences(context).edit { putString(KEY_REGISTERED_EMAIL, email) }
    }

    fun getRegisteredEmail(context: Context): String? {
        return getPreferences(context).getString(KEY_REGISTERED_EMAIL, null)
    }

    fun setHasCompletedSignUp(context: Context, hasCompleted: Boolean) {
        getPreferences(context).edit { putBoolean(KEY_HAS_COMPLETED_SIGN_UP, hasCompleted) }
    }

    fun hasCompletedSignUp(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_HAS_COMPLETED_SIGN_UP, false)
    }

    fun clearSignUpData(context: Context) {
        getPreferences(context).edit {
            remove(KEY_REGISTERED_EMAIL)
                .remove(KEY_HAS_COMPLETED_SIGN_UP)
        }
    }
}