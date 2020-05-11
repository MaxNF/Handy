package ru.netfantazii.handy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import ru.netfantazii.handy.data.model.Catalog
import ru.netfantazii.handy.data.localdb.GeofenceEntity
import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.Product
import ru.netfantazii.handy.data.localdb.BaseEntity
import ru.netfantazii.handy.data.localdb.CatalogNetInfoEntity
import ru.netfantazii.handy.data.repositories.LocalRepository
import java.util.*
import javax.inject.Inject

class FakeLocalRepository @Inject constructor() : LocalRepository {

    private val catalogs: SortedSet<Catalog> =
        // Сэт сортируется по позиции, т.к. у настоящей бд запрашиваемые данные имеют такую же сортировку
        sortedSetOf(Comparator { o1, o2 -> (o1.id.toInt() - o2.id.toInt()) })
    private val groups: SortedSet<Group> =
        sortedSetOf(Comparator { o1, o2 -> (o1.id.toInt() - o2.id.toInt()) })
    private val products: SortedSet<Product> =
        sortedSetOf(Comparator { o1, o2 -> (o1.id.toInt() - o2.id.toInt()) })


    private var catalogsLiveData = MutableLiveData<MutableList<Catalog>>()
    private var groupsLiveData = MutableLiveData<MutableList<Group>>()

    private fun <T : BaseEntity> assignNewIdAndReturn(t: T, set: SortedSet<T>): T {
        val maxId = set.maxBy { it.id }?.id ?: 0
        t.id = maxId + 1
        return t
    }

    fun resetRepository() {
        catalogs.clear()
        groups.clear()
        catalogsLiveData = MutableLiveData()
    }

    private fun notifyCatalogsObservers() {
        catalogsLiveData.value = catalogs.toMutableList()
    }

    private fun notifyGroupsObservers() {
        groupsLiveData.value = groupsLiveData.value
    }

    override fun addCatalog(catalog: Catalog): Disposable {
        val updatedCatalog = assignNewIdAndReturn(catalog, catalogs)
        catalogs.add(updatedCatalog)
        notifyCatalogsObservers()
        return Disposables.empty()
    }

    override fun removeCatalog(catalog: Catalog): Disposable {
        catalogs.remove(catalog)
        notifyCatalogsObservers()
        return Disposables.empty()
    }

    override fun updateCatalog(catalog: Catalog): Disposable {
        if (catalogs.contains(catalog)) {
            catalogs.remove(catalog)
            catalogs.add(catalog)
        }
        notifyCatalogsObservers()
        return Disposables.empty()
    }

    override fun updateAllCatalogs(catalogs: List<Catalog>): Disposable {
        catalogs.forEach {
            updateCatalog(it)
        }
        notifyCatalogsObservers()
        return Disposables.empty()
    }

    override fun removeAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable {
        catalogs.remove(catalog)
        updateAllCatalogs(list)
        notifyCatalogsObservers()
        return Disposables.empty()
    }

    override fun addAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable {
        addCatalog(catalog)
        updateAllCatalogs(list)
        notifyCatalogsObservers()
        return Disposables.empty()
    }

    override fun getCatalogs(): LiveData<MutableList<Catalog>> {
        val copiedCatalogs = mutableListOf<Catalog>()
        catalogs.forEach { originalCatalog ->
            copiedCatalogs.add(originalCatalog.getCopy())
        }
        copiedCatalogs.sortBy { it.position }
        catalogsLiveData.value = copiedCatalogs
        return catalogsLiveData
    }

    override fun removeAllCatalogs(): Disposable {
        groups.clear()
        catalogs.clear()
        notifyCatalogsObservers()
        return Disposables.empty()
    }

    override fun updateGroupExpandStates(
        catalogId: Long,
        expandStates: RecyclerViewExpandableItemManager.SavedState
    ): Disposable {
        val catalog = catalogs.find { it.id == catalogId }
        catalog?.let {
            it.groupExpandStates = expandStates
            updateCatalog(it)
            notifyCatalogsObservers()
        }
        return Disposables.empty()
    }

