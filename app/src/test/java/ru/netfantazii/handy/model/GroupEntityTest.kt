package ru.netfantazii.handy.model

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import kotlin.random.Random


class GroupEntityTest {

    /**
     * Тестируем группу на корректное вычисление статуса */
    @Test
    fun groupStatusCalculationTest() {
        val notBoughtGroup1 = createGroup(createNotBoughtUnitList1(), name = "group1")
        val notBoughtGroup2 = createGroup(createNotBoughtUnitList2(), name = "group2")
        val notBoughtGroup3 = createGroup(createNotBoughtUnitList3(), name = "group3")
        val notBoughtGroup4 = createGroup(createEmptyList(), name = "group4")
        assertThat(notBoughtGroup1.buyStatus, `is`(BuyStatus.NOT_BOUGHT))
        assertThat(notBoughtGroup2.buyStatus, `is`(BuyStatus.NOT_BOUGHT))
        assertThat(notBoughtGroup3.buyStatus, `is`(BuyStatus.NOT_BOUGHT))
        assertThat(notBoughtGroup4.buyStatus, `is`(BuyStatus.NOT_BOUGHT))

        val boughtGroup1 = createGroup(createBoughtUnitList1(), name = "group1")
        val boughtGroup2 = createGroup(createBoughtUnitList2(), name = "group2")
        assertThat(boughtGroup1.buyStatus, `is`(BuyStatus.BOUGHT))
        assertThat(boughtGroup2.buyStatus, `is`(BuyStatus.BOUGHT))
    }

    private fun createProduct(buyStatus: BuyStatus): Product {
        return Product(
            0,
            1,
            1,
            name = "unit1",
            buyStatus = buyStatus,
            creationTime = Calendar.getInstance(),
            position = Random.nextInt()
        )
    }

    private fun createGroup(productList: MutableList<Product>, name: String): Group {
        return Group(0, 1, name = name, position = Random.nextInt(), productList = productList)
    }

    private fun createNotBoughtUnitList1(): MutableList<Product> {
        val product1 = createProduct(BuyStatus.NOT_BOUGHT)
        val product2 = createProduct(BuyStatus.BOUGHT)
        val product3 = createProduct(BuyStatus.BOUGHT)
        return mutableListOf(product1, product2, product3)
    }

    private fun createNotBoughtUnitList2(): MutableList<Product> {
        val product1 = createProduct(BuyStatus.NOT_BOUGHT)
        val product2 = createProduct(BuyStatus.NOT_BOUGHT)
        val product3 = createProduct(BuyStatus.BOUGHT)
        return mutableListOf(product1, product2, product3)
    }

    private fun createNotBoughtUnitList3(): MutableList<Product> {
        val product1 = createProduct(BuyStatus.NOT_BOUGHT)
        val product2 = createProduct(BuyStatus.NOT_BOUGHT)
        val product3 = createProduct(BuyStatus.NOT_BOUGHT)
        return mutableListOf(product1, product2, product3)
    }

    private fun createBoughtUnitList1(): MutableList<Product> {
        val product1 = createProduct(BuyStatus.BOUGHT)
        val product2 = createProduct(BuyStatus.BOUGHT)
        val product3 = createProduct(BuyStatus.BOUGHT)
        return mutableListOf(product1, product2, product3)
    }

    private fun createBoughtUnitList2(): MutableList<Product> {
        val product1 = createProduct(BuyStatus.BOUGHT)
        return mutableListOf(product1)
    }

    private fun createEmptyList(): MutableList<Product> {
        return mutableListOf()
    }
}