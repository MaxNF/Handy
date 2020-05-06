package ru.netfantazii.handy.core.groupsandproducts.usecases

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.di.CatalogId
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class SubscribeToGroupsChangesUseCase @Inject constructor(localRepository: LocalRepository, @CatalogId catalogId: Long) {
    private val notFilteredGroupSource = localRepository.getGroups(catalogId)
    private val _filteredAndNotFilteredGroups = MediatorLiveData<Pair<List<Group>, List<Group>>>()
    val filteredAndNotFilteredGroups: LiveData<Pair<List<Group>, List<Group>>> =
        _filteredAndNotFilteredGroups

    init {
        startFilteringGroups()
    }

    private fun startFilteringGroups() {
        _filteredAndNotFilteredGroups.addSource(notFilteredGroupSource) { groups ->
            _filteredAndNotFilteredGroups.value = Pair(getFilteredList(groups), groups)
        }
    }

    private fun getFilteredList(groups: List<Group>): List<Group> {
        return when {
            groups.isEmpty() -> groups
            groups[0].productList.isEmpty() -> groups.slice(1..groups.lastIndex)
            else -> groups
        }
    }
}