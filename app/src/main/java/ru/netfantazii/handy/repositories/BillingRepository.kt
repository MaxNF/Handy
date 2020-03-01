package ru.netfantazii.handy.repositories

import android.content.Context
import androidx.room.RoomOpenHelper
import com.android.billingclient.api.*
import com.google.firebase.functions.FirebaseFunctions
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.netfantazii.handy.model.BillingException
import ru.netfantazii.handy.model.database.CloudFunctions
import ru.netfantazii.handy.model.database.TokenValidation

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

    fun observePurchases(): Observable<List<Purchase>> =
        Observable.create<List<Purchase>> { emitter ->
            purchasedAction = { list -> emitter.onNext(list) }
            errorAction = { errorCode -> emitter.onError(BillingException(errorCode)) }
            emitter.setCancellable {
                purchasedAction = null
                errorAction = null
            }
        }

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

    fun validatePurchase(sku: String, purchaseToken: String, packageName: String) =
        Completable.create { emitter ->
            val validationData = hashMapOf(
                TokenValidation.SKU_ID_KEY to sku,
                TokenValidation.PURCHASE_TOKEN_KEY to purchaseToken,
                TokenValidation.PACKAGE_NAME_KEY to packageName)
            val task =
                firestoreHttpsEuWest1.getHttpsCallable(CloudFunctions.VALIDATE_PURCHASE_TOKEN)
                    .call(validationData)
            task.addOnSuccessListener { result ->
                when (result.data as String) {
                    TokenValidation.RESPONSE_VALID -> {
                        emitter.onComplete()
                    }
                    TokenValidation.RESPONSE_INVALID -> {
                        emitter.onError(BillingException(BillingException.ITEM_NOT_OWNED))
                    }
                    else -> {
                        emitter.onError(BillingException(BillingException.UNKNOWN_ERROR_CODE))
                    }
                }
            }
        }

    fun queryCachedSubs() =
        billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList.toList()

    fun queryCachedPurchases() =
        billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList.toList()
}

