package ru.netfantazii.handy

import ru.netfantazii.handy.model.*

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