package ru.netfantazii.handy.data.usecases.catalog

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

class UpdateCatalogExpandStatesUseCase @Inject constructor(private val localRepository: LocalRepository) {
    fun updateCatalogExpandStates(
        catalogId: Long,
        expandStates: RecyclerViewExpandableItemManager.SavedState
    ) {
        localRepository.updateGroupExpandStates(catalogId, expandStates)
    }
}