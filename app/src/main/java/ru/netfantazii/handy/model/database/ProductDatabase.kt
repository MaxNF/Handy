package ru.netfantazii.handy.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CatalogEntity::class, GroupEntity::class, ProductEntity::class, GeofenceEntity::class],
    version = 1)
@TypeConverters(Converters::class)
abstract class ProductDatabase : RoomDatabase() {
    abstract fun getCatalogDao(): CatalogDao
    abstract fun getGroupDao(): GroupDao
    abstract fun getProductDao(): ProductDao
    abstract fun getGeofenceDao(): GeofenceDao
}