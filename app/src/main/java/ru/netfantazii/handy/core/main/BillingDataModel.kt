package ru.netfantazii.handy.core.main

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.data.*
import ru.netfantazii.handy.data.database.SkuList
import ru.netfantazii.handy.di.PackageName
import ru.netfantazii.handy.repositories.BillingRepository
import ru.netfantazii.handy.repositories.BillingRepositoryImpl
import java.util.*
import javax.inject.Inject

class BillingDataModel @Inject constructor(
    private val billingRepositoryImpl: BillingRepository,
    @PackageName private val packageName: String
) {
    private val TAG = "BillingDataModel"
    private val subscribeScheduler = Schedulers.io()
    private val maxRetryCount = 10L

    fun observePurchases(): Observable<ShopItem> =
        billingRepositoryImpl.observePurchases()
            .subscribeOn(subscribeScheduler)
            .flatMap { purchase ->
                val shopItem = transformPurchaseToShopItem(purchase)
                if (shopItem.isAcknowlodged) {
                    Observable.just(shopItem)
                } else {
                    validatePurchase(shopItem)
                        .subscribeOn(subscribeScheduler)
                        .andThen { acknowledgePurchase(shopItem) }
                        .andThen(Observable.just(shopItem))
                }
            }

    private fun validatePurchase(shopItem: ShopItem): Completable =
        billingRepositoryImpl.validatePurchase(shopItem.sku,
            shopItem.purchaseToken,
            packageName)

    private fun acknowledgePurchase(shopItem: ShopItem): Completable {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(shopItem.purchaseToken)
            .build()
        return billingRepositoryImpl.acknowledgePurchase(params)
            .subscribeOn(Schedulers.io())
    }

    fun connectToBillingAndGetPrices(): Observable<List<BillingObject>> =
        billingRepositoryImpl.maintainConnection()
            .subscribeOn(Schedulers.io())
            .retry(maxRetryCount)
            .flatMap {
                requestSubPrices().zipWith(requestForeverPrice(),
                    BiFunction<List<BillingObject>, List<BillingObject>, List<BillingObject>> { firstList, secondList ->
                        mutableListOf<BillingObject>().apply {
                            addAll(firstList)
                            addAll(secondList)
                        }
                    })
                    .toObservable()
            }

    private fun requestSubPrices(): Single<List<BillingObject>> {
        val subscriptions =
            SkuDetailsParams.newBuilder()
                .setSkusList(SkuList.SUB_SKU_LIST)
                .setType(BillingClient.SkuType.SUBS)
                .build()
        return queryPurchaseForPrice(subscriptions)
    }

    private fun requestForeverPrice(): Single<List<BillingObject>> {
        val purchases = SkuDetailsParams
            .newBuilder()
            .setSkusList(SkuList.PURCHASE_SKU_LIST)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        return queryPurchaseForPrice(purchases)
    }

    private fun queryPurchaseForPrice(params: SkuDetailsParams) =
        billingRepositoryImpl.getSkuDetails(params)
            .map { skuDetailsList ->
                skuDetailsList.map {
                    BillingObject(it, getBillingItemTypeFromSku(it.sku))
                }
            }

    private fun getBillingItemTypeFromSku(sku: String): BillingPurchaseTypes = when (sku) {
        SkuList.ONE_MONTH_SUB -> BillingPurchaseTypes.ONE_MONTH
        SkuList.ONE_YEAR_SUB -> BillingPurchaseTypes.ONE_YEAR
        SkuList.FOREVER_PURCHASE -> BillingPurchaseTypes.FOREVER
        else -> {
            throw IllegalArgumentException("Unknown sku")
        }
    }

    fun getCurrentPremiumStatus(): Single<ShopItem>? {
        val shopItem = getMostValuablePurchasedItemFromCache()
        return when {
            shopItem == null -> null
            shopItem.isAcknowlodged -> Single.just(shopItem)
            !shopItem.isAcknowlodged -> acknowledgePurchase(shopItem).toSingle { shopItem }
            else -> throw IllegalArgumentException("Something wrong with this shop item")
        }
    }

    private fun getMostValuablePurchasedItemFromCache(): ShopItem? {
        val activePurchases =
            billingRepositoryImpl.queryCachedPurchases()
                .map { transformPurchaseToShopItem(it) }
        val activeSubs =
            billingRepositoryImpl.queryCachedSubs().map { transformPurchaseToShopItem(it) }
        val activeShopItems = mutableListOf<ShopItem>().apply {
            addAll(activePurchases)
            addAll(activeSubs)
            sortByDescending { it.weight } // сортируем по важности, чтобы потом взять только первый элемент
        }
        return if (activeShopItems.isNotEmpty()) {
            // берем самый важный элемент (т.к. нет смысла устанавливать месячную подписку, если пользователь купил вечную)
            activeShopItems[0]
        } else {
            null
        }
    }

    private fun transformPurchaseToShopItem(purchase: Purchase): ShopItem {
        val purchaseDate =
            Calendar.getInstance().apply { timeInMillis = purchase.purchaseTime }
        val isAcknowledged = purchase.isAcknowledged
        val purchaseToken = purchase.purchaseToken
        val isAutoRenewing = purchase.isAutoRenewing
        return when (val sku = purchase.sku) {
            SkuList.ONE_MONTH_SUB -> {
                val endDate = Calendar.getInstance().apply {
                    time = purchaseDate.time
                    add(Calendar.MONTH, 1)
                }
                ShopItem(sku,
                    purchaseToken,
                    1,
                    isAcknowledged,
                    purchaseDate,
                    endDate,
                    BillingPurchaseTypes.ONE_MONTH,
                    isAutoRenewing)
            }
            SkuList.ONE_YEAR_SUB -> {
                val endDate = Calendar.getInstance().apply {
                    time = purchaseDate.time
                    add(Calendar.YEAR, 1)
                }
                ShopItem(sku,
                    purchaseToken,
                    2,
                    isAcknowledged,
                    purchaseDate,
                    endDate,
                    BillingPurchaseTypes.ONE_YEAR,
                    isAutoRenewing)
            }
            SkuList.FOREVER_PURCHASE -> {
                ShopItem(sku,
                    purchaseToken,
                    3,
                    isAcknowledged,
                    purchaseDate,
                    null,
                    BillingPurchaseTypes.FOREVER,
                    true)
            }
            else -> {
                throw java.lang.IllegalArgumentException("Shop item not found")
            }
        }
    }

    fun isBillingClientReady() = billingRepositoryImpl.isBillingClientReady()

    fun launchBillingFlow(activity: Activity, billingObject: BillingObject): BillingResult {
        val params = BillingFlowParams.newBuilder()
            .setSkuDetails(billingObject.skuDetails)
            .build()
        return billingRepositoryImpl.launchBillingFlow(activity, params)
    }
}