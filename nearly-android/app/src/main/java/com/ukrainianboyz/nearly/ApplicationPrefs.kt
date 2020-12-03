package com.ukrainianboyz.nearly

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

//A singleton
object ApplicationPrefs {

    const val USER_KEY = "user"
    private const val USER_PREFERENCES = "user_pref_data"
    var isInForeground = false
    lateinit var encryptedPrefs: SharedPreferences
        private set


    fun initEncryptedPrefs(context: Context) {
        encryptedPrefs =
            EncryptedSharedPreferences.create(
                context,
                USER_PREFERENCES,
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
    }

}