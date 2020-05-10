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
            val removedGroupIndex = groupList.indexOf(group)
            val listForUpdating = groupList.slice(removedGroupIndex + 1..groupList.lastIndex)
            listForUpdating.shiftPositionsToLeft()
            localRepository.removeAndUpdateGroups(group, listForUpdating)
        }
        pendingRemovedObject.insertEntity(group, false)
    }
}