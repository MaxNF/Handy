package ru.netfantazii.handy

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import ru.netfantazii.handy.utils.extensions.move
import ru.netfantazii.handy.utils.extensions.moveAndReassignPositions
import ru.netfantazii.handy.utils.extensions.reassignPositions
import ru.netfantazii.handy.utils.extensions.sliceModified

class ExtensionsTest {

    @Test
    fun moveAndReassignPosition() {
        val catalog1 = createFakeCatalog("superCatalog1", 0, 1)
        val catalog2 = createFakeCatalog("superCatalog2", 1, 2)
        val catalog3 = createFakeCatalog("superCatalog3", 2, 3)
        val list = mutableListOf(catalog1, catalog2, catalog3)
        list.moveAndReassignPositions(2, 0)
        assertThat(list[0].name, `is`("superCatalog3"))
        assertThat(list[0].position, `is`(0))
        assertThat(list[1].name, `is`("superCatalog1"))
        assertThat(list[1].position, `is`(1))
        assertThat(list[2].name, `is`("superCatalog2"))
        assertThat(list[2].position, `is`(2))
    }

    @Test
    fun reassignPositionTest() {
        val catalog1 = createFakeCatalog("superCatalog1", 0, 1)
        val catalog2 = createFakeCatalog("superCatalog2", 1, 2)
        val catalog3 = createFakeCatalog("superCatalog3", 2, 3)
        val list = mutableListOf(catalog2, catalog3, catalog1)

        //initial positions
        assertThat(list[0].position, `is`(1))
        assertThat(list[1].position, `is`(2))
        assertThat(list[2].position, `is`(0))

        list.reassignPositions()

        //positions after reassigning
        assertThat(list[0].position, `is`(0))
        assertThat(list[1].position, `is`(1))
        assertThat(list[2].position, `is`(2))
    }

    @Test
    fun moveTest() {
        val catalog1 = createFakeCatalog("superCatalog1", 0, 1)
        val catalog2 = createFakeCatalog("superCatalog2", 1, 2)
        val catalog3 = createFakeCatalog("superCatalog3", 2, 3)
        val list = mutableListOf(catalog1, catalog2, catalog3)

        list.move(2, 0)
        assertThat(list[0].name, `is`("superCatalog3"))
        assertThat(list[1].name, `is`("superCatalog1"))
        assertThat(list[2].name, `is`("superCatalog2"))
    }

    @Test
    fun sublistModifiedTest() {
        val catalog1 = createFakeCatalog("superCatalog1", 0, 1)
        val catalog2 = createFakeCatalog("superCatalog2", 1, 2)
        val catalog3 = createFakeCatalog("superCatalog3", 2, 3)
        val catalog4 = createFakeCatalog("superCatalog4", 3, 4)
        val list = mutableListOf(catalog1, catalog2, catalog3, catalog4)
        val newList = list.sliceModified(3, 1)
        assertThat(newList[0], `is`(catalog2))
        assertThat(newList[1], `is`(catalog3))
        assertThat(newList[2], `is`(catalog4))
    }
}