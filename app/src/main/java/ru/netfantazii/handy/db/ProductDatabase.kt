package ru.netfantazii.handy.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CatalogEntity::class, GroupEntity::class, ProductEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class ProductDatabase : RoomDatabase() {
    abstract fun getCatalogDao(): CatalogDao
    abstract fun getGroupDao(): GroupDao
    abstract fun getProductDao(): ProductDao
}