package ru.netfantazii.handy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.data.model.Catalog
import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.Product
import ru.netfantazii.handy.data.localdb.BaseEntity
import java.util.*
import kotlin.Comparator
import kotlin.reflect.KClass

open class FakeLocalDatabase {

    protected val catalogs: SortedSet<Catalog> =
        // Сэт сортируется по позиции, т.к. у настоящей бд запрашиваемые данные имеют такую же сортировку
        sortedSetOf(Comparator { o1, o2 -> (o1.id.toInt() - o2.id.toInt()) })
    protected val groups: SortedSet<Group> =
        sortedSetOf(Comparator { o1, o2 -> (o1.id.toInt() - o2.id.toInt()) })
    protected val products: SortedSet<Product> =
        sortedSetOf(Comparator { o1, o2 -> (o1.id.toInt() - o2.id.toInt()) })

    private val _catalogsLiveData = MutableLiveData<MutableList<Catalog>>()
    protected val catalogsLiveData: LiveData<MutableList<Catalog>>
        get() {
            notifyObservers(Catalog::class)
            return _catalogsLiveData
        }
    private val _groupsLiveData = MutableLiveData<MutableList<Group>>()
    protected val groupsLiveData: LiveData<MutableList<Group>>
        get() {
            notifyObservers(Group::class)
            return _groupsLiveData
        }

    fun <T : BaseEntity> assignNewIdAndReturn(t: T, set: SortedSet<T>): T {
        val maxId = set.maxBy { it.id }?.id ?: 0
        t.id = maxId + 1
        return t
    }

    protected inline fun <reified T : BaseEntity> SortedSet<T>.addNotify(entity: T) {
        this.addNoNotify(entity)
        notifyObservers(T::class)
    }

    protected inline fun <reified T : BaseEntity> SortedSet<T>.addAllNotify(collection: Collection<T>) {
        this.addAllNoNotify(collection)
        notifyObservers(T::class)
    }

    protected inline fun <reified T : BaseEntity> SortedSet<T>.removeNotify(entity: T) {
        if (this.removeNoNotify(entity)) {
            notifyObservers(T::class)
        }
    }

    protected inline fun <reified T : BaseEntity> SortedSet<T>.clearNotify() {
        this.clearNoNotify()
        notifyObservers(T::class)
    }

    protected inline fun <reified T : BaseEntity> SortedSet<T>.updateNotify(entity: T) {
        if (this.updateNoNotify(entity)) {
            notifyObservers(T::class)
        }
    }

    protected inline fun <reified T : BaseEntity> SortedSet<T>.updateAllNotify(collection: Collection<T>) {
        if (this.updateAllNoNotify(collection)) {
            notifyObservers(T::class)
        }
    }

    protected inline fun <reified T : BaseEntity> SortedSet<T>.removeAllNotify(collection: Collection<T>) {
        if (this.removeAllNoNotify(collection)) {
            notifyObservers(T::class)
        }
    }

    protected fun SortedSet<Catalog>.updateExpandStatesNotify(
        catalogId: Long,
        expandStates: RecyclerViewExpandableItemManager.SavedState
    ) {
        if (this.updateExpandStatesNoNotify(catalogId, expandStates)) {
            notifyObservers(Catalog::class)
        }
    }

    protected fun <T : BaseEntity> SortedSet<T>.addNoNotify(entity: T) {
        this.add(assignNewIdAndReturn(entity, this))
    }


    protected fun <T : BaseEntity> SortedSet<T>.addAllNoNotify(collection: Collection<T>) {
        collection.forEach {
            val entity = assignNewIdAndReturn(it, this)
            this.add(entity)
        }
    }

    protected fun <T : BaseEntity> SortedSet<T>.removeNoNotify(entity: T): Boolean {
        return this.remove(entity)
    }

    protected fun <T : BaseEntity> SortedSet<T>.clearNoNotify() {
        this.clear()
    }

    protected fun <T : BaseEntity> SortedSet<T>.removeAllNoNotify(collection: Collection<T>): Boolean {
        return this.removeAll(collection)
    }

    protected fun <T : BaseEntity> SortedSet<T>.updateNoNotify(entity: T): Boolean {
        if (this.contains(entity)) {
            this.remove(entity)
            this.add(entity)
            return true
        }
        return false
    }

    protected fun <T : BaseEntity> SortedSet<T>.updateAllNoNotify(collection: Collection<T>): Boolean {
        var isUpdated = false
        collection.forEach { entity ->
            if (this.contains(entity)) {
                this.remove(entity)
                this.add(entity)
                isUpdated = true
            }
        }
        return isUpdated
    }

    protected fun SortedSet<Catalog>.updateExpandStatesNoNotify(
        catalogId: Long,
        expandStates: RecyclerViewExpandableItemManager.SavedState
    ): Boolean {
        var isUpdated = false
        catalogs.find { it.id == catalogId }?.let { catalog ->
            catalog.groupExpandStates = expandStates
            isUpdated = true
        }
        return isUpdated
    }

    protected fun notifyObservers(clazz: KClass<out Any>) {
        if (clazz == Catalog::class) {
            _catalogsLiveData.value = getCatalogsCopySortedByPosition(catalogs)
        } else {
            _groupsLiveData.value = getPopulatedGroupsCopySortedByPosition(groups, products)
        }
    }

    private fun getCatalogsCopySortedByPosition(catalogs: Collection<Catalog>): MutableList<Catalog> {
        val list = mutableListOf<Catalog>()
        catalogs.forEach { originalCatalog ->
            list.add(originalCatalog.getCopy())
        }
        list.sortBy { it.position }
        return list
    }

    private fun getPopulatedGroupsCopySortedByPosition(
        groups: Collection<Group>,
        products: Collection<Product>
    ): MutableList<Group> {
        val list = mutableListOf<Group>()
        groups.forEach { originalGroup ->
            val copiedProducts = getProductsCopySortedByPosition(products, originalGroup.id)
            val newGroup = Group(
                originalGroup.id,
                originalGroup.catalogId,
                originalGroup.creationTime,
                originalGroup.name,
                originalGroup.groupType,
                originalGroup.position,
                originalGroup.expandStatus,
                copiedProducts)
            list.add(newGroup)
        }
        list.sortBy { it.position }
        return list
    }

    private fun getProductsCopySortedByPosition(
        products: Collection<Product>,
        groupId: Long
    ): MutableList<Product> {
        val list = mutableListOf<Product>()
        products.filter { it.groupId == groupId }.forEach { product ->
            list.add(product.getCopy())
        }
        list.sortBy { it.position }
        return list
    }
}