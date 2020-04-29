package ru.netfantazii.handy.core.groupsandproducts.usecases

import android.util.Log
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.extensions.reassignPositions
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

class AddNewGroupToTheBeginningUseCase @Inject constructor(private val localRepository: LocalRepository) {

    fun addNewGroup(group: Group, groupList: MutableList<Group>) {
        groupList.add(1, group)
        groupList.reassignPositions()
        groupList.removeAt(1)
        localRepository.addAndUpdateGroups(group, groupList)
    }
}