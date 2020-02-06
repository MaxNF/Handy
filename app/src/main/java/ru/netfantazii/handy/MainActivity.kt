package ru.netfantazii.handy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import ru.netfantazii.handy.core.preferences.FIRST_LAUNCH_KEY
import ru.netfantazii.handy.core.preferences.currentSortOrder
import ru.netfantazii.handy.core.preferences.getCurrentThemeValue
import ru.netfantazii.handy.core.preferences.setTheme
import ru.netfantazii.handy.databinding.NavigationHeaderBinding
import ru.netfantazii.handy.extensions.getSortOrder
import ru.netfantazii.handy.model.User

const val NOTIFICATION_CHANNEL_ID = "Handy notification channel"
var user: User? = null

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG = "MainActivity"

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var sp: SharedPreferences
    private lateinit var viewModel: NetworkViewModel
    private val allLiveDataList = mutableListOf<LiveData<*>>()
    private lateinit var signInClient: GoogleSignInClient
    private val SIGN_IN_REQUEST_CODE = 0

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

        val header = navigationView.getHeaderView(0)
        val binding = NavigationHeaderBinding.bind(header)
        binding.viewModel = createNetworkViewModel()
        buildSignInClient()

        registerNotitificationChannel(this)
        showWelcomeScreenIfNeeded()
    }

    private fun buildSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        signInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun createNetworkViewModel(): NetworkViewModel {
        val remoteRepository =
            (applicationContext as HandyApplication).remoteRepository
        viewModel = ViewModelProviders.of(
            this,
            NetworkVmFactory(remoteRepository)
        ).get(NetworkViewModel::class.java)
        return viewModel
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


    private fun registerNotitificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStart() {
        super.onStart()
        subscribeToEvents()
        // todo возможно сделать silentSignIn() в гугл аккаунт
    }

    private fun subscribeToEvents() {
        val owner = this
        with(viewModel) {
            signInClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    val signInIntent = signInClient.signInIntent
                    startActivityForResult(signInIntent, SIGN_IN_REQUEST_CODE)
                }
            })
            allLiveDataList.add(signInClicked)

            signOutClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    // todo если пользователь находится во фрагментах требующих авторизации (список друзей, отправка каталога), то выйти из них
                }
            })
            allLiveDataList.add(signOutClicked)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                if (task.isSuccessful) {
                    viewModel.signInToFirebase(task.result!!)
                } else {
                    Toast.makeText(this, "Sign in task is not successful", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "ApiException", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        unsubscribeFromEvents()
    }

    private fun unsubscribeFromEvents() {
        allLiveDataList.forEach { it.removeObservers(this) }
    }
}