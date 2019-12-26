package ru.netfantazii.handy.db

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

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
    @Query("SELECT c.id, c.creation_time, c.name, c.position, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id) AS totalElementCount, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id AND p.buyStatus = 1) AS boughtElementCount FROM CatalogEntity c ORDER BY c.position ASC")
    abstract fun getCatalogs(): Observable<MutableList<Catalog>>

    @Query("DELETE FROM CatalogEntity")
    abstract fun removeAllCatalogs(): Completable
}

@Dao
abstract class GroupDao : BaseDao<GroupEntity>() {
    @Transaction
    @Query("SELECT * FROM GroupEntity WHERE catalog_id = :catalogId ORDER BY position")
    abstract fun getGroups(catalogId: Long): Observable<List<Group>>

    @Query("DELETE FROM GroupEntity WHERE catalog_id = :catalogId AND group_type != 1")
    abstract fun removeAllGroups(catalogId: Long): Completable
}

@Dao
abstract class ProductDao : BaseDao<ProductEntity>()