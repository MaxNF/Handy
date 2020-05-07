package ru.netfantazii.handy.core

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.netfantazii.handy.FakeLocalRepository
import ru.netfantazii.handy.FakeLocalRepositoryFixed
import ru.netfantazii.handy.repositories.LocalRepository

open class UseCasesTestBase {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    protected lateinit var localRepository: LocalRepository

    @Before
    fun setupRepositoryAndUseCase() {
        localRepository = FakeLocalRepositoryFixed()
    }

}