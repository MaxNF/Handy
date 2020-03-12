package ru.netfantazii.handy.core.main

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.android.billingclient.api.BillingClient
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.data.*
import java.util.concurrent.TimeUnit
import kotlin.math.log

class BillingViewModel(application: Application, private val billingDataModel: BillingDataModel) :
    AndroidViewModel(application) {
    private val TAG = "BillingViewModel"

    var oneMonthBillingObject: BillingObject? = null
        private set
    var oneYearBillingObject: BillingObject? = null
        private set
    var foreverBillingObject: BillingObject? = null
        private set

    val premiumStatus: ObservableField<ShopItem?> = ObservableField()

    // Если хоть одно из значений прайс-листа - null, то возвращаем false
    val isPriceListReady: Boolean
        get() {
            val value =
                !(oneMonthBillingObject == null || oneYearBillingObject == null || foreverBillingObject == null)
            Log.d(TAG, "Is price list ready? $value")
            return value
        }

    private val disposables = CompositeDisposable()

    private val _oneMonthButtonClicked = MutableLiveData<Event<BillingObject>>()
    val oneMonthButtonClicked: LiveData<Event<BillingObject>> = _oneMonthButtonClicked

    private val _oneYearButtonClicked = MutableLiveData<Event<BillingObject>>()
    val oneYearButtonClicked: LiveData<Event<BillingObject>> = _oneYearButtonClicked

    private val _foreverButtonClicked = MutableLiveData<Event<BillingObject>>()
    val foreverButtonClicked: LiveData<Event<BillingObject>> = _foreverButtonClicked

    private val _unknownBillingException = MutableLiveData<Event<Throwable>>()
    val unknownBillingException: LiveData<Event<Throwable>> = _unknownBillingException

    private val _billingFlowError = MutableLiveData<Event<Int>>()
    val billingFlowError: LiveData<Event<Int>> = _billingFlowError

    private val _openSubscriptionSettingsClicked = MutableLiveData<Event<Unit>>()
    val openSubscriptionSettingsClicked: LiveData<Event<Unit>> = _openSubscriptionSettingsClicked

    init {
        establishConnectionAndGetPricesAndCurrentPremiumStatus()
        observePurchases()
    }

    private fun setPremiumStatus(shopItem: ShopItem?) {
        premiumStatus.set(shopItem)
        getApplication<HandyApplication>().isPremium.set(shopItem != null)
    }

    private fun observePurchases() {
        disposables.add(billingDataModel.observePurchases()
            .observeOn(AndroidSchedulers.mainThread())
            .retryWhen { errorStream ->
                errorStream.flatMap { t: Throwable ->
                    if (t is BillingException) {
                        Log.d(TAG, "observePurchases: ${t.message}")
                        Observable.just(0L)
                    } else {
                        Observable.error(t)
                    }
                }
            }
            .subscribe({ shopItem ->
                setPremiumStatus(shopItem)
                Log.d(TAG, "observePurchases: $shopItem")
            }, {
                _unknownBillingException.value = Event(it)
            }))
    }

    private fun establishConnectionAndGetPricesAndCurrentPremiumStatus() {
        disposables.add(billingDataModel.connectToBillingAndGetPrices()
            .observeOn(AndroidSchedulers.mainThread())
            .retryWhen { errorStream ->
                errorStream.flatMap { t ->
                    clearPrices()
                    if (t is BillingException) {
                        t.printStackTrace()
                        if (t.message == BillingException.SERVICE_DISCONNECTED) {
                            Log.d(TAG, "BILLING DISCONNECTED, CONNECTING AGAIN...")
                            Observable.timer(10, TimeUnit.SECONDS)
                        } else {
                            Log.d(TAG,
                                "ERROR WHILE MAINTAINING BILLING CONNECTION, RETRYING IN 30 SEC")
                            Observable.timer(20, TimeUnit.SECONDS)
                        }
                    } else {
                        Observable.error(t)
                    }
                }
            }
            .subscribe { priceList ->
                updatePrices(priceList)
                setCurrentPremiumStatus()
                Log.d(TAG, "observeConnectionAndGetPrices: $priceList")
            })
    }

    private fun clearPrices() {
        oneMonthBillingObject = null
        oneYearBillingObject = null
        foreverBillingObject = null
    }

    private fun updatePrices(objectList: List<BillingObject>) {
        objectList.forEach {
            Log.d(TAG, "updatePrices: ${it.type}")
            when (it.type) {
                BillingPurchaseTypes.FOREVER -> foreverBillingObject = it
                BillingPurchaseTypes.ONE_YEAR -> oneYearBillingObject = it
                BillingPurchaseTypes.ONE_MONTH -> oneMonthBillingObject = it
            }
        }
    }

    fun setCurrentPremiumStatus() {
        if (billingDataModel.isBillingClientReady()) {
            billingDataModel.getCurrentPremiumStatus()?.let {
                it.observeOn(AndroidSchedulers.mainThread())
                    .subscribe { shopItem ->
                        setPremiumStatus(shopItem)
                        Log.d(TAG, "setCurrentPremiumStatus: $shopItem")
                    }
            } ?: setPremiumNull()
        }
    }

    private fun setPremiumNull() {
        Log.d(TAG, "setPremiumNull: ")
        setPremiumStatus(null)
    }

    fun launchBillingFlow(activity: Activity, billingObject: BillingObject) {
        val result = billingDataModel.launchBillingFlow(activity, billingObject)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _billingFlowError.value = Event(result.responseCode)
        }
        Log.d(TAG, "launchBillingFlow: ${result.responseCode}")
    }

    fun openGooglePlaySubscriptionSetting() {
        _openSubscriptionSettingsClicked.value = Event(Unit)
    }

    fun onOneMonthButtonClick() {
        Log.d(TAG, "onOneMonthButtonClick: ")
        oneMonthBillingObject?.let { _oneMonthButtonClicked.value = Event(it) }

    }

    fun onOneYearButtonClick() {
        Log.d(TAG, "onOneYearButtonClick: ")
        oneYearBillingObject?.let { _oneYearButtonClicked.value = Event(it) }
    }

    fun onForeverButtonClick() {
        Log.d(TAG, "onForeverButtonClick: ")
        foreverBillingObject?.let { _foreverButtonClicked.value = Event(it) }
    }
}

class BillingVmFactory(
    private val billingDataModel: BillingDataModel,
    private val application: Application
) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillingViewModel::class.java)) {
            return BillingViewModel(application, billingDataModel) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}