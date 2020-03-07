package ru.netfantazii.handy.core.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
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
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import io.reactivex.plugins.RxJavaPlugins
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.notifications.BUNDLE_DESTINATION_ID_KEY
import ru.netfantazii.handy.core.notifications.map.MapFragment
import ru.netfantazii.handy.core.preferences.FIRST_LAUNCH_KEY
import ru.netfantazii.handy.core.preferences.SHOULD_SILENT_SIGN_IN_KEY
import ru.netfantazii.handy.core.preferences.getCurrentThemeValue
import ru.netfantazii.handy.core.preferences.setTheme
import ru.netfantazii.handy.databinding.ActivityMainBinding
import ru.netfantazii.handy.databinding.NavigationHeaderBinding
import ru.netfantazii.handy.extensions.reloadActivity
import ru.netfantazii.handy.extensions.showLongToast
import ru.netfantazii.handy.extensions.showShortToast
import ru.netfantazii.handy.data.GeofenceLimitException
import ru.netfantazii.handy.data.PbOperations
import ru.netfantazii.handy.data.User
import ru.netfantazii.handy.data.database.ErrorCodes

//Проверить и сделать, чтобы будильники и геометки перерегистрировался при перезагрузки телефона!
//проверить все цветовые схемы со всеми элементами (особенно напоминания, т.к. там подсветку заголовков не видно)
//попробовать другие фоны (градиент)

//настроить цвета кнопок у диалогов, а также чтобы кнопки не склеивались
//настроить правила безопасности в бэкенде
//сделать новое обучение

//устранить неприятную серую окантовку у продуктов/групп
//проверить все лэйауты в лэндскейп режиме, если где-то будет некрасиво - подкорректировать
//сделать ripple для всех мелких кнопок, в т.ч. и для каталогов и групп (если будет работать)

//сделать красивые переходы между фрагментами
//еще раз проверить уведомления на удаление при клике (в т.ч. и уведомления от присланных каталогов)
//сделать подходящие заголовки для всех новых фрагментов

//перевести все сообщения на русский язык
//сделать меню О программе с предложением доната (донат запрещен)
//сделать поп-ап спустя определенное кол-во запусков о донате (донат запрещен)

//узнать как быть с апи ключом для карт и где его хранить (возможно перед релизом заменить на новый)
//сделать новое приветственное сообщение с разделом: что нового в про-версии
//сделать лимит на обновление секретного кода раз в 1 минуту (мб через класс InputFilter)

//сделать кнопку Начать больше, чтобы влазил весь текст (в приветствии)
//если приложению не выдано разрешение на отслеживания местоположения, то после того как человек разрешает, сразу открывать фрагмент с геозонами
//проверить catalogJobService (на отложенное выполнение загрузки каталога при фейле)

//проверить перегенерацию секретного кода при случайном совпадении в клауд функциях (сделать вторую фейк функцию и вызвать вручную)
//добавить перерегистрацию геозон при очищении данных гугл плей
//сделать лимит геозон 100 шт.

//пофиксить слайдер радиуса геозон в лендскейп режиме
//позиция таб лэйаута сбрасывается после поворота экрана
//добавить подсказку во фрагмент с контактами (добавьте новый контакт)

//исправить сообщение про долгий тап
//исправить диалоговое окно с описанием каталога (стиль)
//не добавлять в базу геозоны, если регистрация не удалась

//сделать возможность делиться секретным кодом с помощью выбора средства отправления
//todo убрать полосы при выборе будильника ??? сами кудато исчезли, хз что за баг (появились после удаление и рега нового акка, потом также исчезли)
//сделать time picker зависимым от локали

//дергается полоска количества покупок после выхода из контактов/настроек
//сделать, чтобы на телефоне был звук оповещения и вибрация (на эмуляторе почему-то есть)
//при клике по уведомлению (напр. геометки) логинится еще раз, хотя приложение открыто

//todo добавить в приветствие первую страницу, где разъясняется что происходит с данными пользователей (локация, аккаунт и прочее)
//todo зарегистрировать домен для приложения, прикрутить небольшой лендинг на гугл плей и политику конфиденциальности

//Сделать лимит геозон для бесплатной версии (1) и без лимита для премиум версии.
//вырезать рекламу в премиум версии

