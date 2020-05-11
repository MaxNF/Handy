package ru.netfantazii.handy.data.usecases.group

import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.PendingRemovedObject
import ru.netfantazii.handy.utils.extensions.reassignPositions
import ru.netfantazii.handy.data.repositories.LocalRepository
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