    override fun addGroup(group: Group): Disposable {
        val updatedGroup = assignNewIdAndReturn(group, groups)
        groups.add(updatedGroup)
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun addGroupWithProducts(group: Group): Disposable {
        addGroup(group)
        addAllProducts(group.productList)
        notifyGroupsObservers()
        return Disposables.empty()
    }

    private fun addAllProducts(list: List<Product>) {
        list.forEach {
            addProduct(it)
        }
    }

    override fun addGroupWithProductsAndUpdateAll(group: Group, list: List<Group>): Disposable {
        addGroupWithProducts(group)
        updateAllGroups(list)
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun removeGroup(group: Group): Disposable {
        groups.remove(group)
        products.removeIf { group.id == it.groupId }
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun updateGroup(group: Group): Disposable {
        if (groups.contains(group)) {
            groups.remove(group)
            groups.add(group)
        }
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun updateAllGroups(groups: List<Group>): Disposable {
        for (group in groups) {
            updateGroup(group)
        }
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun removeAndUpdateGroups(group: Group, list: List<Group>): Disposable {
        removeGroup(group)
        updateAllGroups(list)
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun removeAllGroups(groupList: List<Group>): Disposable {
        groups.removeAll(groupList)
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun addAndUpdateGroups(group: Group, list: List<Group>): Disposable {
        addGroup(group)
        updateAllGroups(list)
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun getGroups(catalogId: Long): LiveData<MutableList<Group>> {
        // создаем новый список копированием (для групп важно симулировать работу Room)
        // в фейковом репозитории отдаются все группы независимо от полученного ИД каталога
        groupsLiveData.value = getPopulatedGroups()
        return groupsLiveData
    }

    private fun getPopulatedGroups(): MutableList<Group> {
        val copiedGroupList = mutableListOf<Group>()
        groups.forEach { originalGroup ->
            originalGroup.productList = getProductsForGroup(originalGroup.id)
            val copiedGroup = originalGroup.getCopy()
            copiedGroupList.add(copiedGroup)
        }
        copiedGroupList.sortBy { it.position }
        return copiedGroupList
    }

    private fun getProductsForGroup(groupId: Long): MutableList<Product> {
        val copiedProductList = mutableListOf<Product>()
        products.forEach {
            if (it.groupId == groupId) {
                copiedProductList.add(it.getCopy())
            }
        }
        copiedProductList.sortBy { it.position }
        return copiedProductList
    }

    override fun addProduct(product: Product): Disposable {
        val productWithId = assignNewIdAndReturn(product, products)
        products.add(productWithId)
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun removeProduct(product: Product): Disposable {
        products.remove(product)
        return Disposables.empty()
    }

    override fun updateProduct(product: Product): Disposable {
        if (products.contains(product)) {
            products.remove(product)
            products.add(product)
            notifyGroupsObservers()
        }
        return Disposables.empty()
    }

    override fun updateAllProducts(products: List<Product>): Disposable {
        val newList = mutableListOf<Product>()
        newList.addAll(products)
        newList.forEach { updateProduct(it) }
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun removeAndUpdateProducts(product: Product, list: List<Product>): Disposable {
        removeProduct(product)
        updateAllProducts(list)
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun addAndUpdateProducts(product: Product, list: List<Product>): Disposable {
        addProduct(product)
        updateAllProducts(list)
        notifyGroupsObservers()
        return Disposables.empty()
    }

    override fun addGeofence(geofence: GeofenceEntity): Single<Long> {
        return object : Single<Long>() {
            override fun subscribeActual(observer: SingleObserver<in Long>) {
//
            }
        }
    }

    override fun getGeofences(catalogId: Long): Observable<MutableList<GeofenceEntity>> {
        return Observable.just(mutableListOf())
    }

    override fun removeGeofenceById(id: Long): Disposable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAllGeofencesFromCatalog(catalogId: Long): Disposable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCatalogAlarmTime(catalogId: Long): Observable<List<Calendar>> {
        return Observable.just(listOf(Calendar.getInstance()))
    }

    override fun addCatalogAlarmTime(catalogId: Long, calendar: Calendar): Disposable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeCatalogAlarmTime(catalogId: Long): Disposable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addCatalogWithNetInfoAndProductsAndUpdatePositions(
        catalog: Catalog,
        groupList: List<Group>,
        catalogNetInfo: CatalogNetInfoEntity,
        catalogListToUpdate: List<Catalog>
    ): Single<Long> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCatalogNetInfo(catalogId: Long): Single<CatalogNetInfoEntity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCatalogsSignleTime(): Single<MutableList<Catalog>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllGeofences(): Single<List<GeofenceEntity>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTotalGeofenceCount(): Single<Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}