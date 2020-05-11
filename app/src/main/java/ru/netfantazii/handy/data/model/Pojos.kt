package ru.netfantazii.handy.data.model

import android.net.Uri
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.room.Ignore
import androidx.room.Relation
import com.android.billingclient.api.SkuDetails
import com.google.firebase.auth.AuthCredential
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.R
import ru.netfantazii.handy.data.localdb.*
import ru.netfantazii.handy.di.FragmentScope
import java.util.*
import javax.inject.Inject

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

    fun getCopy() = Catalog(this.id,
        this.creationTime,
        this.name,
        this.position,
        this.groupExpandStates,
        this.alarmTime,
        this.fromNetwork,
        this.totalProductCount,
        this.boughtProductCount)


    private fun calculateStatus(): BuyStatus {
        val isAllBought = totalProductCount == boughtProductCount
        return if (totalProductCount != 0 && isAllBought) BuyStatus.BOUGHT
        else BuyStatus.NOT_BOUGHT
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other.javaClass != this.javaClass) false
        else this.id == (other as Catalog).id
    }

    override fun hashCode(): Int {
        return id.hashCode()
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
        buyStatus = calculateAndReturnStatus(productList)
        statusColor =
            if (buyStatus == BuyStatus.BOUGHT) R.color.boughtColor else R.color.notBoughtColor
        productList.sortBy { it.position }

    }

    fun getCopy() = Group(
        this.id,
        this.catalogId,
        this.creationTime,
        this.name,
        this.groupType,
        this.position,
        this.expandStatus,
        this.productList
    )

    fun calculateAndReturnStatus(productEntityList: List<Product>): BuyStatus =
        if (productEntityList.isNotEmpty() && productEntityList.all { it.buyStatus == BuyStatus.BOUGHT }) BuyStatus.BOUGHT else BuyStatus.NOT_BOUGHT

    fun isStatusChanged() = buyStatus != calculateAndReturnStatus(productList)

    override fun equals(other: Any?): Boolean {
        //todo дописать проверку класса для корректного переопределения (у других классов тоже)
        return if (other == null || other.javaClass != this.javaClass) false
        else this.id == (other as Group).id
    }

    override fun hashCode(): Int {
        return id.hashCode()
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

    fun getCopy() = Product(this.id,
        this.catalogId,
        this.groupId,
        this.creationTime,
        this.name,
        this.position,
        this.buyStatus
    )

    override fun equals(other: Any?): Boolean {
        return if (other == null || other.javaClass != this.javaClass) false
        else this.id == (other as Product).id
    }

    override fun hashCode(): Int {
        return id.hashCode()
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
    val yandexMapApiKey: String
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
    val isAcknowlodged: Boolean,
    val startedDate: Calendar?,
    val endDate: Calendar?,
    val type: BillingPurchaseTypes,
    val isAutoRenewing: Boolean
) : BaseObservable()

class BillingObject(val skuDetails: SkuDetails, val type: BillingPurchaseTypes)

@FragmentScope
class PendingRemovedObject @Inject constructor() {

    private val listeners: MutableSet<() -> Unit> = mutableSetOf()

    var entity: BaseEntity? = null
        private set

    private fun triggerListeners() = listeners.forEach { it() }


    /**
     * Вставляет объект ожидающий удаления.
     * @param entity объект, который следует поместить на удаление
     * @param shouldTriggerListeners нужно ли уведомлять об этом зарегистрированных слушателей
     * */
    fun insertEntity(entity: BaseEntity, shouldTriggerListeners: Boolean) {
        this.entity = entity
        if (shouldTriggerListeners) {
            triggerListeners()
        }
    }

    /**
     * Удаляет объект ожидающий удаления.
     * @param shouldTriggerListeners нужно ли уведомлять об этом зарегистрированных слушателей
     * */
    fun clearEntity(shouldTriggerListeners: Boolean) {
        this.entity = null
        if (shouldTriggerListeners) {
            triggerListeners()
        }
    }


    /**
     * Добавляет слушателя. Уведомление придет при изменении объекта. Возвращает true, если слушатель
     * был добавлен, false, если он уже зарегистрирован.
     * @param onChanged действие, которое будет выполнено при изменении объекта. Клиент, который
     * меняет значение может отказаться от уведомления слушателей.
     * */
    fun addListener(onChanged: () -> Unit) = listeners.add(onChanged)

    /**
     * Удаляет слушателя. Возвращает true, если слушатель был удален, false, если он не был зарегистрирован
     * @param onChanged действие, которое будет выполнено при изменении объекта. Клиент, который
     * меняет значение может отказаться от уведомления слушателей.
     * */
    fun removeListener(onChanged: () -> Unit): Boolean = listeners.remove(onChanged)

    /**
     * Удаляет всех слушателей.
     */
    fun removeAllListeners() = listeners.clear()
}
