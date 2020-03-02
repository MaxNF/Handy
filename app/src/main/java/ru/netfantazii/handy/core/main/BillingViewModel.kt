package ru.netfantazii.handy.core.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.data.*
import ru.netfantazii.handy.data.database.SkuList
import ru.netfantazii.handy.repositories.BillingRepository
import java.lang.UnsupportedOperationException

class BillingViewModel(application: Application, private val billingDataModel: BillingDataModel) :
    AndroidViewModel(application) {
    private var oneMonthSubPrice: String? = null
    private var oneYearSubPrice: String? = null
    private var foreverProviderPrice: String? = null

    var currentPremium: ShopItem? = null
        private set

    private val disposables = CompositeDisposable()

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
                BillingPriceTypes.FOREVER_PRICE -> foreverProviderPrice = it.price
                BillingPriceTypes.ONE_YEAR_PRICE -> oneYearSubPrice = it.price
                BillingPriceTypes.ONE_MONTH_PRICE -> oneMonthSubPrice = it.price
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