package ru.netfantazii.handy.db

import androidx.annotation.VisibleForTesting
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
abstract class BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun add(t: T): Completable

    @Delete
    abstract fun remove(t: T): Completable

    @Update
    abstract fun update(t: T): Completable

    @Update
    abstract fun updateAll(t: List<T>): Completable

    open fun removeAndUpdateAll(t: T, list: List<T>): Completable {
        return remove(t).andThen(updateAll(list))
    }

    open fun addAndUpdateAll(t: T, list: List<T>): Completable {
        return add(t).andThen(updateAll(list))
    }
}

@Dao
abstract class CatalogDao : BaseDao<CatalogEntity>() {
    @Transaction
    @Query("SELECT c.id, c.creation_time, c.name, c.position, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id) AS totalElementCount, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id AND p.buy_status = 1) AS boughtElementCount FROM CatalogEntity c ORDER BY c.position ASC")
    abstract fun getCatalogs(): Observable<MutableList<Catalog>>

    @Query("DELETE FROM CatalogEntity")
    abstract fun removeAllCatalogs(): Completable

    @Insert
    abstract fun addAndReturnId(t: CatalogEntity): Single<Long>

    @Insert
    abstract fun addDefaultGroup(group: GroupEntity): Completable

    fun addWithDefaultGroup(catalog: CatalogEntity): Completable {
        return addAndReturnId(catalog).flatMapCompletable {
            val defaultGroup = Group(catalogId = it,
                name = "default group",
                groupType = GroupType.ALWAYS_ON_TOP,
                position = 0,
                expandStatus = ExpandStatus.EXPANDED)
            addDefaultGroup(defaultGroup)
        }
    }

    //todo попробовать запилить @transaction, чтобы не было бага в отображении
    override fun addAndUpdateAll(t: CatalogEntity, list: List<CatalogEntity>): Completable {
        return addWithDefaultGroup(t).andThen(updateAll(list))
    }
}

@Dao
abstract class GroupDao : BaseDao<GroupEntity>() {
    @Transaction
    @Query("SELECT * FROM GroupEntity WHERE catalog_id = :catalogId ORDER BY position")
    abstract fun getGroups(catalogId: Long): Observable<MutableList<Group>>

    @Query("DELETE FROM GroupEntity WHERE catalog_id = :catalogId AND group_type != 1")
    abstract fun removeAllGroups(catalogId: Long): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addAllProducts(products: List<ProductEntity>): Completable

    fun addGroupWithProductsAndUpdateAll(group: Group, groupList: List<Group>): Completable {
        return addGroupWithProducts(group).andThen(updateAll(groupList))
    }

    fun addGroupWithProducts(group: Group): Completable {
        return add(group).andThen(addAllProducts(group.productList))
    }
}

@Dao
abstract class ProductDao : BaseDao<ProductEntity>()