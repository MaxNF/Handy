package ru.netfantazii.handy.di.modules

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import dagger.Module
import dagger.Provides
import ru.netfantazii.handy.di.ApplicationContext
import ru.netfantazii.handy.di.FragmentScope

@Module
class RecyclerViewProvideModule {

    @Provides
    @FragmentScope
    fun provideGuardManager() = RecyclerViewTouchActionGuardManager()

    @Provides
    @FragmentScope
    fun provideSwipeManager() = RecyclerViewSwipeManager()

    @Provides
    @FragmentScope
    fun provideDragManager() = RecyclerViewDragDropManager()

    @Provides
    @FragmentScope
    fun provideLayoutManager(@ApplicationContext context: Context) = LinearLayoutManager(context)
}