package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.core.main.MainActivity
import ru.netfantazii.handy.di.modules.billing.BillingViewModelModule
import ru.netfantazii.handy.di.modules.network.NetworkViewModelModule

@Subcomponent(modules = [BillingViewModelModule::class, NetworkViewModelModule::class])
interface MainComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainComponent
    }

    fun inject(mainActivity: MainActivity)
}