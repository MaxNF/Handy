package ru.netfantazii.handy.data.usecases.group

import ru.netfantazii.handy.data.model.BuyStatus
import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.utils.extensions.reassignPositions
import ru.netfantazii.handy.data.repositories.LocalRepository
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