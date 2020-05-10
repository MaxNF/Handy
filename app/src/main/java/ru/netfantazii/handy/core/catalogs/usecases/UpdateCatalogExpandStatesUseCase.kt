package ru.netfantazii.handy.core.catalogs.usecases

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class UpdateCatalogExpandStatesUseCase @Inject constructor(private val localRepository: LocalRepository) {
    fun updateCatalogExpandStates(
        catalogId: Long,
        expandStates: RecyclerViewExpandableItemManager.SavedState
    ) {
        localRepository.updateGroupExpandStates(catalogId, expandStates)
    }
}