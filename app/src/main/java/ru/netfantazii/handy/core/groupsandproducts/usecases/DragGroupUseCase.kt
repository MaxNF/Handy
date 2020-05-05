package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.extensions.moveAndReassignPositions
import ru.netfantazii.handy.extensions.sliceModified
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class DragGroupUseCase @Inject constructor(private var localRepository: LocalRepository) {
    fun dragGroup(fromPosition: Int, toPosition: Int, groupList: MutableList<Group>) {
        groupList.moveAndReassignPositions(fromPosition, toPosition)
        localRepository.updateAllGroups(groupList.sliceModified(fromPosition, toPosition))
    }
}