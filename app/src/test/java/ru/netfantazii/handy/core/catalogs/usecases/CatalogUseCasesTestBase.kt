package ru.netfantazii.handy.core.catalogs.usecases

import org.junit.Before
import ru.netfantazii.handy.FakeLocalRepository
import ru.netfantazii.handy.repositories.LocalRepository

open class CatalogUseCasesTestBase {
    protected lateinit var localRepository: LocalRepository

    @Before
    fun setupRepositoryAndUseCase() {
        localRepository = FakeLocalRepository()
    }

}