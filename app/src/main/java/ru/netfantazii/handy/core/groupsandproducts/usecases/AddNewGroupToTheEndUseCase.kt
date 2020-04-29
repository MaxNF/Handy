package ru.netfantazii.handy.core.groupsandproducts.usecases

import ru.netfantazii.handy.data.BuyStatus
import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class AddNewGroupToTheEndUseCase @Inject constructor(private val localRepository: LocalRepository) {

    fun addNewGroup(group: Group, groupList: MutableList<Group>) {
        val newGroupPosition = getPositionForInsertion(groupList)
        groupList.add(newGroupPosition, group)
        groupList.reassignPositions()
        groupList.removeAt(newGroupPosition)
        localRepository.addAndUpdateGroups(group, groupList)
    }

    private fun getPositionForInsertion(groupList: List<Group>): Int {
        val listToFindGroup = groupList.subList(1, groupList.size)
        val lastNotBoughtGroup = listToFindGroup.findLast { it.buyStatus == BuyStatus.NOT_BOUGHT }
        return if (lastNotBoughtGroup != null) lastNotBoughtGroup.position + 1 else 1
    }
}