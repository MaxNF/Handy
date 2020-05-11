package ru.netfantazii.handy.ui.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.drawerlayout.widget.DrawerLayout
import androidx.leanback.app.ProgressBarManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.*
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.R
import ru.netfantazii.handy.data.service.BUNDLE_DESTINATION_ID_KEY
import ru.netfantazii.handy.ui.notifications.map.MapFragment
import ru.netfantazii.handy.ui.preferences.FIRST_LAUNCH_KEY
import ru.netfantazii.handy.ui.preferences.SHOULD_SILENT_SIGN_IN_KEY
import ru.netfantazii.handy.utils.getCurrentThemeValue
import ru.netfantazii.handy.utils.setTheme
import ru.netfantazii.handy.ui.premium.RedeemDialog
import ru.netfantazii.handy.databinding.ActivityMainBinding
import ru.netfantazii.handy.databinding.NavigationHeaderBinding
import ru.netfantazii.handy.data.model.PbOperations
import ru.netfantazii.handy.data.model.User
import ru.netfantazii.handy.di.ViewModelFactory
import ru.netfantazii.handy.di.components.MainComponent
import ru.netfantazii.handy.utils.extensions.*
import ru.netfantazii.handy.initGlobalRxErrorHandler
import javax.inject.Inject

const val REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel"
const val CATALOG_RECEIVED_NOTIFICATION_CHANNEL_ID = "download_notification_channel"

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var component: MainComponent
    @Inject
    lateinit var factory: ViewModelFactory

    private val TAG = "MainActivity"

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var sp: SharedPreferences
    private lateinit var networkViewModel: NetworkViewModel
    private lateinit var billingViewModel: BillingViewModel
    private val allLiveDataList = mutableListOf<LiveData<*>>()
    private lateinit var signInClient: GoogleSignInClient
    private val SIGN_IN_REQUEST_CODE = 0
    private lateinit var pbManager: ProgressBarManager
    private lateinit var pbText: TextView
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var adScreen: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        component = (application as HandyApplication).appComponent.mainComponent().create()
        component.inject(this)
        super.onCreate(savedInstanceState)
        loadTheme()
        mainBinding =
            DataBindingUtil.setContentView(this,
                R.layout.activity_main)
        networkViewModel = ViewModelProviders.of(this, factory).get(NetworkViewModel::class.java)
        billingViewModel = ViewModelProviders.of(this, factory).get(BillingViewModel::class.java)

        mainBinding.viewModel = networkViewModel
        pbManager = ProgressBarManager()
        pbManager.setProgressBarView(mainBinding.progressBarContainer)
        pbText = mainBinding.progressBarDescription

        val toolbar = mainBinding.toolbar
        setSupportActionBar(toolbar)

        drawerLayout = mainBinding.navigationDrawer
        navigationView = drawerLayout.findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        setInitialMenuItemsVisibility(navigationView)

        navController = Navigation.findNavController(this,
            R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
//        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        sp = PreferenceManager.getDefaultSharedPreferences(this)
        disableFirestorePersistence()

        val header = navigationView.getHeaderView(0)
        val drawerHeaderBinding = NavigationHeaderBinding.bind(header)
        drawerHeaderBinding.viewModel = networkViewModel
        drawerHeaderBinding.signInButton.setOnLongClickListener {
            if (networkViewModel.user.get() != null) {
                signInClient.revokeAccess().addOnCompleteListener {
                    if (it.isSuccessful) {
                        showShortToast(this, getString(R.string.revoke_is_successful))
                        networkViewModel.signOut()
                    }
                }
            }
            true
        }

        buildSignInClient()
        registerNotificationChannels()
        showWelcomeScreenIfNeeded()

        initGlobalRxErrorHandler(this, networkViewModel)
        handleNotificationIntent(intent)

        MobileAds.setRequestConfiguration(getTestDevicesConfiguration())
        MobileAds.initialize(this, getString(R.string.admob_app_id))
        setUpInterstitialAds()

        if ((application as HandyApplication).shouldRateDialogBeShown) {
            RateDialog().show(supportFragmentManager, "rate_dialog")
            (application as HandyApplication).shouldRateDialogBeShown = false
        }
    }

    fun lockDrawerClosed() {
        if (::drawerLayout.isInitialized) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    fun unlockDrawer() {
        if (::drawerLayout.isInitialized) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
    }

    private fun getTestDevicesConfiguration() = RequestConfiguration.Builder()
        // add your test device ids to keys.xml and link them here
        .setTestDeviceIds(listOf(getString(R.string.admob_test_device_id_1),
            getString(R.string.admob_test_device_id_2)))
        .build()


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ${item.itemId}")
        return if (item.itemId == android.R.id.home) {
            when (navController.currentDestination?.id) {
                R.id.catalogs_fragment -> {
                    drawerLayout.openDrawer(GravityCompat.START)
                    true
                }
                R.id.welcomeFragment -> {
                    moveTaskToBack(true)
                    true
                }
                else -> {
                    navController.popBackStack()
                }
            }
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun setUpInterstitialAds() {
        adScreen = InterstitialAd(this)
        adScreen.adUnitId = getString(R.string.ad_interstitial_share_unit_id)
        adScreen.loadAd(AdRequest.Builder().build())
        adScreen.adListener = object : AdListener() {
            override fun onAdClosed() {
                adScreen.loadAd(AdRequest.Builder().build())
            }
        }
    }

    fun showAdScreen() {
        if (!(application as HandyApplication).isPremium.get()) {
            if (::adScreen.isInitialized) {
                if (adScreen.isLoaded) {
                    adScreen.show()
                }
            }
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.extras?.let {
            val destinationId = it.getInt(BUNDLE_DESTINATION_ID_KEY, 0)
            Log.d(TAG, "onCreate: $destinationId")
            if (destinationId != 0) {
                intent.removeExtra(BUNDLE_DESTINATION_ID_KEY)
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.catalogs_fragment, false)
                    .setLaunchSingleTop(true)
                    .build()
                navController.navigate(destinationId, it, navOptions)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        tryToSilentSignIn()
        billingViewModel.setCurrentPremiumStatus()
        uncheckMenuItems()
    }

    override fun onNewIntent(intent: Intent?) {
        Log.d(TAG, "onNewIntent: ")
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun tryToSilentSignIn() {
        if (networkViewModel.user.get() == null && shouldSilentSignIn()) {
            val task = signInClient.silentSignIn()
            task.addOnSuccessListener {
                showGlobalProgressBar(PbOperations.SIGNING_IN)
                networkViewModel.signInToFirebase(it)
            }
            task.addOnFailureListener {
                //do nothing
            }
        }
    }

    private fun setShouldSilentSignInNextTime() {
        sp.edit().putBoolean(SHOULD_SILENT_SIGN_IN_KEY, true).apply()
    }

    private fun setShouldNotSilentSignInNextTime() {
        sp.edit().putBoolean(SHOULD_SILENT_SIGN_IN_KEY, false).apply()
    }

    private fun shouldSilentSignIn() = sp.getBoolean(SHOULD_SILENT_SIGN_IN_KEY, true)

    private fun setInitialMenuItemsVisibility(navigationView: NavigationView) {
        val menu = navigationView.menu
        val contactsMenuItem = menu.findItem(R.id.contactsFragment)
        contactsMenuItem.isVisible = networkViewModel.user.get() != null
    }

    private fun buildSignInClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        signInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun loadTheme() {
        setTheme(this,
            getCurrentThemeValue(this))
    }

    private fun showWelcomeScreenIfNeeded() {
        if (isFirstLaunch()) {
            if (navController.currentDestination?.id != R.id.welcomeFragment) {
                navController.navigate(R.id.welcomeFragment)
            }
        }
    }

    private fun isFirstLaunch(): Boolean = sp.getBoolean(FIRST_LAUNCH_KEY, true)

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_rate_app -> {
                navigateToPlayMarket(this)
            }
            R.id.navigation_recommend_app -> {
                val googlePlayLink = getString(R.string.googlePlayLink)
                openShareSheet(getString(R.string.recommend_app_text, googlePlayLink))
            }
            R.id.navigation_redeem -> {
                RedeemDialog().show(supportFragmentManager, "redeem_dialog")
            }
            else -> {
                navController.navigate(item.itemId)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uncheckMenuItems() {
        val currentDest = navController.currentDestination?.id
        if (currentDest == R.id.catalogs_fragment || currentDest == R.id.products_fragment) {
            navigationView.checkedItem?.isChecked = false
        }
    }

    fun checkMenuItem(itemId: Int) {
        navigationView.setCheckedItem(itemId)
    }

    private fun registerNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val remindersDescription =
                getString(R.string.notification_channel_reminders_description)
            val catalogReceivedDescription =
                getString(R.string.notification_channel_downloads_description)

            val notificationChannels = mutableListOf<NotificationChannel>()
            notificationChannels.add(NotificationChannel(REMINDER_NOTIFICATION_CHANNEL_ID,
                getString(R.string.reminders_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH).apply {
                description = remindersDescription
                enableVibration(true)
                vibrationPattern = defaultVibrationPattern()
                enableLights(true)
                lightColor = Color.WHITE
            })
            notificationChannels.add(NotificationChannel(CATALOG_RECEIVED_NOTIFICATION_CHANNEL_ID,
                getString(R.string.catalog_received_notification_ch_name),
                NotificationManager.IMPORTANCE_HIGH).apply {
                description = catalogReceivedDescription
                enableVibration(true)
                vibrationPattern = defaultVibrationPattern()
                enableLights(true)
                lightColor = Color.WHITE
            })

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(notificationChannels)
        }
    }

    override fun onStart() {
        super.onStart()
        subscribeToNetworkEvents()
        subscribeToBillingEvents()
    }

    private fun subscribeToBillingEvents() {
        val owner = this
        with(billingViewModel) {
            unknownBillingException.observe(owner, Observer { event ->
                event.getContentIfNotHandled()?.let { e ->
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showLongToast(this@MainActivity, getString(R.string.unknown_billing_exception))
                }
            })
            allLiveDataList.add(unknownBillingException)

            billingFlowError.observe(owner, Observer { event ->
                event.getContentIfNotHandled()?.let { code ->
                    FirebaseCrashlytics.getInstance().log("Billing flow error. Code: $code")
                    showLongToast(this@MainActivity,
                        getString(R.string.billing_flow_error_message, code))
                }
            })
            allLiveDataList.add(billingFlowError)
        }
    }

    private fun subscribeToNetworkEvents() {
        val owner = this
        with(networkViewModel) {
            signInClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    val signInIntent = signInClient.signInIntent
                    startActivityForResult(signInIntent, SIGN_IN_REQUEST_CODE)
                }
            })
            allLiveDataList.add(signInClicked)

            signOutClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    setShouldNotSilentSignInNextTime()
                    closeInternetRequiredFragments()
                }
            })
            allLiveDataList.add(signOutClicked)

            signInComplete.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    setShouldSilentSignInNextTime()
                }
            })
            allLiveDataList.add(signInComplete)

            catalogSentSuccessfully.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { catalogName ->
                    showShortToast(this@MainActivity,
                        getString(R.string.catalog_was_sent_successfully, catalogName))
                }
            })
            allLiveDataList.add(catalogSentSuccessfully)

            firebaseSignInError.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { exception ->
                    exception.printStackTrace()
                    showSignInFailedToast()
                    this@MainActivity.signInClient.signOut()
                }
            })
            allLiveDataList.add(firebaseSignInError)

            secretCopied.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showShortToast(this@MainActivity, getString(R.string.secret_is_copied))
                }
            })
            allLiveDataList.add(secretCopied)

            startingToSendCatalog.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    // do nothing
                }
            })
            allLiveDataList.add(startingToSendCatalog)

            changingSecretFailed.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showShortToast(this@MainActivity,
                        getString(R.string.changing_secret_failed_message))
                }
            })
            allLiveDataList.add(changingSecretFailed)

            accountDeletedSuccessfully.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showShortToast(this@MainActivity,
                        getString(R.string.account_deleted_successfully))
                }
            })
            allLiveDataList.add(accountDeletedSuccessfully)

            accountDeletionFailed.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showShortToast(this@MainActivity, getString(R.string.account_deletion_failed))
                }
            })
            allLiveDataList.add(accountDeletionFailed)

            showProgressBar.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { operation ->
                    showGlobalProgressBar(operation)
                }
            })
            allLiveDataList.add(showProgressBar)

            hideProgressBar.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    hideGlobalProgressBar()
                }
            })
            allLiveDataList.add(hideProgressBar)

            shareSecretCodeClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { code ->
                    val appUrl = getString(R.string.googlePlayLink)
                    val finalTextToShare =
                        getString(R.string.shareCodeTemplate, code, appUrl)
                    openShareSheet(finalTextToShare)
                }
            })
            allLiveDataList.add(shareSecretCodeClicked)

            user.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    val user = (sender as ObservableField<User?>).get()
                    navigationView.menu.findItem(R.id.contactsFragment).isVisible =
                        user != null
                }
            })
        }
    }

    private fun closeInternetRequiredFragments() {
        val insideShareFragment =
            navController.currentDestination == navController.graph[R.id.shareFragment]
        val insideContactsFragment =
            navController.currentDestination == navController.graph[R.id.contactsFragment]

        val insideNotificationFragment =
            navController.currentDestination == navController.graph[R.id.notifications_fragment]
        val insideMapFragment = if (insideNotificationFragment) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            val notificationFragment =
                navHostFragment?.childFragmentManager?.primaryNavigationFragment
            val mapFragment =
                notificationFragment?.childFragmentManager?.fragments?.find { it is MapFragment }

            when {
                mapFragment == null -> false
                mapFragment.isVisible -> true
                else -> {
                    notificationFragment.childFragmentManager.beginTransaction().remove(mapFragment)
                        .commit()
                    false
                }
            }
        } else {
            false
        }

        if (insideShareFragment || insideContactsFragment || insideMapFragment) {
            val pokedSuccessfully =
                navController.popBackStack(navController.graph.startDestination, false)
            if (!pokedSuccessfully) {
                reloadActivity(this@MainActivity)
            }
        }
    }

    private fun openShareSheet(textToShare: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, textToShare)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }

    private fun showGlobalProgressBar(operation: PbOperations) {
        val text = when (operation) {
            PbOperations.UPDATING_CLOUD_DATABASE -> getString(R.string.updating_cloud_db)
            PbOperations.DELETING_ACCOUNT -> getString(R.string.deleting_account)
            PbOperations.SENDING_CATALOG -> getString(R.string.sending_catalog)
            PbOperations.SIGNING_OUT -> getString(R.string.signing_out)
            PbOperations.SIGNING_IN -> getString(R.string.signing_in)
        }
        networkViewModel.inputFilter.netActionAllowed = false
        pbText.text = text
        pbManager.show()
    }

    private fun hideGlobalProgressBar() {
        networkViewModel.inputFilter.netActionAllowed = true
        pbManager.hide()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: ")
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnSuccessListener {
                networkViewModel.signInToFirebase(it)
            }
            task.addOnFailureListener {
                it.printStackTrace()
                networkViewModel.hidePb()
                showSignInFailedToast()
            }
        }
    }

    private fun showSignInFailedToast() {
        showLongToast(this, getString(R.string.signin_error))
    }

    private fun disableFirestorePersistence() {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
    }

    override fun onStop() {
        super.onStop()
        unsubscribeFromEvents()
    }

    private fun unsubscribeFromEvents() {
        allLiveDataList.forEach { it.removeObservers(this) }
    }
}