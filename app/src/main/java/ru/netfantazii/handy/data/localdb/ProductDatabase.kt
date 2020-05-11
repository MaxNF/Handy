package ru.netfantazii.handy.data.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CatalogEntity::class, GroupEntity::class, ProductEntity::class, GeofenceEntity::class, CatalogNetInfoEntity::class],
    version = 1)
@TypeConverters(Converters::class)
abstract class ProductDatabase : RoomDatabase() {
    abstract fun getCatalogDao(): CatalogDao
    abstract fun getGroupDao(): GroupDao
    abstract fun getProductDao(): ProductDao
    abstract fun getGeofenceDao(): GeofenceDao
    abstract fun getCatalogNetInfoDao(): CatalogNetInfoDao
}