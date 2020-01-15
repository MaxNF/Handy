package ru.netfantazii.handy.core

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import ru.netfantazii.handy.LocalRepository
import ru.netfantazii.handy.LocalRepositoryImpl
import ru.netfantazii.handy.db.ProductDatabase

object ServiceLocator {
    private var productDatabase: ProductDatabase? = null
    @Volatile
    var localRepository: LocalRepository? = null
        @VisibleForTesting set

    private val lock = Any()

    fun provideLocalRepository(context: Context): LocalRepository {
        synchronized(this) {
            return localRepository ?: LocalRepositoryImpl(getDatabaseInstance(context))
        }
    }

    private fun getDatabaseInstance(context: Context): ProductDatabase {
        return productDatabase ?: buildDatabase(context)
    }

    private fun buildDatabase(context: Context): ProductDatabase {
        val result =
            Room.databaseBuilder(context, ProductDatabase::class.java, "Products.db").build()
        productDatabase = result
        return result
    }

    private fun buildInMemoryDatabase(context: Context): ProductDatabase {
        val result = Room.inMemoryDatabaseBuilder(context, ProductDatabase::class.java).build()
        productDatabase = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            localRepository?.removeAllCatalogs()
            productDatabase?.apply {
                clearAllTables()
                close()
            }
            productDatabase = null
            localRepository = null
        }
    }
}