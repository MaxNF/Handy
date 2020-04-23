package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.BuyStatus
import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.data.GroupType
import ru.netfantazii.handy.extensions.moveAndReassignPositions
import ru.netfantazii.handy.extensions.sliceModified
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class CalculateAndChangeGroupPositionUseCase @Inject constructor(private val localRepository: LocalRepository) {

    fun calculateGroupPosition(group: Group, groupList: MutableList<Group>) {
        if (group.groupType != GroupType.ALWAYS_ON_TOP && group.isStatusChanged()) {
            val previousStatus = group.buyStatus
            val fromPosition = group.position
            val toPosition =
                if (previousStatus == BuyStatus.NOT_BOUGHT) groupList.lastIndex else 1 // позиция самой верхней стандартной группы (0 относится к неизменяемой группе несорт. товаров)
            groupList.moveAndReassignPositions(fromPosition, toPosition)

            val groupListForUpdating = groupList.sliceModified(fromPosition, toPosition)
            localRepository.updateAllGroups(groupListForUpdating)
        }
    }
}