package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.core.notifications.map.MapFragment
import ru.netfantazii.handy.di.modules.map.MapViewModelModule

@Subcomponent(modules = [MapViewModelModule::class])
interface MapComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MapComponent
    }

    fun inject(mapFragment: MapFragment)
}