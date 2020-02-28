package ru.netfantazii.handy

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import ru.netfantazii.handy.model.database.ProductDatabase
import ru.netfantazii.handy.repositories.*

object ServiceLocator {
    private var productDatabase: ProductDatabase? = null
    @Volatile
    var localRepository: LocalRepository? = null
        @VisibleForTesting set

    @Volatile
    var remoteRepository: RemoteRepository? = null
        @VisibleForTesting set

    @Volatile
    var billingRepository: BillingRepository? = null
        @VisibleForTesting set

    private val lock = Any()

    fun provideLocalRepository(context: Context): LocalRepository {
        synchronized(this) {
            return localRepository
                ?: createLocalRepository(
                    context)
        }
    }

    private fun createLocalRepository(context: Context): LocalRepository {
        val localRepository = LocalRepositoryImpl(
            getDatabaseInstance(context))
        ServiceLocator.localRepository = localRepository
        return localRepository
    }

    fun provideBillingRepository(context: Context): BillingRepository {
        synchronized(this) {
            return billingRepository
                ?: createBillingRepository()
        }
    }

    private fun createBillingRepository(): BillingRepository {
        val billintRepository =
            return
    }

    fun provideRemoteRepository(): RemoteRepository {
        synchronized(this) {
            return remoteRepository
                ?: createRemoteRepository()
        }
    }

    private fun createRemoteRepository(): RemoteRepository {
        val remoteRepository = RemoteRepositoryImpl()
        ServiceLocator.remoteRepository = remoteRepository
        return remoteRepository
    }

    private fun getDatabaseInstance(context: Context): ProductDatabase {
        return productDatabase ?: buildDatabase(context)
//        return productDatabase ?: buildInMemoryDatabase(context)
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
    fun resetLocalRepository() {
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

    fun resetRemoteRepository() {
        synchronized(lock) {

        }
    }
}