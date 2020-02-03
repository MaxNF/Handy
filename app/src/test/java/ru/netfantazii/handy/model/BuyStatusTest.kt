package ru.netfantazii.handy.model

import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Test
import ru.netfantazii.handy.model.database.Converters.buyStatusToNumber
import ru.netfantazii.handy.model.database.Converters.numberToBuyStatus

/**
 * Проверяем конвертацию статусов в инт и обратно.*/
class BuyStatusTest {

    @Test
    fun statusToNumber_Test() {
        assertThat(buyStatusToNumber(BuyStatus.BOUGHT), `is`(1))
        assertThat(buyStatusToNumber(BuyStatus.NOT_BOUGHT), `is`(2))
        assertThat(buyStatusToNumber(BuyStatus.BOUGHT), not(0))
        assertThat(buyStatusToNumber(BuyStatus.BOUGHT), not(12))
        assertThat(buyStatusToNumber(BuyStatus.NOT_BOUGHT), not(0))
        assertThat(buyStatusToNumber(BuyStatus.NOT_BOUGHT), not(32))
    }

    @Test
    fun numberToStatus_Test() {
        assertThat(numberToBuyStatus(1), `is`(BuyStatus.BOUGHT))
        assertThat(numberToBuyStatus(2), `is`(BuyStatus.NOT_BOUGHT))
        assertThat(numberToBuyStatus(0), not(BuyStatus.BOUGHT))
        assertThat(numberToBuyStatus(55), not(BuyStatus.BOUGHT))
        assertThat(numberToBuyStatus(0), not(BuyStatus.NOT_BOUGHT))
        assertThat(numberToBuyStatus(7), not(BuyStatus.NOT_BOUGHT))
    }
}