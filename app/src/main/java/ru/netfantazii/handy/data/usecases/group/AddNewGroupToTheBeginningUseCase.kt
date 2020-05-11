package ru.netfantazii.handy.data.usecases.group

import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.utils.extensions.reassignPositions
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

class AddNewGroupToTheBeginningUseCase @Inject constructor(private val localRepository: LocalRepository) {

    fun addNewGroup(group: Group, groupList: MutableList<Group>) {
        groupList.add(1, group)
        groupList.reassignPositions()
        groupList.removeAt(1)
        localRepository.addAndUpdateGroups(group, groupList)
    }
}