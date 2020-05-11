package ru.netfantazii.handy.data.usecases.group

import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

class RenameGroupUseCase @Inject constructor(private val localRepository: LocalRepository) {

    fun renameGroup(group: Group) {
        localRepository.updateGroup(group)
    }
}