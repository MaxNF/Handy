package ru.netfantazii.handy.data.usecases.group

import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.utils.extensions.moveAndReassignPositions
import ru.netfantazii.handy.utils.extensions.sliceModified
import ru.netfantazii.handy.data.repositories.LocalRepository
import javax.inject.Inject

class DragGroupUseCase @Inject constructor(private var localRepository: LocalRepository) {
    fun dragGroup(fromPosition: Int, toPosition: Int, groupList: MutableList<Group>) {
        groupList.moveAndReassignPositions(fromPosition, toPosition)
        localRepository.updateAllGroups(groupList.sliceModified(fromPosition, toPosition))
    }
}