package ru.netfantazii.handy.core.preferences

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ru.netfantazii.handy.MainActivity
import ru.netfantazii.handy.R

const val FIRST_LAUNCH_KEY = "first_launch"

class AppSettings : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = "AppSettings"
    private lateinit var sp: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).checkMenuItem(R.id.navigation_app_settings)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref, rootKey)
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun setDivider(divider: Drawable?) {
        val newDivider = context!!.getDrawable(android.R.drawable.divider_horizontal_textfield)
        if (newDivider != null) {
            super.setDivider(newDivider)
        } else {
            super.setDivider(divider)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val preference: Preference? = findPreference(key)
        preference?.let { if (key == context!!.getString(R.string.theme_pref_key)) reloadActivity() }
    }

    private fun reloadActivity() {
        val intent = activity!!.intent
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}