package ru.netfantazii.handy.di.components

import dagger.Subcomponent
import ru.netfantazii.handy.core.share.ShareFragment
import ru.netfantazii.handy.di.modules.share.ShareViewModelModule

@Subcomponent(modules = [ShareViewModelModule::class])
interface ShareComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ShareComponent
    }

    fun inject(shareFragment: ShareFragment)
}