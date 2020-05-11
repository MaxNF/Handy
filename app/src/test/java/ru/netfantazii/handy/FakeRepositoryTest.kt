package ru.netfantazii.handy

import org.junit.Before
import ru.netfantazii.handy.data.repositories.LocalRepository

class FakeRepositoryTest {

    private lateinit var fakeRepository: LocalRepository

    @Before
    fun createFakeRepository() {
        fakeRepository = FakeLocalRepositoryFixed()
    }

}