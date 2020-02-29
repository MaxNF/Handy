package ru.netfantazii.handy.core.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.model.ForeverPurchase
import ru.netfantazii.handy.model.OneMonthSub
import ru.netfantazii.handy.model.OneYearSub
import ru.netfantazii.handy.model.database.SkuList
import ru.netfantazii.handy.repositories.BillingRepository
import java.lang.UnsupportedOperationException

class BillingViewModel(private val billingRepository: BillingRepository) : ViewModel() {
    private var oneMonthSub: OneMonthSub? = null
    private var oneYearSub: OneYearSub? = null
    private var foreverProvider: ForeverPurchase? = null

    private val maxRetryCount = 10L

    private val disposables = CompositeDisposable()

    init {
        connectToBilling()
        observePurchases()
    }

    private fun observePurchases() {
        disposables.add(billingRepository.observePurchases()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                //todo
            })
    }

    private fun connectToBilling() {
        disposables.add(billingRepository.maintainConnection()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .retry(maxRetryCount)
            .subscribe {
                requestSubPrices()
                requestForeverPrice()
            })
    }

    private fun requestSubPrices() {
        val subscriptions =
            SkuDetailsParams.newBuilder()
                .setSkusList(SkuList.SUB_SKU_LIST)
                .setType(BillingClient.SkuType.SUBS)
                .build()
        queryPurchaseForDetails(subscriptions)
    }

    private fun requestForeverPrice() {
        val purchases = SkuDetailsParams
            .newBuilder()
            .setSkusList(SkuList.PURCHASE_SKU_LIST)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        queryPurchaseForDetails(purchases)
    }

    private fun queryPurchaseForDetails(params: SkuDetailsParams) {
        disposables.add(billingRepository.getSkuDetails(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .retry(maxRetryCount)
            .subscribe { skuDetailsList ->
                skuDetailsList.forEach { skuDetails -> createPurchaseObject(skuDetails) }
            })
    }

    private fun createPurchaseObject(skuDetails: SkuDetails) {
        val price = skuDetails.price
        when (skuDetails.sku) {
            SkuList.ONE_MONTH_SUB -> {
                oneMonthSub = OneMonthSub()
            }
            SkuList.ONE_YEAR_SUB -> {

            }
            SkuList.FOREVER_PURCHASE -> {

            }
            else -> {
                throw UnsupportedOperationException("Unsupported SKU type")
            }
        }
    }

    fun validateAndAcknowledgePurchase() {
        disposables.add(billingRepository.validatePurchase()
            .andThen(billingRepository.acknowledgePurchase())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                //do nothing
            }, {
                //todo варианты действий при провале валидации 1) сервер отключен / пропал интернет 2) валидация провалилась
            }))
    }
}

class BillingVmFactory(private val billingRepository: BillingRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillingViewModel::class.java)) {
            return BillingViewModel(billingRepository) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}