package ru.netfantazii.handy.di.components

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import dagger.BindsInstance
import dagger.Subcomponent
import ru.netfantazii.handy.ui.notifications.map.MapFragment
import ru.netfantazii.handy.di.CatalogId
import ru.netfantazii.handy.di.CatalogName
import ru.netfantazii.handy.di.modules.map.MapBindModule
import ru.netfantazii.handy.di.modules.map.MapProvideModule

@Subcomponent(modules = [MapBindModule::class, MapProvideModule::class])
interface MapComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance @CatalogId catalogId: Long,
            @BindsInstance @CatalogName catalogName: String,
            @BindsInstance expandStates: RecyclerViewExpandableItemManager.SavedState
        ): MapComponent
    }

    fun inject(mapFragment: MapFragment)
}