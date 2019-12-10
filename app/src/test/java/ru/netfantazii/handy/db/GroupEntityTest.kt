package ru.netfantazii.handy.db

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test


class GroupEntityTest {

    /**
     * Тестируем группу на корректное вычисление статуса */
    @Test
    fun groupStatusCalculationTest() {
        val notBoughtGroup1 = GroupEntity(0, 1, productEntityList = createNotBoughtUnitList1(), name = "group1")
        val notBoughtGroup2 = GroupEntity(0, 1, productEntityList = createNotBoughtUnitList2(), name = "group2")
        val notBoughtGroup3 = GroupEntity(0, 1, productEntityList = createNotBoughtUnitList3(), name = "group3")
        val notBoughtGroup4 = GroupEntity(0, 1, productEntityList = createEmptyList(), name = "group4")
        assertThat(notBoughtGroup1.status, `is`(BuyStatus.NOT_BOUGHT))
        assertThat(notBoughtGroup2.status, `is`(BuyStatus.NOT_BOUGHT))
        assertThat(notBoughtGroup3.status, `is`(BuyStatus.NOT_BOUGHT))
        assertThat(notBoughtGroup4.status, `is`(BuyStatus.NOT_BOUGHT))

        val boughtGroup1 = GroupEntity(0, 1, productEntityList = createBoughtUnitList1(), name = "group1")
        val boughtGroup2 = GroupEntity(0, 1, productEntityList = createBoughtUnitList2(), name = "group2")
        assertThat(boughtGroup1.status, `is`(BuyStatus.BOUGHT))
        assertThat(boughtGroup2.status, `is`(BuyStatus.BOUGHT))
    }

    private fun createNotBoughtUnitList1(): List<ProductEntity> {
        val product1 = ProductEntity(0, 1, 1, name = "unit1")
        val product2 = ProductEntity(0, 1, 1, name = "unit2", buyStatus = BuyStatus.BOUGHT)
        val product3 = ProductEntity(0, 1, 1, name = "unit3", buyStatus = BuyStatus.BOUGHT)
        return listOf(product1, product2, product3)
    }

    private fun createNotBoughtUnitList2(): List<ProductEntity> {
        val product1 = ProductEntity(0, 1, 1, name = "unit1")
        val product2 = ProductEntity(0, 1, 1, name = "unit2")
        val product3 = ProductEntity(0, 1, 1, name = "unit3", buyStatus = BuyStatus.BOUGHT)
        return listOf(product1, product2, product3)
    }

    private fun createNotBoughtUnitList3(): List<ProductEntity> {
        val product1 = ProductEntity(0, 1, 1, name = "unit1")
        val product2 = ProductEntity(0, 1, 1, name = "unit2")
        val product3 = ProductEntity(0, 1, 1, name = "unit3")
        return listOf(product1, product2, product3)
    }

    private fun createBoughtUnitList1(): List<ProductEntity> {
        val product1 = ProductEntity(0, 1, 1, name = "unit1", buyStatus = BuyStatus.BOUGHT)
        val product2 = ProductEntity(0, 1, 1, name = "unit2", buyStatus = BuyStatus.BOUGHT)
        val product3 = ProductEntity(0, 1, 1, name = "unit3", buyStatus = BuyStatus.BOUGHT)
        return listOf(product1, product2, product3)
    }

    private fun createBoughtUnitList2(): List<ProductEntity> {
        val product1 = ProductEntity(0, 1, 1, name = "unit1", buyStatus = BuyStatus.BOUGHT)
        return listOf(product1)
    }

    private fun createEmptyList(): List<ProductEntity> {
        return listOf<ProductEntity>()
    }
}