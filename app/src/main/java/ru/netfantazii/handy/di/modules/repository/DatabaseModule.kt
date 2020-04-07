package ru.netfantazii.handy.di.modules.repository

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ru.netfantazii.handy.data.database.ProductDatabase
import ru.netfantazii.handy.di.ApplicationContext
import javax.inject.Singleton

@Module
class DatabaseModule {

    private val databaseName = "Products.db"

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): ProductDatabase =
        Room.databaseBuilder(context, ProductDatabase::class.java, databaseName).build()
}