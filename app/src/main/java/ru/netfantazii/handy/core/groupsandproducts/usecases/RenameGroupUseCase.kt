package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class RenameGroupUseCase @Inject constructor(private val localRepository: LocalRepository) {

    fun renameGroup(group: Group) {
        localRepository.updateGroup(group)
    }
}