package ru.netfantazii.handy.data

import android.net.Uri
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Ignore
import androidx.room.Relation
import com.android.billingclient.api.SkuDetails
import com.google.firebase.auth.AuthCredential
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.R
import ru.netfantazii.handy.data.database.*
import java.util.*

class Catalog(
    id: Long = 0,
    creationTime: Calendar = Calendar.getInstance(),
    name: String = "",
    position: Int = 0,
    groupExpandStates: RecyclerViewExpandableItemManager.SavedState = RecyclerViewExpandableItemManager.SavedState(
        longArrayOf()),
    alarmTime: Calendar? = null,
    fromNetwork: Boolean = false,
    val totalProductCount: Int = 0,
    val boughtProductCount: Int = 0
) : CatalogEntity(id, creationTime, position, name, groupExpandStates, alarmTime, fromNetwork) {
    @field:Ignore
    val buyStatus: BuyStatus

    init {
        buyStatus = calculateStatus()
    }

    private fun calculateStatus(): BuyStatus {
        val isAllBought = totalProductCount == boughtProductCount
        return if (totalProductCount != 0 && isAllBought) BuyStatus.BOUGHT
        else BuyStatus.NOT_BOUGHT
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null) false
        else this.id == (other as Catalog).id
    }
}

open class Group(
    id: Long = 0,
    catalogId: Long,
    creationTime: Calendar = Calendar.getInstance(),
    name: String = "",
    groupType: GroupType = GroupType.STANDARD,
    position: Int = 1,
    expandStatus: ExpandStatus = ExpandStatus.EXPANDED,
    @Relation(entity = ProductEntity::class, parentColumn = "id", entityColumn = "group_id")
    var productList: MutableList<Product> = mutableListOf()
) : GroupEntity(id, catalogId, creationTime, groupType, position, name, expandStatus) {
    @field:Ignore
    var buyStatus: BuyStatus
    @field:Ignore
    val totalProductCount = productList.size
    @field:Ignore
    val boughtProductCount = productList.count { it.buyStatus == BuyStatus.BOUGHT }
    @field:Ignore
    val statusColor: Int


    init {
        buyStatus = calculateStatus(productList)
        statusColor =
            if (buyStatus == BuyStatus.BOUGHT) R.color.boughtColor else R.color.notBoughtColor
        productList.sortBy { it.position }

    }

    private fun calculateStatus(productEntityList: List<Product>): BuyStatus =
        if (productEntityList.isNotEmpty() && productEntityList.all { it.buyStatus == BuyStatus.BOUGHT }) BuyStatus.BOUGHT else BuyStatus.NOT_BOUGHT

    fun isStatusChanged() = buyStatus != calculateStatus(productList)

    override fun equals(other: Any?): Boolean {
        return if (other == null) false
        else this.id == (other as Group).id
    }
}

class Product(
    id: Long = 0,
    catalogId: Long,
    groupId: Long,
    creationTime: Calendar = Calendar.getInstance(),
    name: String = "",
    position: Int = 0,
    buyStatus: BuyStatus = BuyStatus.NOT_BOUGHT
) : ProductEntity(id, catalogId, groupId, creationTime, position, name, buyStatus) {

    override fun equals(other: Any?): Boolean {
        return if (other == null) false
        else this.id == (other as Product).id
    }
}

data class Contact(
    var name: String,
    var secret: String,
    val date: Calendar = Calendar.getInstance(),
    val isValid: Boolean = true
) {
    override fun toString(): String {
        return name
    }
}

data class User(
    val name: String,
    val email: String,
    val imageUri: Uri?,
    @get:Bindable
    var secret: String,
    val credential: AuthCredential,
    val placesApiKey: String
) : BaseObservable()

data class CatalogNotificationContent(
    val catalogId: Long,
    val catalogName: String,
    val groupExpandStates: RecyclerViewExpandableItemManager.SavedState = RecyclerViewExpandableItemManager.SavedState(
        longArrayOf()),
    val alarmTime: Calendar? = null,
    var geofenceEntities: List<GeofenceEntity>
)

open class ShopItem(
    val sku: String,
    val purchaseToken: String,
    val weight: Int,
    val isAcknowlodged: Boolean
) : BaseObservable()

class OneMonthSub(
    purchaseToken: String,
    val startedDate: Calendar?,
    val endDate: Calendar?,
    isAcknowledged: Boolean
) : ShopItem(SkuList.ONE_MONTH_SUB, purchaseToken,1, isAcknowledged)

class OneYearSub(
    purchaseToken: String,
    val startedDate: Calendar? = null,
    val endDate: Calendar? = null,
    isAcknowledged: Boolean
) : ShopItem(SkuList.ONE_YEAR_SUB, purchaseToken,2, isAcknowledged)

class ForeverPurchase(
    purchaseToken: String,
    val purchaseDate: Calendar? = null,
    isAcknowledged: Boolean
) :
    ShopItem(SkuList.FOREVER_PURCHASE, purchaseToken,3, isAcknowledged)

class BillingObject(val skuDetails: SkuDetails, val type: BillingPurchaseTypes)