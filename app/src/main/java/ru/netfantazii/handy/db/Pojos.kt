package ru.netfantazii.handy.db

import androidx.room.Ignore
import androidx.room.Relation
import ru.netfantazii.handy.R
import java.util.*

class Catalog(
    id: Long = 0,
    creationTime: Calendar = Calendar.getInstance(),
    name: String = "",
    position: Int = 0,
    val totalElementCount: Int = 0,
    val boughtElementCount: Int = 0
) : CatalogEntity(id, creationTime, position, name) {
    @field:Ignore
    val buyStatus: BuyStatus

    init {
        buyStatus = calculateStatus()
    }

    private fun calculateStatus(): BuyStatus {
        val isAllBought = totalElementCount == boughtElementCount
        return if (totalElementCount != 0 && isAllBought) BuyStatus.BOUGHT
        else BuyStatus.NOT_BOUGHT
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null) false
        else this.id == (other as Catalog).id
    }
}

class Group(
    id: Long = 0,
    catalogId: Long,
    creationTime: Calendar = Calendar.getInstance(),
    name: String = "",
    groupType: GroupType = GroupType.STANDARD,
    position: Int = 1,
    expandStatus: ExpandStatus = ExpandStatus.EXPANDED,
    @Relation(entity = ProductEntity::class, parentColumn = "id", entityColumn = "group_id")
    val productList: MutableList<Product> = mutableListOf()
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
        statusColor = if (buyStatus == BuyStatus.BOUGHT) R.color.boughtColor else R.color.notBoughtColor
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