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
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.get
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import io.reactivex.plugins.RxJavaPlugins
import ru.netfantazii.handy.core.preferences.FIRST_LAUNCH_KEY
import ru.netfantazii.handy.core.preferences.SHOULD_SILENT_SIGN_IN_KEY
import ru.netfantazii.handy.core.preferences.getCurrentThemeValue
import ru.netfantazii.handy.core.preferences.setTheme
import ru.netfantazii.handy.databinding.ActivityMainBinding
import ru.netfantazii.handy.databinding.NavigationHeaderBinding
import ru.netfantazii.handy.extensions.reloadActivity
import ru.netfantazii.handy.extensions.showLongToast
import ru.netfantazii.handy.extensions.showShortToast
import ru.netfantazii.handy.model.GeofenceLimitException
import ru.netfantazii.handy.model.PbOperations
import ru.netfantazii.handy.model.User
import ru.netfantazii.handy.model.database.ErrorCodes

//Проверить и сделать, чтобы будильники и геометки перерегистрировался при перезагрузки телефона!
//проверить все цветовые схемы со всеми элементами (особенно напоминания, т.к. там подсветку заголовков не видно)
//todo попробовать другие фоны (градиент)

//настроить цвета кнопок у диалогов, а также чтобы кнопки не склеивались
//todo настроить правила безопасности в бэкенде
//todo сделать новое обучение

//устранить неприятную серую окантовку у продуктов/групп
//проверить все лэйауты в лэндскейп режиме, если где-то будет некрасиво - подкорректировать
//сделать ripple для всех мелких кнопок, в т.ч. и для каталогов и групп (если будет работать)

//сделать красивые переходы между фрагментами
//еще раз проверить уведомления на удаление при клике (в т.ч. и уведомления от присланных каталогов)
//сделать подходящие заголовки для всех новых фрагментов

//todo перевести все сообщения на русский язык
//сделать меню О программе с предложением доната (донат запрещен)
//сделать поп-ап спустя определенное кол-во запусков о донате (донат запрещен)

//todo узнать как быть с апи ключом для карт и где его хранить (возможно перед релизом заменить на новый)
//todo сделать новое приветственное сообщение с разделом: что нового в версии 1.3
//сделать лимит на обновление секретного кода раз в 1 минуту (мб через класс InputFilter)

//сделать кнопку Начать больше, чтобы влазил весь текст (в приветствии)
//если приложению не выдано разрешение на отслеживания местоположения, то после того как человек разрешает, сразу открывать фрагмент с геозонами
//todo проверить catalogJobService (на отложенное выполнение загрузки каталога при фейле)

//проверить перегенерацию секретного кода при случайном совпадении в клауд функциях (сделать вторую фейк функцию и вызвать вручную)
//добавить перерегистрацию геозон при очищении данных гугл плей
//сделать лимит геозон 100 шт.
//пофиксить слайдер радиуса геозон в лендскейп режиме

