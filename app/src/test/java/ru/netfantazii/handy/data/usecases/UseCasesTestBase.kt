package ru.netfantazii.handy.data.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Before
import org.junit.Rule
import ru.netfantazii.handy.FakeLocalRepositoryFixed
import ru.netfantazii.handy.data.repositories.LocalRepository

open class UseCasesTestBase {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    protected lateinit var localRepository: LocalRepository

    @Before
    fun setupRepositoryAndUseCase() {
        localRepository = FakeLocalRepositoryFixed()
    }

}