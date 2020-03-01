package ru.netfantazii.handy.core.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.model.*
import ru.netfantazii.handy.model.database.SkuList
import ru.netfantazii.handy.repositories.BillingRepository
import java.lang.UnsupportedOperationException
import java.util.*

class BillingViewModel(application: Application, private val billingRepository: BillingRepository) :
    AndroidViewModel(application) {
    private var oneMonthSubPrice: String? = null
    private var oneYearSubPrice: String? = null
    private var foreverProviderPrice: String? = null

    var currentPremium: ShopItem? = null
        private set

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
                oneMonthSubPrice = price
            }
            SkuList.ONE_YEAR_SUB -> {
                oneYearSubPrice = price
            }
            SkuList.FOREVER_PURCHASE -> {
                foreverProviderPrice = price
            }
            else -> {
                throw UnsupportedOperationException("Unsupported SKU type")
            }
        }
    }


    fun getActivePremiumStatus() {
        val activePurchases =
            billingRepository.queryCachedPurchases().map { getBoughtShopItemFromPurchase(it) }
        val activeSubs =
            billingRepository.queryCachedSubs().map { getBoughtShopItemFromPurchase(it) }
        val activeShopItems = mutableListOf<ShopItem>().apply {
            addAll(activePurchases)
            addAll(activeSubs)
            sortByDescending { it.weight } // сортируем по важности, чтобы потом взять только первый элемент
        }
        if (activeShopItems.isNotEmpty()) {
            // берем самый важный элемент (т.к. нет смысла устанавливать месячную подписку, если пользователь купил вечную)
            if (activeShopItems[0].isAcknowlodged) {
                currentPremium = activeShopItems[0]
            } else {
                validateAndAcknowledgePurchase(activeShopItems[0])
            }
        }
    }

    private fun validateAndAcknowledgePurchase(shopItem: ShopItem) {
        disposables.add(billingRepository.validatePurchase(shopItem.sku,
            shopItem.purchaseToken,
            getApplication<Application>().packageName)
            .andThen(acknowledgePurchaseAndSetPremium(shopItem))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                currentPremium = shopItem
            })
    }

    private fun acknowledgePurchaseAndSetPremium(shopItem: ShopItem): Completable {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(shopItem.purchaseToken)
            .build()
        return billingRepository.acknowledgePurchase(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .retry(maxRetryCount)
    }

    private fun getBoughtShopItemFromPurchase(purchase: Purchase): ShopItem {
        val purchaseDate =
            Calendar.getInstance().apply { timeInMillis = purchase.purchaseTime }
        val isAcknowledged = purchase.isAcknowledged
        val purchaseToken = purchase.purchaseToken
        return when (purchase.sku) {
            SkuList.ONE_MONTH_SUB -> {
                val endDate = Calendar.getInstance().apply {
                    time = purchaseDate.time
                    add(Calendar.MONTH, 1)
                }
                OneMonthSub(startedDate = purchaseDate,
                    endDate = endDate,
                    isAcknowledged = isAcknowledged,
                    purchaseToken = purchaseToken)
            }
            SkuList.ONE_YEAR_SUB -> {
                val endDate = Calendar.getInstance().apply {
                    time = purchaseDate.time
                    add(Calendar.YEAR, 1)
                }
                OneMonthSub(startedDate = purchaseDate,
                    endDate = endDate,
                    isAcknowledged = isAcknowledged,
                    purchaseToken = purchaseToken)
            }
            SkuList.FOREVER_PURCHASE -> {
                ForeverPurchase(purchaseDate = purchaseDate,
                    isAcknowledged = isAcknowledged,
                    purchaseToken = purchaseToken)
            }
            else -> {
                throw java.lang.IllegalArgumentException("Shop item not found")
            }
        }
    }
}

class BillingVmFactory(
    private val billingRepository: BillingRepository,
    private val application: Application
) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillingViewModel::class.java)) {
            return BillingViewModel(application, billingRepository) as T
        }
        throw IllegalArgumentException("Wrong ViewModel class")
    }
}