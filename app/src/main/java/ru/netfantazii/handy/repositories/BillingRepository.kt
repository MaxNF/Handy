package ru.netfantazii.handy.repositories

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.netfantazii.handy.model.BillingException

//
//class BillingRepository(private val context: Context) : PurchasesUpdatedListener,
//    BillingClientStateListener,
//    SkuDetailsResponseListener {
//    private object Sku {
//        //todo заменить ID
//        val monthSub = "month"
//        val yearSub = "year"
//        val foreverPurchase = "forever"
//
//        val subList = listOf(monthSub, yearSub)
//        val purchaseList = listOf(foreverPurchase)
//    }
//
//    private lateinit var billingClient: BillingClient
//
//    fun initBillingClient() {
//        billingClient = BillingClient.newBuilder(context).setListener(this).build()
//        connectToBilling()
//    }
//
//    private fun connectToBilling() {
//        billingClient.startConnection(this)
//    }
//
//    override fun onPurchasesUpdated(
//        billingResult: BillingResult,
//        purchases: MutableList<Purchase>?
//    ) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun onBillingServiceDisconnected() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun onBillingSetupFinished(billingResult: BillingResult) {
//        requestSubPrices(Sku.subList)
//        requestForeverPrice(Sku.purchaseList)
//    }
//
//    override fun onSkuDetailsResponse(
//        billingResult: BillingResult,
//        skuDetailsList: MutableList<SkuDetails>
//    ) {
//        when (billingResult.responseCode) {
//            BillingClient.BillingResponseCode.OK -> {
//                skuDetailsList.forEach { skuDetail ->
//                    loadPriceInMemory(skuDetail.sku, skuDetail.price)
//                }
//            }
//            else -> {
//
//            }
//        }
//    }
//
//    private fun loadPriceInMemory(sku: String, price: String) {
//        when (sku) {
//            Sku.monthSub -> {
//
//            }
//            Sku.yearSub -> {
//
//            }
//            Sku.foreverPurchase -> {
//
//            }
//        }
//
//    }
//
//    private fun requestSubPrices(subList: List<String>) {
//        val subscriptions =
//            SkuDetailsParams.newBuilder()
//                .setSkusList(subList)
//                .setType(BillingClient.SkuType.SUBS)
//                .build()
//        billingClient.querySkuDetailsAsync(subscriptions, this)
//    }
//
//    private fun requestForeverPrice(purchaseList: List<String>) {
//        val purchases = SkuDetailsParams
//            .newBuilder()
//            .setSkusList(purchaseList)
//            .setType(BillingClient.SkuType.INAPP)
//            .build()
//        billingClient.querySkuDetailsAsync(purchases, this)
//    }
//}

class BillingRepository(context: Context) {
    private val billingClient by lazy { BillingClient.newBuilder(context).build() }


    fun maintainConnection() = Observable.create<Unit> { emitter ->
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        emitter.onNext(Unit)
                    }
                    else -> {
                        emitter.onError(BillingException(billingResult.responseCode))
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                emitter.onError(BillingException(BillingException.SERVICE_DISCONNECTED))
            }
        })
        emitter.setCancellable { billingClient.endConnection() } //todo может заменить на disposable? посмотреть разницу
    }

    fun getSkuDetails(params: SkuDetailsParams) = Single.create<List<SkuDetails>> { emitter ->
        billingClient.querySkuDetailsAsync(params
        ) { billingResult, skuDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    emitter.onSuccess(skuDetailsList)
                }
                else -> {
                    emitter.onError(BillingException(billingResult.responseCode))
                }
            }
        }
    }

    fun getPurchaseUpdates() = Observable.create<List<Purchase>> { emitter ->
        PurchasesUpdatedListener { billingResult, purchaseList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (purchaseList != null) {
                        emitter.onNext(purchaseList)
                    }
                }
                else -> {
                    emitter.onError(BillingException(billingResult.responseCode))
                }
            }
        }
    }
}

