package ru.netfantazii.handy.di.components

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import dagger.BindsInstance
import dagger.Subcomponent
import ru.netfantazii.handy.core.notifications.alarm.AlarmFragment
import ru.netfantazii.handy.di.CatalogId
import ru.netfantazii.handy.di.CatalogName
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.di.modules.alarm.AlarmBindModule
import ru.netfantazii.handy.di.modules.alarm.AlarmProvideModule

@FragmentScope
@Subcomponent(modules = [AlarmProvideModule::class, AlarmBindModule::class])
interface AlarmComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance @CatalogId catalogId: Long, @BindsInstance @CatalogName catalogName: String,
            @BindsInstance expandStates: RecyclerViewExpandableItemManager.SavedState
        ): AlarmComponent
    }

    fun inject(alarmFragment: AlarmFragment)
}