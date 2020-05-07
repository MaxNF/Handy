package ru.netfantazii.handy

import org.junit.Before
import org.junit.Test
import ru.netfantazii.handy.repositories.LocalRepository

class FakeRepositoryTest {

    private lateinit var fakeRepository: LocalRepository

    @Before
    fun createFakeRepository() {
        fakeRepository = FakeLocalRepositoryFixed()
    }

}