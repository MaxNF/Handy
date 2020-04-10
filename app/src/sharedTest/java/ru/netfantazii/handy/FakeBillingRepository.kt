package ru.netfantazii.handy

import android.app.Activity
import com.android.billingclient.api.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.netfantazii.handy.repositories.BillingRepository
import javax.inject.Inject

class FakeBillingRepository @Inject constructor() : BillingRepository {
    override fun observePurchases(): Observable<Purchase> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun maintainConnection(): Observable<Unit> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSkuDetails(params: SkuDetailsParams): Single<List<SkuDetails>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun acknowledgePurchase(acknowledgePurchaseParams: AcknowledgePurchaseParams): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun validatePurchase(
        sku: String,
        purchaseToken: String,
        packageName: String
    ): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun queryCachedSubs(): List<Purchase> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun queryCachedPurchases(): List<Purchase> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isBillingClientReady(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun launchBillingFlow(
        activity: Activity,
        billingFlowParams: BillingFlowParams
    ): BillingResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}