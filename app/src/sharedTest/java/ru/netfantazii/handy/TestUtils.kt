package ru.netfantazii.handy

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import ru.netfantazii.handy.data.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun createFakeCatalog(name: String = "fake catalog", position: Int = 0, id: Long = 0): Catalog {
    return Catalog(
        id = id,
        name = name,
        position = position
    )
}

fun createFakeGroup(
    catalogId: Long = 1,
    name: String = "fake group",
    position: Int = 0,
    id: Long = 0
): Group {
    return Group(
        id = id,
        catalogId = catalogId,
        name = name,
        position = position
    )
}

fun createFakeTopGroup(
    catalogId: Long = 1,
    name: String = "fakeTopGroup",
    position: Int = 0,
    id: Long = 0
): Group {
    return Group(
        id = id,
        catalogId = catalogId,
        name = name,
        position = position,
        groupType = GroupType.ALWAYS_ON_TOP
    )
}

fun createFakeProduct(
    catalogId: Long = 1,
    groupId: Long = 1,
    buyStatus: BuyStatus = BuyStatus.NOT_BOUGHT,
    name: String = "fake product",
    position: Int = 0,
    id: Long = 0
): Product {
    return Product(
        id = id,
        catalogId = catalogId,
        groupId = groupId,
        name = name,
        position = position,
        buyStatus = buyStatus
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)

    try {
        afterObserve.invoke()

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            throw TimeoutException("LiveData value was never set.")
        }

    } finally {
        this.removeObserver(observer)
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}
