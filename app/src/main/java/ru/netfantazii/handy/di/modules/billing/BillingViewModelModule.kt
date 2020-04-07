package ru.netfantazii.handy.di.modules.billing

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.netfantazii.handy.core.main.BillingViewModel
import ru.netfantazii.handy.di.ViewModelKey

@Module
abstract class BillingViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(BillingViewModel::class)
    abstract fun bindBillingViewModel(viewModel: BillingViewModel): ViewModel
}