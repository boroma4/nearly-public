package com.ukrainianboyz.nearly.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

import com.ukrainianboyz.nearly.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}
