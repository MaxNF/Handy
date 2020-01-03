package ru.netfantazii.handy.core.groupsandproducts

import org.junit.Before
import ru.netfantazii.handy.FakeLocalRepository
import ru.netfantazii.handy.createFakeGroup

class GroupsAndProductsViewModelTest {
    private lateinit var localRepository: FakeLocalRepository
    private lateinit var viewModel: GroupsAndProductsViewModel
    private val group1 = createFakeGroup(1)
    private val group2 = createFakeGroup(1)

    @Before
    fun setupViewModel() {
        localRepository = FakeLocalRepository()
        localRepository.addGroup(group1)
        localRepository.addGroup(group2)
        viewModel = GroupsAndProductsViewModel(localRepository, 1)
    }
}