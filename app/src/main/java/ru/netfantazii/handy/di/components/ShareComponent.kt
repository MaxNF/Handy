package ru.netfantazii.handy.di.components

import dagger.BindsInstance
import dagger.Subcomponent
import ru.netfantazii.handy.ui.share.ShareFragment
import ru.netfantazii.handy.di.CatalogId
import ru.netfantazii.handy.di.CatalogName
import ru.netfantazii.handy.di.TotalProducts
import ru.netfantazii.handy.di.modules.share.ShareBindModule

@Subcomponent(modules = [ShareBindModule::class])
interface ShareComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance @CatalogId catalogId: Long,
            @BindsInstance @CatalogName catalogName: String,
            @BindsInstance @TotalProducts totalProducts: String
        ): ShareComponent
    }

    fun inject(shareFragment: ShareFragment)
}