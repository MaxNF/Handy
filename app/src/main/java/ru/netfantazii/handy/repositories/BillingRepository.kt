package ru.netfantazii.handy.repositories

import android.content.Context
import com.android.billingclient.api.*
import com.google.firebase.functions.FirebaseFunctions
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.netfantazii.handy.model.BillingException
import ru.netfantazii.handy.model.database.CloudFunctions
import ru.netfantazii.handy.model.database.TokenValidationResponse

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
////
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

class BillingRepository(val context: Context) {
    private val firestoreHttpsEuWest1 =
        FirebaseFunctions.getInstance(CloudFunctions.REGION_EU_WEST1)
    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context).setListener(purchaseUpdatedListener).build()
    }
    private val purchaseUpdatedListener = PurchasesUpdatedListener { billingResult, purchaseList ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchaseList != null) {
                    purchasedAction?.invoke(purchaseList)
                }
            }
            else -> {
                errorAction?.invoke(billingResult.responseCode)
            }
        }
    }
    private var purchasedAction: ((List<Purchase>) -> Unit)? = null
    private var errorAction: ((Int) -> Unit)? = null

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

    fun observePurchases(): Observable<List<Purchase>> =
        Observable.create<List<Purchase>> { emitter ->
            purchasedAction = { list -> emitter.onNext(list) }
            errorAction = { errorCode -> emitter.onError(BillingException(errorCode)) }
            emitter.setCancellable {
                purchasedAction = null
                errorAction = null
            }
        }

    fun acknowledgePurchase(acknowledgePurchaseParams: AcknowledgePurchaseParams) =
        Completable.create { emitter ->
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        emitter.onComplete()
                    }
                    else -> {
                        emitter.onError(BillingException(billingResult.responseCode))
                    }
                }
            }
        }

    fun validatePurchase(purchaseToken: String) = Completable.create { emitter ->
        val task =
            firestoreHttpsEuWest1.getHttpsCallable(CloudFunctions.VALIDATE_PURCHASE_TOKEN)
                .call(purchaseToken)
        task.addOnSuccessListener { result ->
            when (result.data as String) {
                TokenValidationResponse.VALID -> {
                    emitter.onComplete()
                }
                TokenValidationResponse.INVALID -> {
                    emitter.onError(BillingException(BillingException.ITEM_NOT_OWNED))
                }
                else -> {
                    emitter.onError(BillingException(BillingException.UNKNOWN_ERROR_CODE))
                }
            }
        }
    }
}

