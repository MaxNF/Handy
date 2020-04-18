package ru.netfantazii.handy.core.catalogs

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import ru.netfantazii.handy.HandyTestApplication
import ru.netfantazii.handy.R

@RunWith(AndroidJUnit4::class)
@MediumTest
class CatalogsFragmentTest {

    private lateinit var navController: NavController

    @Before
    fun prepareFragmentForTest() {
        navController = Mockito.mock(NavController::class.java)
    }

    @After
    fun cleanupDb() {
        val application = ApplicationProvider.getApplicationContext<HandyTestApplication>()
        application.resetRepository()
    }

    @Test
    fun clickFabButton_overlayOpened() {
        val scenario = launchFragmentInContainer<CatalogsFragment>(themeResId = R.style.Sunny)
        onView(withId(R.id.fab_create_catalog)).perform(click())
        onView(withId(R.id.overlay_background)).check(matches(isDisplayed()))
    }
}