package ru.netfantazii.handy.core.main

import android.app.Activity
import com.android.billingclient.api.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.data.*
import ru.netfantazii.handy.data.database.SkuList
import ru.netfantazii.handy.repositories.BillingRepository
import java.util.*

class BillingDataModel(
    private val billingRepository: BillingRepository,
    private val packageName: String
) {
    private val subscribeScheduler = Schedulers.io()
    private val maxRetryCount = 10L


    fun observePurchases(): Observable<ShopItem> =
        billingRepository.observePurchases()
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
        billingRepository.validatePurchase(shopItem.sku,
            shopItem.purchaseToken,
            packageName)

    private fun acknowledgePurchase(shopItem: ShopItem): Completable {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(shopItem.purchaseToken)
            .build()
        return billingRepository.acknowledgePurchase(params)
            .subscribeOn(Schedulers.io())
    }

    fun connectToBillingAndGetPrices(): Observable<List<BillingPrice>> =
        billingRepository.maintainConnection()
            .subscribeOn(Schedulers.io())
            .retry(maxRetryCount)
            .flatMap {
                requestSubPrices().zipWith(requestForeverPrice(),
                    BiFunction<List<BillingPrice>, List<BillingPrice>, List<BillingPrice>> { firstList, secondList ->
                        mutableListOf<BillingPrice>().apply {
                            addAll(firstList)
                            addAll(secondList)
                        }
                    })
                    .toObservable()
            }

    private fun requestSubPrices(): Single<List<BillingPrice>> {
        val subscriptions =
            SkuDetailsParams.newBuilder()
                .setSkusList(SkuList.SUB_SKU_LIST)
                .setType(BillingClient.SkuType.SUBS)
                .build()
        return queryPurchaseForPrice(subscriptions)
    }

    private fun requestForeverPrice(): Single<List<BillingPrice>> {
        val purchases = SkuDetailsParams
            .newBuilder()
            .setSkusList(SkuList.PURCHASE_SKU_LIST)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        return queryPurchaseForPrice(purchases)
    }

    private fun queryPurchaseForPrice(params: SkuDetailsParams) =
        billingRepository.getSkuDetails(params)
            .map { skuDetailsList ->
                skuDetailsList.map {
                    BillingPrice(it.price, getBillingItemTypeFromSku(it.sku))
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
            billingRepository.queryCachedPurchases()
                .map { transformPurchaseToShopItem(it) }
        val activeSubs =
            billingRepository.queryCachedSubs().map { transformPurchaseToShopItem(it) }
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
                OneYearSub(startedDate = purchaseDate,
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

    fun launchBillingFlow(activity: Activity, type: BillingPurchaseTypes) {
        //todo сделать
        val skuDetails: SkuDetails
        val paramsBuilder = BillingFlowParams.newBuilder()
        val params = when (type) {
            BillingPurchaseTypes.FOREVER -> {
                val skuDetails = SkuDetail
                paramsBuilder.setSkuDetails()

            }
        }
    }

    private fun getSkuDetailsForBillingFlow(params: SkuDetailsParams) =
        billingRepository.getSkuDetails(params)
            .subscribeOn(subscribeScheduler)
}