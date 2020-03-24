package ru.netfantazii.handy.core.welcome

import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.OverlayActions
import ru.netfantazii.handy.core.main.MainActivity
import ru.netfantazii.handy.core.preferences.FIRST_LAUNCH_KEY
import ru.netfantazii.handy.extensions.showLongToast

class WelcomeFragment : Fragment() {
    private val TAG = "WelcomeFragment"
    private lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        overrideBackButton()
        (activity as MainActivity).lockDrawerClosed()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.welcome_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val beginButton = view.findViewById<Button>(R.id.begin_button)
        val navController = NavHostFragment.findNavController(this)
        val privacyText1 = view.findViewById<TextView>(R.id.welcome_privacy_policy_msg)
        val privacyText2 = view.findViewById<TextView>(R.id.welcome_privacy_policy_msg2)
        privacyText1.movementMethod = LinkMovementMethod.getInstance()
        privacyText2.movementMethod = LinkMovementMethod.getInstance()
        beginButton.setOnClickListener {
            navController.popBackStack()
            setPrefToDefault()
            setFirstLaunchToFalse()
            (activity as MainActivity).unlockDrawer()
        }
    }

    private fun setPrefToDefault() {
        val themeKey = getString(R.string.theme_pref_key)
        val themeDefaultValue = getString(R.string.theme_violet_value)
        val sortingKey = getString(R.string.sorting_pref_key)
        val sortingDefaultValue = getString(R.string.sorting_newest_first_value)
        sp.edit().putString(themeKey, themeDefaultValue)
            .putString(sortingKey, sortingDefaultValue)
            .apply()
    }

    private fun setFirstLaunchToFalse() {
        sp.edit().putBoolean(FIRST_LAUNCH_KEY, false).apply()
    }

    private fun overrideBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().moveTaskToBack(true)
                }
            })
    }
}