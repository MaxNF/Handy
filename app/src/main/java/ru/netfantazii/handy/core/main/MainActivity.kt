package ru.netfantazii.handy.core.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
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
import ru.netfantazii.handy.core.notifications.BUNDLE_DESTINATION_ID_KEY
import ru.netfantazii.handy.core.notifications.map.MapFragment
import ru.netfantazii.handy.core.preferences.FIRST_LAUNCH_KEY
import ru.netfantazii.handy.core.preferences.SHOULD_SILENT_SIGN_IN_KEY
import ru.netfantazii.handy.core.preferences.getCurrentThemeValue
import ru.netfantazii.handy.core.preferences.setTheme
import ru.netfantazii.handy.core.premium.RedeemDialog
import ru.netfantazii.handy.databinding.ActivityMainBinding
import ru.netfantazii.handy.databinding.NavigationHeaderBinding
import ru.netfantazii.handy.data.PbOperations
import ru.netfantazii.handy.data.User
import ru.netfantazii.handy.extensions.*

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
//убрать полосы при выборе будильника ??? сами кудато исчезли, хз что за баг (появились после удаление и рега нового акка, потом также исчезли)
//сделать time picker зависимым от локали

//дергается полоска количества покупок после выхода из контактов/настроек
//сделать, чтобы на телефоне был звук оповещения и вибрация (на эмуляторе почему-то есть)
//при клике по уведомлению (напр. геометки) логинится еще раз, хотя приложение открыто

//добавить в приветствие первую страницу, где разъясняется что происходит с данными пользователей (локация, аккаунт и прочее)
//добавить ссылку на политику конфиденциальности в меню приложения (СДЕЛАТЬ ПОЛИТИКУ КОНФИДЕНЦИАЛЬНОСТИ)
//зарегистрировать домен для приложения, прикрутить небольшой лендинг на гугл плей и политику конфиденциальности

//Сделать лимит геозон для бесплатной версии (1) и без лимита для премиум версии.
//вырезать рекламу в премиум версии

//Сделать поп-ап рекламу с возможностью закрытия при каждом третьем заходи в Share меню.
//прикрутить рекламный банер вниз (как у листоник). Банер можно будет закрыть крестиком, после чего на его место будет не очень яркое сообщение о том, что можно купить подписку либо бесплатно активировать пробный период на некоторое кол-во дней.

//исчезла тень у продуктов
//сделать меню Рассказать о приложении (обычный share sheet с предустановленным текстом)
//todo дополнить страницу netfantazii.ru/handypro/ картинкой и ссылкой на гугл-плей.

//сделать удаление сообщений из бд старше 1 дня со статусом доставлено и старше 30 дней со статусом не доставлено (т.к. платный акк есть, то настроить функцию по времени)
//оформить оповещения
//сделать сообщение при неудачном логине, а не тост с неизвестной ошибкой

//поискать бесплатные картинки, чтобы добавить в hint (каталоги, продукты, контакты)
//перепроверить бэкенд функции, подумать над возможной оптимизацией будущих расходов
//ограничить использование API firebase только для моего приложения

//исправить баг у оверлея каталогов (не сбрасывается результат переименования при отмене)
//добавить пояснение в раздел помощи, что поиск по карте осуществляется в видимых границах и он не автоматизирован
//заменить кругляшки на пины как в гугл картах
//сделать подпись к пинам, что это за место
//добавить кнопки зума на карту
//убрать поиск в тулбар
//добавить действие  для покупки премиума, если пользователь пытается добавить больше 1 геозоны

//протестировать биллинг как рекомендовано у гугла на сайте
//протестировать валидацию покупок
//сделать зависимость рекламных объявлений от премиум статуса пользователя
//сделать зависимость кол-ва геозон от премиум статуса пользователя
//почему-то при перезапуске программы не получаю премиум статус из кеша
//сделать обработку ошибок биллинга

//добавить раздел о программе, где сделать ссылку на яндекс карты
//при активной подписке сделать другой экран и добавить туда кнопку с ссылкой на настройки гугл плей (где пользователь сможет отменить подписку)
//сделать кликабельными ссылки на политику конфиденциальности при запуске программы
//сделать кликабельной ссылку на политику конф. в О программе
//сделать автообновление фрагмента подписок при покупке подписки

//сделать более заметную вибрацию \ звук при напоминаниях (для приходящего списка, оставляю такой же)
//проверить работоспособность подарочных кодов (похоже что не работают в тестовой версии, нужно проверить вечную покупку на не основном аккаунте)
//сделать кнопочку в меню для перехода к списку каталогов

//--------------------- ОБНОВЛЕНИЕ
//подготовить скриншоты
//подготовить описание
//сделать описание функций

//ЗАМЕНИТЬ ПЕРЕД РЕЛИЗОМ АПИ КЛЮЧ яндекс карт

//кнопки пометить как не влазят
//название подписки налазит на цену

//для ver 1.1:
//не скрывать иконку share, а сделать тусклой
//вместо слогана сделать подсказку, что требуется вход, чтобы делиться списками (можно курсивом)
//установить диалоговое окно со счетчиком, которое будет призывать оценить приложение в гугл плей (узнать как вытянуть эту инфу от гугла, оценено оно или нет)
//на сайте приложение выложить email адрес
//счетчик продуктов у групп (без группы) уплывает на новую строку
//увеличить нижний паддинг у списков, т.к. баннер + кнопка закрывают последнюю группу
//у присланных списков сортировка работает не правильно
//заменить устаревшие скриншоты Google Play на более актуальные
//исправить стиль диалоговых окон (ужать)
//на узких экранах счетчик кол-ва уплывает влево

//настройка куда добавлять новые элементы имеет плохо читаемую кнопку на светлых темах

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
    private lateinit var adScreen: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadTheme()
        mainBinding =
            DataBindingUtil.setContentView(this,
                R.layout.activity_main)
        createGlobalViewModels()
        mainBinding.viewModel = networkViewModel
        pbManager = ProgressBarManager()
        pbManager.setProgressBarView(mainBinding.progressBarContainer)
        pbText = mainBinding.progressBarDescription

        val toolbar = mainBinding.toolbar
        setSupportActionBar(toolbar)

        drawerLayout = mainBinding.navigationDrawer
//        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
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
        MobileAds.initialize(this, "ca-app-pub-4546128231433208~4467489086")
        setUpInterstitialAds()

        if ((application as HandyApplication).shouldRateDialogBeShown) {
            RateDialog().show(supportFragmentManager, "rate_dialog")
            (application as HandyApplication).shouldRateDialogBeShown = false
        }
    }

    private fun getTestDevicesConfiguration() = RequestConfiguration.Builder()
        .setTestDeviceIds(listOf("7878A7AA6ECBC58AB4FD75D4A0FD5C9E",
            "144FBBB720CF6B04896D65E9E88C6164"))
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

    private fun createGlobalViewModels() {
        val remoteRepository =
            (applicationContext as HandyApplication).remoteRepository
        networkViewModel = ViewModelProviders.of(
            this,
            NetworkVmFactory(remoteRepository)
        ).get(NetworkViewModel::class.java)

        val billingRepository =
            (applicationContext as HandyApplication).billingRepository
        val billingDataModel = BillingDataModel(billingRepository, packageName)
        billingViewModel =
            ViewModelProviders.of(this, BillingVmFactory(billingDataModel, application)
            ).get(BillingViewModel::class.java)
    }

    private fun loadTheme() {
        setTheme(this, getCurrentThemeValue(this))
    }

    private fun showWelcomeScreenIfNeeded() {
        if (isFirstLaunch()) {
            navController.navigate(R.id.welcomeFragment)
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