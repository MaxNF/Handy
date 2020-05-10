package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class RemoveAllGroupsUseCase @Inject constructor(private val localRepository: LocalRepository) {
    fun removeAll(groups: List<Group>) {
        localRepository.removeAllGroups(groups)
    }
}