package ru.netfantazii.handy.model.database

import androidx.room.*
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.netfantazii.handy.extensions.registerGeofences
import ru.netfantazii.handy.model.*
import java.util.*

@Dao
abstract class BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun add(t: T)

    @Delete
    abstract fun remove(t: T)

    @Delete
    abstract fun removeAll(t: List<T>)

    @Update
    abstract fun update(t: T)

    @Update
    abstract fun updateAll(t: List<T>)

    @Transaction
    open fun removeAndUpdateAll(t: T, list: List<T>) {
        remove(t)
        updateAll(list)
    }

    @Transaction
    open fun addAndUpdateAll(t: T, list: List<T>) {
        add(t)
        updateAll(list)
    }
}

@Dao
abstract class CatalogDao : BaseDao<CatalogEntity>() {
    @Transaction
    open fun addCatalogWithNetInfoAndProductsAndUpdatePositions(
        catalog: CatalogEntity,
        groupList: List<Group>,
        catalogNetInfoEntity: CatalogNetInfoEntity,
        catalogListToUpdate: List<Catalog>
    ): Long {
        val catalogId = addAndReturnId(catalog)
        updateAllCatalogs(catalogListToUpdate)

        groupList.forEach { group ->
            group.catalogId = catalogId
            val groupId = addGroupAndReturnId(group)
            group.productList.forEach { product ->
                product.catalogId = catalogId
                product.groupId = groupId
            }
            addProductList(group.productList)
        }

        catalogNetInfoEntity.catalogId = catalogId
        addCatalogNetInfo(catalogNetInfoEntity)
        return catalogId
    }

    @Transaction
    @Query("SELECT c.id, c.creation_time, c.name, c.position, c.group_expand_states, c.alarm_time, c.from_network, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id) AS totalProductCount, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id AND p.buy_status = 1) AS boughtProductCount FROM CatalogEntity c ORDER BY c.position ASC")
    abstract fun getCatalogsSignleTime(): Single<MutableList<Catalog>>

    @Insert
    abstract fun addGroupAndReturnId(group: GroupEntity): Long

    @Insert
    abstract fun addProductList(products: List<ProductEntity>)

    @Insert
    abstract fun addCatalogNetInfo(catalogNetInfoEntity: CatalogNetInfoEntity)

    @Transaction
    @Query("SELECT c.id, c.creation_time, c.name, c.position, c.group_expand_states, c.alarm_time, c.from_network, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id) AS totalProductCount, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id AND p.buy_status = 1) AS boughtProductCount FROM CatalogEntity c ORDER BY c.position ASC")
    abstract fun getCatalogs(): Observable<MutableList<Catalog>>

    @Query("DELETE FROM CatalogEntity")
    abstract fun removeAllCatalogs(): Completable

    @Insert
    abstract fun addAndReturnId(t: CatalogEntity): Long

    @Insert
    abstract fun addDefaultGroup(group: GroupEntity)

    @Transaction
    open fun addWithDefaultGroup(catalog: CatalogEntity) {
        val catalogId = addAndReturnId(catalog)
        val defaultGroup =
            Group(catalogId = catalogId,
                name = "default group",
                groupType = GroupType.ALWAYS_ON_TOP,
                position = 0,
                expandStatus = ExpandStatus.EXPANDED)
        addDefaultGroup(defaultGroup)
    }

    @Update
    abstract fun updateAllCatalogs(list: List<CatalogEntity>)

    @Query("SELECT alarm_time FROM catalogentity WHERE id = :catalogId")
    abstract fun getCatalogAlarmTime(catalogId: Long): Observable<List<Calendar>>

    @Query("UPDATE catalogentity SET alarm_time = :calendar WHERE id = :catalogId")
    abstract fun addAlarmTime(catalogId: Long, calendar: Calendar?): Completable

    @Query("UPDATE catalogentity SET alarm_time = 0 WHERE id = :catalogId")
    abstract fun removeAlarmTime(catalogId: Long): Completable

    @Transaction
    open fun addCatalogAndUpdateAll(catalog: CatalogEntity, list: List<CatalogEntity>) {
        addWithDefaultGroup(catalog)
        updateAllCatalogs(list)
    }

    @Query("SELECT c.id, c.creation_time, c.name, c.position, c.group_expand_states, c.alarm_time, c.from_network, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id) AS totalProductCount, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id AND p.buy_status = 1) AS boughtProductCount FROM CatalogEntity c WHERE id = :id")
    abstract fun getCatalogById(id: Long): Catalog

    @Transaction
    open fun getCatalogByIdAndUpdateStates(
        id: Long,
        expandStates: RecyclerViewExpandableItemManager.SavedState
    ) {
        val catalog = getCatalogById(id)
        catalog.groupExpandStates = expandStates
        update(catalog)
    }
}

@Dao
abstract class GroupDao : BaseDao<GroupEntity>() {
    @Transaction
    @Query("SELECT * FROM GroupEntity WHERE catalog_id = :catalogId ORDER BY position")
    abstract fun getGroups(catalogId: Long): Observable<MutableList<Group>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addAllProducts(products: List<ProductEntity>): Completable

    @Transaction
    open fun addGroupWithProductsAndUpdateAll(group: Group, groupList: List<Group>) {
        addGroupWithProducts(group)
        updateAll(groupList)
    }

    @Transaction
    open fun addGroupWithProducts(group: Group) {
        add(group)
        addAllProducts(group.productList)
    }

    @Delete
    abstract fun removeGroups(groups: List<GroupEntity>)

    @Delete
    abstract fun removeProducts(products: List<ProductEntity>)

    @Transaction
    open fun removeAllGroups(groupList: List<Group>) {
        val groupsForRemoval = groupList.subList(1, groupList.size)
        val productsForRemoval = groupList[0].productList
        removeGroups(groupsForRemoval)
        removeProducts(productsForRemoval)
    }
}

@Dao
abstract class ProductDao : BaseDao<ProductEntity>()

@Dao
abstract class GeofenceDao : BaseDao<GeofenceEntity>() {

    @Query("SELECT * FROM GeofenceEntity WHERE catalog_id = :catalogId")
    abstract fun getGeofences(catalogId: Long): Observable<MutableList<GeofenceEntity>>

    @Query("DELETE FROM GeofenceEntity WHERE id = :geofenceId")
    abstract fun removeGeofenceById(geofenceId: Long): Completable

    @Query("DELETE FROM GeofenceEntity WHERE catalog_id = :catalogId")
    abstract fun removeAllGeofencesFromCatalog(catalogId: Long): Completable

    @Insert
    abstract fun addGeofenceAndGetId(geofenceEntity: GeofenceEntity): Single<Long>

    @Query("SELECT * FROM GeofenceEntity")
    abstract fun getAllGeofences(): Single<List<GeofenceEntity>>

    @Query("SELECT COUNT(id) FROM GeofenceEntity")
    abstract fun getTotalGeofenceCount(): Single<Int>
}

@Dao
abstract class CatalogNetInfoDao : BaseDao<CatalogNetInfoEntity>() {
    @Query("SELECT * FROM CatalogNetInfoEntity WHERE catalog_id = :catalogId")
    abstract fun getCatalogNetInfo(catalogId: Long): Single<CatalogNetInfoEntity>
}