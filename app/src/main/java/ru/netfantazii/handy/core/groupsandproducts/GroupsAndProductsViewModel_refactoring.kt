package ru.netfantazii.handy.core.groupsandproducts

import androidx.lifecycle.ViewModel
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.di.CatalogId
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class GroupsAndProductsViewModel_refactor @Inject constructor(
    private val localRepository: LocalRepository,
    @CatalogId private val currentCatalogId: Long,
    var groupExpandStates: RecyclerViewExpandableItemManager.SavedState
) : ViewModel() {
}