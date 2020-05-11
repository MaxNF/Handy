package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.ui.main.MainActivity
import ru.netfantazii.handy.di.modules.billing.BillingBindModule
import ru.netfantazii.handy.di.modules.network.NetworkBindModule

@Subcomponent(modules = [BillingBindModule::class, NetworkBindModule::class])
interface MainComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainComponent
    }

    fun inject(mainActivity: MainActivity)
}