//Сделать поп-ап рекламу с возможностью закрытия при каждом третьем заходи в Share меню.
//прикрутить рекламный банер вниз (как у листоник). Банер можно будет закрыть крестиком, после чего на его место будет не очень яркое сообщение о том, что можно купить подписку либо бесплатно активировать пробный период на некоторое кол-во дней.

//исчезла тень у продуктов
//сделать меню Рассказать о приложении (обычный share sheet с предустановленным текстом)

//todo сделать удаление сообщений из бд старше 1 дня со статусом доставлено и старше 30 дней со статусом не доставлено (т.к. платный акк есть, то настроить функцию по времени)
//оформить оповещения
//сделать сообщение при неудачном логине, а не тост с неизвестной ошибкой

//поискать бесплатные картинки, чтобы добавить в hint (каталоги, продукты, контакты)
//todo перепроверить бэкенд функции, подумать над возможной оптимизацией будущих расходов
//todo ограничить использование API firebase только для моего приложения

//исправить баг у оверлея каталогов (не сбрасывается результат переименования при отмене)
//добавить пояснение в раздел помощи, что поиск по карте осуществляется в видимых границах и он не автоматизирован
//todo заменить кругляшки на пины как в гугл картах
//todo сделать подпись к пинам, что это за место
//todo добавить кнопки зума на карту
//todo убрать поиск в тулбар
//todo добавить действие  для покупки премиума, если пользователь пытается добавить больше 1 геозоны

//todo протестировать биллинг как рекомендовано у гугла на сайте
//todo протестировать валидацию покупок
//todo сделать зависимость рекламных объявлений от премиум статуса пользователя
//todo сделать зависимость кол-ва геозон от премиум статуса пользователя




//--------------------- ОБНОВЛЕНИЕ
//подготовить скриншоты
//подготовить описание
//сделать описание функций

//TODO ЗАМЕНИТЬ ПЕРЕД РЕЛИЗОМ АПИ КЛЮЧ


const val REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel"
const val CATALOG_RECEIVED_NOTIFICATION_CHANNEL_ID = "download_notification_channel"

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadTheme()
        mainBinding =
            DataBindingUtil.setContentView(this,
                R.layout.activity_main)
        createNetworkViewModel()
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
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)

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
        registerNotitificationChannels()
        showWelcomeScreenIfNeeded()

        setUpErrorHandler()
        handleNotificationIntent(intent)

        createBillingViewModel()
        MobileAds.initialize(this, "ca-app-pub-4546128231433208~4467489086")
    }

    private fun createBillingViewModel(): BillingViewModel {
        val billingRepository =
            (applicationContext as HandyApplication).billingRepository
        val billingDataModel = BillingDataModel(billingRepository, packageName)
        billingViewModel =
            ViewModelProviders.of(this, BillingVmFactory(billingDataModel, application)
            ).get(BillingViewModel::class.java)
        return billingViewModel
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

    private fun setUpErrorHandler() {
        RxJavaPlugins.setErrorHandler { e ->
            Log.d(TAG, "setUpErrorHandler: $e")
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
                    showLongToast(this, getString(R.string.unknown_error_occurred))
                    e.printStackTrace()
                }
            }
            networkViewModel.hidePb()
        }
    }

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

    private fun createNetworkViewModel(): NetworkViewModel {
        val remoteRepository =
            (applicationContext as HandyApplication).remoteRepository
        networkViewModel = ViewModelProviders.of(
            this,
            NetworkVmFactory(remoteRepository)
        ).get(NetworkViewModel::class.java)
        return networkViewModel
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
            R.id.navigation_recommend_app -> {
                val googlePlayLink = getString(R.string.googlePlayLink)
                openShareSheet(getString(R.string.recommend_app_text, googlePlayLink))
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


    private fun registerNotitificationChannels() {
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
            })
            notificationChannels.add(NotificationChannel(CATALOG_RECEIVED_NOTIFICATION_CHANNEL_ID,
                getString(R.string.catalog_received_notification_ch_name),
                NotificationManager.IMPORTANCE_HIGH).apply {
                description = catalogReceivedDescription
            })

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(notificationChannels)
        }
    }

    override fun onStart() {
        super.onStart()
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
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
                it.getContentIfNotHandled()?.let {
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