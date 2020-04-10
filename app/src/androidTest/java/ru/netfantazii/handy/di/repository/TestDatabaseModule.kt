package ru.netfantazii.handy.di.repository

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ru.netfantazii.handy.data.database.ProductDatabase
import ru.netfantazii.handy.di.ApplicationContext
import javax.inject.Singleton

@Module
class TestDatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): ProductDatabase =
        Room.inMemoryDatabaseBuilder(context, ProductDatabase::class.java).build()
}