//--------------------- ОБНОВЛЕНИЕ
//todo подготовить новые скриншоты
//todo подготовить новое описание
//todo сделать описание новых функций


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
    private lateinit var pbManager: ProgressBarManager
    private lateinit var pbText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadTheme()
        val mainBinding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        createNetworkViewModel()
        mainBinding.viewModel = viewModel
        pbManager = ProgressBarManager()
        pbManager.setProgressBarView(mainBinding.progressBarContainer)
        pbText = mainBinding.progressBarDescription

        val toolbar = mainBinding.toolbar
        setSupportActionBar(toolbar)

        drawerLayout = mainBinding.navigationDrawer
        navigationView = drawerLayout.findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        setInitialMenuItemsVisibility(navigationView)

        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)

        sp = PreferenceManager.getDefaultSharedPreferences(this)
        disableFirestorePersistence()

        val header = navigationView.getHeaderView(0)
        val drawerHeaderBinding = NavigationHeaderBinding.bind(header)
        drawerHeaderBinding.viewModel = viewModel
        drawerHeaderBinding.signInButton.setOnLongClickListener {
            if (viewModel.user.get() != null) {
                signInClient.revokeAccess().addOnCompleteListener {
                    if (it.isSuccessful) {
                        showShortToast(this, getString(R.string.revoke_is_successful))
                        viewModel.signOut()
                    }
                }
            }
            true
        }

        buildSignInClient()
        registerNotitificationChannel(this)
        showWelcomeScreenIfNeeded()

        setUpErrorHandler()
    }

    override fun onResume() {
        super.onResume()
        tryToSilentSignIn()
    }

    private fun tryToSilentSignIn() {
        if (viewModel.user.get() == null && shouldSilentSignIn()) {
            val task = signInClient.silentSignIn()
            task.addOnSuccessListener {
                showGlobalProgressBar(PbOperations.SIGNING_IN)
                viewModel.signInToFirebase(it)
            }
            task.addOnFailureListener {
                // do nothing
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

    private fun setUpErrorHandler() {
        RxJavaPlugins.setErrorHandler { e ->
            if (e.cause is GeofenceLimitException) {
                showLongToast(this, getString(R.string.geofence_limit_error_message))
                return@setErrorHandler
            }

            when (e.cause?.message) {
                ErrorCodes.DATA_PAYLOAD_IS_NULL -> {
                    showLongToast(this, "Data payload error")
                    e.printStackTrace()
                }
                ErrorCodes.FOUND_USER_DUPLICATE -> {
                    showLongToast(this, "Found user duplicate")
                    e.printStackTrace()
                }
                ErrorCodes.INSTANCE_ID_TOKEN_NOT_FOUND -> {
                    showLongToast(this, "Instance Id token not found")
                    e.printStackTrace()
                }
                ErrorCodes.MESSAGE_IS_EMPTY -> {
                    showLongToast(this, "Message is empty")
                    e.printStackTrace()
                }
                ErrorCodes.USER_IS_NOT_FOUND -> {
                    showLongToast(this, getString(R.string.user_not_found_error))
                    e.printStackTrace()
                }
                ErrorCodes.NO_MESSAGES_SENT -> {
                    showLongToast(this, getString(R.string.no_messages_sent_error))
                    e.printStackTrace()
                }
                ErrorCodes.MESSAGE_FAILED_DUE_INCORRECT_SECRET -> {
                    showLongToast(this,
                        getString(R.string.message_failed_incorrect_secret_error))
                }

                ErrorCodes.USER_IS_NOT_LOGGED_IN -> {
                    showLongToast(this, "Authentication error. Not logged in")
                }
                else -> {
                    showLongToast(this, getString(R.string.uknown_error_occured))
                    e.printStackTrace()
                }
            }
            viewModel.hidePb()
        }
    }

    private fun setInitialMenuItemsVisibility(navigationView: NavigationView) {
        val menu = navigationView.menu
        val contactsMenuItem = menu.findItem(R.id.contactsFragment)
        contactsMenuItem.isVisible = viewModel.user.get() != null
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
                    setShouldNotSilentSignInNextTime()

                    val insideShareFragment =
                        navController.currentDestination == navController.graph[R.id.shareFragment]
                    val insideContactsFragment =
                        navController.currentDestination == navController.graph[R.id.contactsFragment]

                    if (insideShareFragment || insideContactsFragment) {
                        val pokedSuccessfully =
                            navController.popBackStack(navController.graph.startDestination, false)
                        if (!pokedSuccessfully) {
                            reloadActivity(this@MainActivity)
                        }
                    }
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
                it.getContentIfNotHandled()?.let {
                    showShortToast(this@MainActivity, getString(R.string.signin_error))
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

            user.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    val user = (sender as ObservableField<User?>).get()
                    navigationView.menu.findItem(R.id.contactsFragment).isVisible =
                        user != null
                }
            })
        }
    }

    private fun showGlobalProgressBar(operation: PbOperations) {
        val text = when (operation) {
            PbOperations.UPDATING_CLOUD_DATABASE -> getString(R.string.updating_cloud_db)
            PbOperations.DELETING_ACCOUNT -> getString(R.string.deleting_account)
            PbOperations.SENDING_CATALOG -> getString(R.string.sending_catalog)
            PbOperations.SIGNING_OUT -> getString(R.string.signing_out)
            PbOperations.SIGNING_IN -> getString(R.string.signing_in)
        }
        viewModel.inputFilter.netActionAllowed = false
        pbText.text = text
        pbManager.show()
    }

    private fun hideGlobalProgressBar() {
        viewModel.inputFilter.netActionAllowed = true
        pbManager.hide()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnSuccessListener {
                viewModel.signInToFirebase(it)
            }
            task.addOnFailureListener {
                it.printStackTrace()
                showShortToast(this, "Error while logging in")
            }
        }
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