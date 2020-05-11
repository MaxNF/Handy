package ru.netfantazii.handy.data.repositories

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.google.firebase.functions.FirebaseFunctions
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.netfantazii.handy.data.model.BillingException
import ru.netfantazii.handy.data.remotedb.CloudFunctions
import ru.netfantazii.handy.data.remotedb.TokenValidation
import ru.netfantazii.handy.di.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface BillingRepository {
    fun observePurchases(): Observable<Purchase>
    fun maintainConnection(): Observable<Unit>
    fun getSkuDetails(params: SkuDetailsParams): Single<List<SkuDetails>>
    fun acknowledgePurchase(acknowledgePurchaseParams: AcknowledgePurchaseParams): Completable
    fun validatePurchase(sku: String, purchaseToken: String, packageName: String): Completable
    fun queryCachedSubs(): List<Purchase>
    fun queryCachedPurchases(): List<Purchase>
    fun isBillingClientReady(): Boolean
    /**
     * Должен запускаться из UI потока.
     * */
    fun launchBillingFlow(activity: Activity, billingFlowParams: BillingFlowParams): BillingResult
}

@Singleton
class BillingRepositoryImpl @Inject constructor(@ApplicationContext val context: Context) :
    BillingRepository {
    private val firestoreHttpsEuWest1 =
        FirebaseFunctions.getInstance(CloudFunctions.REGION_EU_WEST1)
    private val billingClient: BillingClient by lazy {
        val billingClient = BillingClient.newBuilder(context).setListener(purchaseUpdatedListener)
            .enablePendingPurchases().build()
        clearGooglePlayStoreBillingCacheIfPossible(billingClient)
        billingClient
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

    override fun observePurchases(): Observable<Purchase> =
        Observable.create<List<Purchase>> { emitter ->
            purchasedAction = { list -> emitter.onNext(list) }
            errorAction = { errorCode -> emitter.onError(BillingException(
                errorCode)) }
            emitter.setCancellable {
                purchasedAction = null
                errorAction = null
            }
        }.flatMapIterable { it }

    override fun maintainConnection() = Observable.create<Unit> { emitter ->
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        emitter.onNext(Unit)
                    }
                    else -> {
                        emitter.onError(BillingException(
                            billingResult.responseCode))
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                emitter.onError(BillingException(
                    BillingException.SERVICE_DISCONNECTED))
            }
        })
    }

    override fun getSkuDetails(params: SkuDetailsParams) =
        Single.create<List<SkuDetails>> { emitter ->
            billingClient.querySkuDetailsAsync(params
            ) { billingResult, skuDetailsList ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        emitter.onSuccess(skuDetailsList)
                    }
                    else -> {
                        emitter.onError(BillingException(
                            billingResult.responseCode))
                    }
                }
            }
        }


    override fun acknowledgePurchase(acknowledgePurchaseParams: AcknowledgePurchaseParams) =
        Completable.create { emitter ->
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        emitter.onComplete()
                    }
                    else -> {
                        emitter.onError(BillingException(
                            billingResult.responseCode))
                    }
                }
            }
        }

    override fun validatePurchase(sku: String, purchaseToken: String, packageName: String) =
        Completable.create { emitter ->
            val validationData = hashMapOf(
                TokenValidation.SKU_ID_KEY to sku,
                TokenValidation.PURCHASE_TOKEN_KEY to purchaseToken,
                TokenValidation.PACKAGE_NAME_KEY to packageName)
            val task =
                firestoreHttpsEuWest1.getHttpsCallable(CloudFunctions.VALIDATE_PURCHASE_TOKEN)
                    .call(validationData)
            task.addOnSuccessListener { result ->
                val status = (result.data as HashMap<String, Int>).get("status")
                when (status) {
                    TokenValidation.RESPONSE_VALID -> {
                        emitter.onComplete()
                    }
                    TokenValidation.RESPONSE_INVALID -> {
                        emitter.onError(BillingException(
                            BillingException.ITEM_NOT_OWNED))
                    }
                    else -> {
                        emitter.onError(BillingException(
                            BillingException.UNKNOWN_ERROR_CODE))
                    }
                }
            }
        }

    override fun queryCachedSubs(): List<Purchase> =
        billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList?.toList()
            ?: listOf()


    override fun queryCachedPurchases() =
        billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList?.toList()
            ?: listOf()

    override fun isBillingClientReady() = billingClient.isReady

    /**
     * Должен запускаться из UI потока.
     * */
    override fun launchBillingFlow(
        activity: Activity,
        billingFlowParams: BillingFlowParams
    ): BillingResult {
        return billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Мифический способ, который должен (все надеются) помочь ускорить обновление кэша покупок.
     * Самостоятельно кэш гугл-плея может не обновляться долго. Помогает ли этот способ достоверно неизвестно.*/
    private fun clearGooglePlayStoreBillingCacheIfPossible(billingClient: BillingClient) {
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) { _, _ ->
        }
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.SUBS) { _, _ ->
        }
    }
}

