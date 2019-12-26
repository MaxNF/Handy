package ru.netfantazii.handy.core.catalogs

import androidx.annotation.UiThread
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.netfantazii.handy.FakeLocalRepository
import ru.netfantazii.handy.createFakeCatalog

@RunWith(AndroidJUnit4::class)
class CatalogsViewModelTest {
    private lateinit var localRepository: FakeLocalRepository
    private lateinit var viewModel: CatalogsViewModel
    private val catalog1 = createFakeCatalog("catalog1", 0, 1)
    private val catalog2 = createFakeCatalog("catalog2", 1, 2)


    @Before
    fun setupViewModel() {
        localRepository = FakeLocalRepository()
        localRepository.addCatalog(catalog1)
        localRepository.addCatalog(catalog2)
        viewModel = CatalogsViewModel(localRepository)
    }

    @Test
    @UiThreadTest
    fun dragCatalog_catalogPositionChanged() {
        viewModel.onCatalogDragSucceed(1, 0)
        val catalogs = localRepository.getCatalogs().test().values()[0]
        MatcherAssert.assertThat(catalogs[0].name, CoreMatchers.`is`("catalog2"))
        MatcherAssert.assertThat(catalogs[0].position, CoreMatchers.`is`(0))
        MatcherAssert.assertThat(catalogs[1].name, CoreMatchers.`is`("catalog1"))
        MatcherAssert.assertThat(catalogs[1].position, CoreMatchers.`is`(1))
    }

    @Test
    @UiThreadTest
    fun onCatalogSwipePerform_CatalogDeletedPositionChancged() {
        viewModel.onCatalogSwipePerform(catalog1)
        val catalogs = localRepository.getCatalogs().test().values()[0]
        assertThat(catalogs[0].name, `is`("catalog2"))
        assertThat(catalogs[0].id, `is`(2L))
        assertThat(catalogs[0].position, `is`(0))
    }

}