package ru.netfantazii.handy.db

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import org.jetbrains.annotations.TestOnly

@Dao
interface BaseDao<T> {
    @Insert
    fun add(t: T) : Completable

    @Delete
    fun remove(t: T) : Completable

    @Update
    fun update(t: T) : Completable

    @Update
    fun updateAll(t: List<T>) : Completable
}

@Dao
interface CatalogDao : BaseDao<CatalogEntity> {
    @Transaction
    @Query("SELECT c.id, c.creation_time, c.name, c.position, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id) AS totalElementCount, (SELECT COUNT(id) FROM ProductEntity p WHERE p.catalog_id = c.id AND p.buyStatus = 1) AS boughtElementCount FROM CatalogEntity c")
    fun getCatalogs(): Observable<List<Catalog>>

}

@Dao
interface GroupDao : BaseDao<GroupEntity> {
    @Transaction
    @Query("SELECT * FROM GroupEntity WHERE catalog_id = :catalogId")
    fun getGroups(catalogId: Long) : Observable<List<Group>>

    @Query("DELETE FROM GroupEntity WHERE catalog_id = :catalogId AND group_type != 1")
    fun deleteAllGroups(catalogId: Long) : Completable

}

@Dao
interface ProductDao : BaseDao<ProductEntity>