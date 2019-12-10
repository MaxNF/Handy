package ru.netfantazii.handy.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CatalogEntity::class, GroupEntity::class, ProductEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class ProductDatabase : RoomDatabase() {
    abstract fun getCatalogDao(): CatalogDao
    abstract fun getGroupDao(): GroupDao
    abstract fun getProductDao(): ProductDao

    companion object {
        @Volatile
        private var instance: ProductDatabase? = null

        //TODO заменить на настоящую базу данных
        fun getInstance(context: Context) : ProductDatabase = instance ?: synchronized(this) {
            instance ?: buildInMemoryDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, ProductDatabase::class.java, "Products.db").build()

        private fun buildInMemoryDatabase(context: Context) =
            Room.inMemoryDatabaseBuilder(context, ProductDatabase::class.java).build()
    }
}