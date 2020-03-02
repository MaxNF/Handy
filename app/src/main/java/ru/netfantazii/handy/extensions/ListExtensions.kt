package ru.netfantazii.handy.extensions

import ru.netfantazii.handy.data.database.BaseEntity

/**
 * Передвигает элемент внутри списка с одной позиции на другую. В конце пересчитывает позиции всех элементов.*/
fun <E : BaseEntity> MutableList<E>.moveAndReassignPositions(fromPosition: Int, toPosition: Int) {
    move(fromPosition, toPosition)
    reassignPositions()
}

fun <E : BaseEntity> MutableList<E>.move(fromPosition: Int, toPosition: Int) {
    val temp = this.removeAt(fromPosition)
    this.add(toPosition, temp)
}

fun <E : BaseEntity> moveBetweenListsAndReassignPositions(
    fromList: MutableList<E>,
    fromPosition: Int,
    toList: MutableList<E>,
    toPosition: Int
) {
    moveBetweenLists(fromList, fromPosition, toList, toPosition)
    fromList.reassignPositions()
    toList.reassignPositions()
}

fun <E : BaseEntity> moveBetweenLists(
    fromList: MutableList<E>,
    fromPosition: Int,
    toList: MutableList<E>,
    toPosition: Int
) {
    val temp = fromList.removeAt(fromPosition)
    toList.add(toPosition, temp)
}

/**
 * То же что и slice. Только меньший из аргументов всегда становится первым индексом, больший - вторым.
 * Все параметры - включительно.*/
fun <E : BaseEntity> List<E>.sliceModified(index1: Int, index2: Int): List<E> {
    val lowestPosition = index1.coerceAtMost(index2)
    val highestPosition = index2.coerceAtLeast(index1)
    return this.slice(lowestPosition..highestPosition)
}

/**
 * Приравнивает поле "position" у каждого элемента к его индексу внутри списка*/
fun <E : BaseEntity> List<E>.reassignPositions() =
    this.forEachIndexed { index, e -> e.position = index }

/**
 * Уменьшает позицию на 1 у каждого элемента списка.*/
fun <E : BaseEntity> List<E>.shiftPositionsToLeft() {
    this.forEach { it.position-- }
}

/**
 * Увеличивает позицию на 1 у каждого элемента списка.
 * @param startingIndex Индекс элемента начиная с которого происходит сдвиг. Дефолтное значение 0
 * (все элементы)*/
fun <E : BaseEntity> List<E>.shiftPositionsToRight(startingIndex: Int = 0) {
    for (i in startingIndex until this.size) {
        this[i].position++
    }
}