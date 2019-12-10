package ru.netfantazii.handy

import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.netfantazii.handy.db.*
import java.io.IOException
import kotlin.random.Random
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private val TAG = "DatabaseTest"
    private lateinit var db: ProductDatabase
    private lateinit var productDao: ProductDao
    private lateinit var groupDao: GroupDao
    private lateinit var catalogDao: CatalogDao

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ProductDatabase::class.java)
            .allowMainThreadQueries().build()
        productDao = db.getProductDao()
        groupDao = db.getGroupDao()
        catalogDao = db.getCatalogDao()
    }

    private fun createFakeCatalog(): Catalog {
        return Catalog(
            name = "fake catalog#${Random.nextInt()}",
            position = Random.nextInt()
        )
    }

    private fun createFakeGroup(catalogId: Long): Group {
        return Group(
            catalogId = catalogId,
            name = "fake group#${Random.nextInt()}",
            position = Random.nextInt()
        )
    }

    private fun createFakeTopGroup(catalogId: Long): Group {
        return Group(
            catalogId = catalogId,
            name = "fakeTopGroup#${Random.nextInt()}",
            position = Random.nextInt(),
            groupType = GroupType.ALWAYS_ON_TOP
        )
    }

    private fun createFakeProduct(catalogId: Long, groupId: Long, buyStatus: BuyStatus): Product {
        return Product(
            catalogId = catalogId,
            groupId = groupId,
            name = "fake product#${Random.nextInt()}",
            position = Random.nextInt(),
            buyStatus = buyStatus
        )
    }

    @get:Rule
    var instantTaskExecutionRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun testEmptyCatalog() {
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        val catalogs = catalogDao.getCatalogs().test().values()[0]

        assertThat(catalogs.size, `is`(1)) // В базу добавлен только один каталог
        assertThat(catalogs[0].totalElementCount, `is`(0)) // Общее кол-во продуктов в каталоге: 0
        assertThat(
            catalogs[0].boughtElementCount,
            `is`(0)
        ) // Кол-во купленных продуктов в каталоге: 0
        assertThat(catalogs[0].buyStatus, `is`(BuyStatus.NOT_BOUGHT)) // Статус каталога: не куплен
    }

    @Test
    @Throws(IOException::class)
    fun testMultipleCatalogs() {
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        val catalogs = catalogDao.getCatalogs().test().values()[0]

        assertThat(catalogs.size, `is`(3)) // В базу добавлено 3 каталога
        assertThat(catalogs[0].id, `is`(1L)) // Первый каталог имеет id = 1
        assertThat(catalogs[1].id, `is`(2L)) // Второй каталог имеет id = 2
        assertThat(catalogs[2].id, `is`(3L)) // Третий каталог имеет id = 3
    }

    @Test
    @Throws(IOException::class)
    fun testEmptyGroup() {
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        groupDao.add(createFakeGroup(1)).test().assertComplete()
        val groups = groupDao.getGroups(1).test().values()[0]

        assertThat(groups.size, `is`(1)) // Кол-во групп в каталоге: 1
        assertThat(groups[0].buyStatus, `is`(BuyStatus.NOT_BOUGHT)) // Статус группы: не куплен
        assertThat(groups[0].productList.isEmpty(), `is`(true)) // Кол-во продуктов в группе: 0
    }

    @Test
    @Throws(IOException::class)
    fun testBoughtGroupAndCatalog() {
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        groupDao.add(createFakeGroup(1)).test().assertComplete()
        productDao.add(createFakeProduct(1, 1, BuyStatus.BOUGHT)).test().assertComplete()
        productDao.add(createFakeProduct(1, 1, BuyStatus.BOUGHT)).test().assertComplete()
        val catalogs = catalogDao.getCatalogs().test().values()[0]

        assertThat(catalogs.size, `is`(1))
        assertThat(catalogs[0].totalElementCount, `is`(2)) // Общее кол-во продуктов в каталоге: 2
        assertThat(
            catalogs[0].boughtElementCount,
            `is`(2)
        ) // Кол-во купленных продуктов в каталоге: 2
        assertThat(catalogs[0].buyStatus, `is`(BuyStatus.BOUGHT)) // Статус каталога: куплен

        val groups = groupDao.getGroups(1).test().values()[0]

        assertThat(groups.size, `is`(1)) // Кол-во групп в каталоге: 1
        assertThat(groups[0].productList.size, `is`(2)) // Кол-во продуктов в группе: 2
        assertThat(groups[0].buyStatus, `is`(BuyStatus.BOUGHT)) // Статус группы: куплен

        val firstProduct = groups[0].productList[0]
        val secondProduct = groups[0].productList[1]

        assertThat(firstProduct.buyStatus, `is`(BuyStatus.BOUGHT)) // Статус первой покупки: куплен
        assertThat(secondProduct.buyStatus, `is`(BuyStatus.BOUGHT)) // Статус второй покупки: куплен
    }

    @Test
    @Throws(IOException::class)
    fun testNotBoughtGroupAndCatalog() {
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        groupDao.add(createFakeGroup(1)).test().assertComplete()
        productDao.add(createFakeProduct(1, 1, BuyStatus.BOUGHT)).test().assertComplete()
        productDao.add(createFakeProduct(1, 1, BuyStatus.NOT_BOUGHT)).test().assertComplete()
        val catalogs = catalogDao.getCatalogs().test().values()[0]

        assertThat(catalogs.size, `is`(1)) // В базу добавлен только один каталог
        assertThat(catalogs[0].totalElementCount, `is`(2)) // Общее кол-во продуктов в каталоге: 2
        assertThat(
            catalogs[0].boughtElementCount,
            `is`(1)
        ) // Кол-во купленных продуктов в каталоге: 1
        assertThat(catalogs[0].buyStatus, `is`(BuyStatus.NOT_BOUGHT)) // Статус каталога: не куплен

        val groups = groupDao.getGroups(1).test().values()[0]
        assertThat(groups.size, `is`(1)) // Кол-во групп в каталоге: 1
        assertThat(groups[0].productList.size, `is`(2)) // Кол-во продуктов в группе: 2
        assertThat(groups[0].buyStatus, `is`(BuyStatus.NOT_BOUGHT)) // Статус группы: не куплен

        val firstProduct = groups[0].productList[0]
        val secondProduct = groups[0].productList[1]

        assertThat(firstProduct.buyStatus, `is`(BuyStatus.BOUGHT)) // Статус первой покупки: куплен
        assertThat(
            secondProduct.buyStatus,
            `is`(BuyStatus.NOT_BOUGHT)
        ) // Статус второй покупки: не куплен
    }

    @Test
    @Throws(IOException::class)
    fun testDeleteAllGroupsExceptWithAlwaysOnTopType() {
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        groupDao.add(createFakeGroup(1)).test().assertComplete()
        groupDao.add(createFakeGroup(1)).test().assertComplete()
        groupDao.add(createFakeTopGroup(1)).test().assertComplete()

        val groupsBeforeDeletion = groupDao.getGroups(1).test().values()[0]
        assertThat(groupsBeforeDeletion.size, `is`(3)) // В каталоге перед удалением 3 группы

        groupDao.deleteAllGroups(1).test().assertComplete()
        val groupsAfterDeletion = groupDao.getGroups(1).test().values()[0]
        assertThat(
            groupsAfterDeletion.size,
            `is`(1)
        ) // В группе должен остаться только один (первый продукт)
        assertThat(groupsAfterDeletion[0].groupType, `is`(GroupType.ALWAYS_ON_TOP)) // Оставшаяся группа имеет тип ALWAYS_ON_TOP
    }

    @Test
    @Throws(IOException::class)
    fun testUpdateAllCatalogs() {
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        catalogDao.add(createFakeCatalog()).test().assertComplete()

        val catalogsBeforeUpdate = catalogDao.getCatalogs().test().values()[0]
        val firstCatalogBeforeUpdate = catalogsBeforeUpdate[0]
        val secondCatalogBeforeUpdate = catalogsBeforeUpdate[1]

        firstCatalogBeforeUpdate.name = "updated1"
        secondCatalogBeforeUpdate.name = "updated2"
        val updatedCatalogs = listOf(firstCatalogBeforeUpdate, secondCatalogBeforeUpdate)
        catalogDao.updateAll(updatedCatalogs).test().assertComplete()

        val catalogsAfterUpdate = catalogDao.getCatalogs().test().values()[0]
        val firstCatalogAfterUpdate = catalogsAfterUpdate[0]
        val secondCatalogAfterUpdate = catalogsAfterUpdate[1]

        assertThat(
            firstCatalogBeforeUpdate.name,
            equalTo(firstCatalogAfterUpdate.name)
        ) // Изменение имени первого каталога прошло успешно
        assertThat(
            secondCatalogBeforeUpdate.name,
            equalTo(secondCatalogAfterUpdate.name)
        ) // Изменение имени второго каталога прошло успешно
    }

    @Test
    @Throws(IOException::class)
    fun testUpdateAllGroups() {
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        groupDao.add(createFakeGroup(1)).test().assertComplete()
        groupDao.add(createFakeGroup(1)).test().assertComplete()

        val groupsBeforeUpdate = groupDao.getGroups(1).test().values()[0]
        val group1BeforeUpdate = groupsBeforeUpdate[0]
        val group2BeforeUpdate = groupsBeforeUpdate[1]

        group1BeforeUpdate.name = "updated1"
        group2BeforeUpdate.name = "updated2"
        val updatedGroups = listOf(group1BeforeUpdate, group2BeforeUpdate)
        groupDao.updateAll(updatedGroups).test().assertComplete()

        val groupsAfterUpdate = groupDao.getGroups(1).test().values()[0]
        val group1AfterUpdate = groupsAfterUpdate[0]
        val group2AfterUpdate = groupsAfterUpdate[1]

        assertThat(
            group1BeforeUpdate.name,
            equalTo(group1AfterUpdate.name)
        ) // Изменение имени первой группы прошло успешно
        assertThat(
            group2BeforeUpdate.name,
            equalTo(group2AfterUpdate.name)
        ) // Изменение имени второй группы прошло успешно
    }

    @Test
    @Throws(IOException::class)
    fun testUpdateAllProducts() {
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        groupDao.add(createFakeGroup(1)).test().assertComplete()
        productDao.add(createFakeProduct(1, 1, BuyStatus.NOT_BOUGHT)).test().assertComplete()
        productDao.add(createFakeProduct(1, 1, BuyStatus.NOT_BOUGHT)).test().assertComplete()

        val groupBeforeUpdate = groupDao.getGroups(1).test().values()[0]
        val productsBeforeUpdate = groupBeforeUpdate[0].productList
        val product1BeforeUpdate = productsBeforeUpdate[0]
        val product2BeforeUpdate = productsBeforeUpdate[1]

        product1BeforeUpdate.name = "update1"
        product2BeforeUpdate.name = "update2"
        val updatedProducts = listOf(product1BeforeUpdate, product2BeforeUpdate)
        productDao.updateAll(updatedProducts).test().assertComplete()

        val groupsAfterUpdate = groupDao.getGroups(1).test().values()[0]
        val productsAfterUpdate = groupsAfterUpdate[0].productList
        val product1AfterUpdate = productsAfterUpdate[0]
        val product2AfterUpdate = productsAfterUpdate[1]

        assertThat(
            product1BeforeUpdate.name,
            equalTo(product1AfterUpdate.name)
        ) // Изменение имени первого продукта прошло успешно
        assertThat(
            product2BeforeUpdate.name,
            equalTo(product2AfterUpdate.name)
        ) // Изменение имени второго продукта прошло успешно
    }

    @Test
    @Throws(IOException::class)
    fun testGroupType() {
        catalogDao.add(createFakeCatalog()).test().assertComplete()
        groupDao.add(createFakeGroup(1)).test().assertComplete()
        groupDao.add(createFakeTopGroup(1)).test().assertComplete()

        val groups = groupDao.getGroups(1).test().values()[0]
        assertThat(groups[0].groupType, `is`(GroupType.STANDARD)) // Первая добавленная группа имеет стандартный тип
        assertThat(groups[1].groupType, `is`(GroupType.ALWAYS_ON_TOP)) // Вторая - ALWAYS_ON_TOP
    }

}