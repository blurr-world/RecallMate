package com.madinaappstudio.recallmate.core.utils

import android.content.Context
import androidx.core.content.edit

class PrefManager(context: Context) {

    companion object {
        private const val KEY_PREF_NAME = "app_pref"
        private const val KEY_LOGIN = "is_login"
        private const val KEY_FIRST_STARTUP = "is_first_startup"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_PROFILE_SELECTION = "profile_selection"
    }

    private val pref = context.getSharedPreferences(KEY_PREF_NAME, Context.MODE_PRIVATE)

    fun setUserId(id: String) {
        pref.edit {
            putString(KEY_USER_ID, id)
        }
    }

    fun getUserId() : String? {
        return pref.getString(KEY_USER_ID,null)
    }

    fun isLogin(): Boolean = pref.getBoolean(KEY_LOGIN, false)

    fun setLogin(flag: Boolean) {
        pref.edit {
            putBoolean(KEY_LOGIN, flag)
        }
    }

    fun isFirstStartup(): Boolean = pref.getBoolean(KEY_FIRST_STARTUP, true)

    fun setFirstStartup(flag: Boolean) {
        pref.edit{
            putBoolean(KEY_FIRST_STARTUP, flag)
        }
    }

    fun setProfileSelection(id: Int) {
        pref.edit {
            putInt(KEY_PROFILE_SELECTION, id)
        }
    }

    fun getProfileSelection(): Int {
        return pref.getInt(KEY_PROFILE_SELECTION, 103)
    }

    fun clearAllPrefs() {
        pref.edit {
            putBoolean(KEY_FIRST_STARTUP, true)
            putBoolean(KEY_LOGIN, false)
            putString(KEY_USER_ID, null)
        }
    }
}
