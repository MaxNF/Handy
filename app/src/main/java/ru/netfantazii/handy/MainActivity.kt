package ru.netfantazii.handy

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import ru.netfantazii.handy.core.preferences.FIRST_LAUNCH_KEY
import ru.netfantazii.handy.core.preferences.currentSortOrder
import ru.netfantazii.handy.core.preferences.getCurrentThemeValue
import ru.netfantazii.handy.core.preferences.setTheme
import ru.netfantazii.handy.db.SortOrder
import ru.netfantazii.handy.extensions.getSortOrder

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG = "MainActivity"

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadTheme()
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.navigation_drawer)
        navigationView = drawerLayout.findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)

        sp = PreferenceManager.getDefaultSharedPreferences(this)
        loadPreferencesToMemory()

        showWelcomeScreenIfNeeded()
    }

    private fun loadTheme() {
        setTheme(this, getCurrentThemeValue(this))
    }

    private fun showWelcomeScreenIfNeeded() {
        if (isFirstLaunch()) {
            setPrefToDefault()
            setFirstLaunchToFalse()
            navController.navigate(R.id.welcomeFragment)
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

    private fun loadPreferencesToMemory() {
        currentSortOrder = getSortOrder(this)
    }

    private fun setFirstLaunchToFalse() {
        sp.edit().putBoolean(FIRST_LAUNCH_KEY, false).apply()
    }

    private fun isFirstLaunch(): Boolean = sp.getBoolean(FIRST_LAUNCH_KEY, true)

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_rate_app -> {
                navigateToPlayMarket()
            }
            else -> {
                navController.navigate(item.itemId)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uncheckActiveMenuItem() {
        navigationView.checkedItem?.isChecked = false
    }

    fun checkMenuItem(itemId: Int) {
        navigationView.menu.findItem(itemId).isChecked = true
    }

    private fun navigateToPlayMarket() {
        startActivity(Intent(Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")))
    }

//    private fun setNavigationIconColor() {
//        val attrs = intArrayOf(R.attr.fabIconTintColor)
//        val typedArray = obtainStyledAttributes(R.style.Base, attrs)
//        val titleColor = typedArray.getColor(0, Color.TRANSPARENT)
//        val black = ContextCompat.getColor(this, R.color.fabIconTintColorBlack)
//        if (titleColor == black) {
//            navigationView.itemIconTintList =
//                ContextCompat.getColorStateList(this, R.color.appLogoBackgroundColor)
//            navigationView.itemTextColor =
//                ContextCompat.getColorStateList(this, R.color.appLogoBackgroundColor)
//        }
//        Log.d(TAG, "setNavigationIconColor: $titleColor")
//        typedArray.recycle()
//    }
}