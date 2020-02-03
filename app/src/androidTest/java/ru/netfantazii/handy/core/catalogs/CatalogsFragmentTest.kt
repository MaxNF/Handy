package ru.netfantazii.handy.core.catalogs

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.netfantazii.handy.FakeLocalRepository
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.ServiceLocator
import ru.netfantazii.handy.createFakeCatalog

@MediumTest
@RunWith(AndroidJUnit4::class)
class CatalogsFragmentTest {

    private lateinit var repository: FakeLocalRepository

//    private lateinit var db: ProductDatabase
//    private lateinit var productDao: ProductDao
//    private lateinit var groupDao: GroupDao
//    private lateinit var catalogDao: CatalogDao
//
//    @Before
//    fun createDb() {
//        val context: Context = ApplicationProvider.getApplicationContext<Context>()
//        db = Room.inMemoryDatabaseBuilder(context, ProductDatabase::class.java)
//            .allowMainThreadQueries().build()
//        productDao = db.getProductDao()
//        groupDao = db.getGroupDao()
//        catalogDao = db.getCatalogDao()
//    }
//
//    @After
//    @Throws(IOException::class)
//    fun closeDb() {
//        db.close()
//    }

    @Before
    fun initRepository() {
        repository = FakeLocalRepository()
        ServiceLocator.localRepository = repository
    }

    @After
    fun cleanupDb() {
        ServiceLocator.resetRepository()
    }

    @Test
    fun newCatalog_DisplayedInUi() {
        createCoolCatalogAndAddToRepo()
        launchFragmentInContainer<CatalogsFragment>(null, R.style.Crimson)
        onView(withId(R.id.tv_element_name)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_element_name)).check(matches(withText("cool catalog")))
    }

    @Test
    fun newCatalog_SwipePerformed() {
        createCoolCatalogAndAddToRepo()
        launchFragmentInContainer<CatalogsFragment>(null, R.style.Crimson)
        onView(withId(R.id.tv_element_name)).perform(ViewActions.swipeLeft()).check(doesNotExist())
        createCoolCatalogAndAddToRepo()
        onView(withId(R.id.tv_element_name)).perform(ViewActions.swipeRight()).check(doesNotExist())

    }

    private fun createCoolCatalogAndAddToRepo() {
        val newCatalog = createFakeCatalog("cool catalog", 0, 1)
        repository.addCatalog(newCatalog)
    }
}