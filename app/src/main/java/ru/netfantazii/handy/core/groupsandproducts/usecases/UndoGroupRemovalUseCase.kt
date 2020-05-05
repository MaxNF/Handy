package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.extensions.shiftPositionsToRight
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class UndoGroupRemovalUseCase @Inject constructor(
    private val pendingRemovedObject: PendingRemovedObject,
    private val localRepository: LocalRepository
) {
    fun undoGroupRemoval(groupList: MutableList<Group>) {
        pendingRemovedObject.entity?.let { group ->
            if (group !is Group) {
                throw UnsupportedOperationException("Object to be restored should be Group!")
            }

            if (groupList.isEmpty()) {
                localRepository.addGroupWithProducts(group)
            } else {
                groupList.add(group.position, group)
                groupList.reassignPositions()
                groupList.removeAt(group.position)
                localRepository.addGroupWithProductsAndUpdateAll(group, groupList)
            }
            pendingRemovedObject.clearEntity(false)
        }
    }
}