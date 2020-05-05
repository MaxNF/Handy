package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.extensions.shiftPositionsToLeft
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class RemoveGroupUseCase @Inject constructor(
    private val pendingRemovedObject: PendingRemovedObject,
    private val localRepository: LocalRepository
) {
    fun removeGroup(group: Group, groupList: MutableList<Group>) {
        if (groupList.size == 1) {
            localRepository.removeGroup(group)
        } else {
            groupList.remove(group)
            groupList.reassignPositions()
            localRepository.removeAndUpdateGroups(group, groupList)
        }
        pendingRemovedObject.insertEntity(group, false)
    }
}