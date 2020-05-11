package ru.netfantazii.handy.data.usecases.group

import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

class RemoveAllGroupsUseCase @Inject constructor(private val localRepository: LocalRepository) {
    fun removeAll(groups: List<Group>) {
        localRepository.removeAllGroups(groups)
    }
}