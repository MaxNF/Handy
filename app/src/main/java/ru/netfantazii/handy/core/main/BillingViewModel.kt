package ru.netfantazii.handy.core.main

import android.app.Activity
import android.app.Application
import androidx.lifecycle.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.data.*

class BillingViewModel(application: Application, private val billingDataModel: BillingDataModel) :
    AndroidViewModel(application) {
    var oneMonthSubPrice: String? = null
        private set
    var oneYearSubPrice: String? = null
        private set
    var foreverProviderPrice: String? = null
        private set

    var currentPremium: ShopItem? = null
        private set

    private val disposables = CompositeDisposable()

    private val _oneMonthButtonClicked = MutableLiveData<Event<Unit>>()
    val oneMonthButtonClicked: LiveData<Event<Unit>> = _oneMonthButtonClicked

    private val _oneYearButtonClicked = MutableLiveData<Event<Unit>>()
    val oneYearButtonClicked: LiveData<Event<Unit>> = _oneYearButtonClicked

    private val _foreverButtonClicked = MutableLiveData<Event<Unit>>()
    val foreverButtonClicked: LiveData<Event<Unit>> = _foreverButtonClicked

    init {
        observeConnectionAndGetPrices()
        observePurchases()
    }

    private fun observePurchases() {
        disposables.add(billingDataModel.observePurchases()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { shopItem ->
                currentPremium = shopItem
            })
    }

    private fun observeConnectionAndGetPrices() {
        disposables.add(billingDataModel.connectToBillingAndGetPrices()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { priceList ->
                updatePrices(priceList)
            })
    }

    private fun updatePrices(priceList: List<BillingPrice>) {
        priceList.forEach {
            when (it.type) {
                BillingPurchaseTypes.FOREVER -> foreverProviderPrice = it.price
                BillingPurchaseTypes.ONE_YEAR -> oneYearSubPrice = it.price
                BillingPurchaseTypes.ONE_MONTH -> oneMonthSubPrice = it.price
            }
        }
    }

    fun setCurrentPremiumStatus() {
        billingDataModel.getCurrentPremiumStatus()?.let {
            it.observeOn(AndroidSchedulers.mainThread())
                .subscribe { shopItem ->
                    currentPremium = shopItem
                }
        } ?: setPremiumNull()
    }

    private fun setPremiumNull() {
        currentPremium = null
    }

    fun launchBillingFlow(activity: Activity, type: BillingPurchaseTypes) {
        billingDataModel.launchBillingFlow(activity, type)
    }

    fun onOneMonthButtonClick() {
        _oneMonthButtonClicked.value = Event(Unit)
    }

    fun onOneYearButtonClick() {
        _oneYearButtonClicked.value = Event(Unit)
    }

    fun onForeverButtonClick() {
        _foreverButtonClicked.value = Event(Unit)
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