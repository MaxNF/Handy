package ru.netfantazii.handy.core.main

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.data.*

class BillingViewModel(application: Application, private val billingDataModel: BillingDataModel) :
    AndroidViewModel(application) {
    private val TAG = "BillingViewModel"

    var oneMonthBillingObject: BillingObject? = null
        private set
    var oneYearBillingObject: BillingObject? = null
        private set
    var foreverBillingObject: BillingObject? = null
        private set

    var premiumStatus: ShopItem? = null
        set(value) {
            field = value
            getApplication<HandyApplication>().isPremium.set(value != null)
        }

    private val disposables = CompositeDisposable()

    private val _oneMonthButtonClicked = MutableLiveData<Event<BillingObject>>()
    val oneMonthButtonClicked: LiveData<Event<BillingObject>> = _oneMonthButtonClicked

    private val _oneYearButtonClicked = MutableLiveData<Event<BillingObject>>()
    val oneYearButtonClicked: LiveData<Event<BillingObject>> = _oneYearButtonClicked

    private val _foreverButtonClicked = MutableLiveData<Event<BillingObject>>()
    val foreverButtonClicked: LiveData<Event<BillingObject>> = _foreverButtonClicked

    init {
        observeConnectionAndGetPrices()
        observePurchases()
    }

    private fun observePurchases() {
        disposables.add(billingDataModel.observePurchases()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { shopItem ->
                premiumStatus = shopItem
                Log.d(TAG, "observePurchases: $shopItem")
            })
    }

    private fun observeConnectionAndGetPrices() {
        disposables.add(billingDataModel.connectToBillingAndGetPrices()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { priceList ->
                updatePrices(priceList)
                Log.d(TAG, "observeConnectionAndGetPrices: $priceList")
            })
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
        billingDataModel.getCurrentPremiumStatus()?.let {
            it.observeOn(AndroidSchedulers.mainThread())
                .subscribe { shopItem ->
                    premiumStatus = shopItem
                    Log.d(TAG, "setCurrentPremiumStatus: $shopItem")
                }
        } ?: setPremiumNull()
    }

    private fun setPremiumNull() {
        Log.d(TAG, "setPremiumNull: ")
        premiumStatus = null
    }

    fun launchBillingFlow(activity: Activity, billingObject: BillingObject) {
        Log.d(TAG, "launchBillingFlow: ")
        billingDataModel.launchBillingFlow(activity, billingObject)
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