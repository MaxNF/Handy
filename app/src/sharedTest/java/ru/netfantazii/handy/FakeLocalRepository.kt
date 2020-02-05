package ru.netfantazii.handy

import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import ru.netfantazii.handy.model.Catalog
import ru.netfantazii.handy.model.database.GeofenceEntity
import ru.netfantazii.handy.model.Group
import ru.netfantazii.handy.model.Product
import ru.netfantazii.handy.repositories.LocalRepository
import java.util.*

class FakeDisposable : Disposable {
    override fun isDisposed(): Boolean {
        return true
    }

    override fun dispose() {}
}

class FakeCompletable : Completable() {
    override fun subscribeActual(observer: CompletableObserver?) {}
}

class FakeLocalRepository : LocalRepository {

    private val catalogs: SortedSet<Catalog> =
        sortedSetOf(kotlin.Comparator { o1, o2 -> o1.position - o2.position })
    private val groups: SortedSet<Group> =
        sortedSetOf(Comparator { o1, o2 -> o1.position - o2.position })

    override fun addCatalog(catalog: Catalog): Disposable {
        catalogs.add(catalog)
        return FakeDisposable()
    }

    override fun removeCatalog(catalog: Catalog): Disposable {
        catalogs.remove(catalog)
        return FakeDisposable()
    }

    override fun updateCatalog(catalog: Catalog): Disposable {
        catalogs.remove(catalog)
        catalogs.add(catalog)
        return FakeDisposable()
    }

    override fun updateAllCatalogs(catalogs: List<Catalog>): Disposable {
        this.catalogs.removeAll(catalogs)
        this.catalogs.addAll(catalogs)
        return FakeDisposable()
    }

    override fun removeAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable {
        catalogs.remove(catalog)
        updateAllCatalogs(list)
        return FakeDisposable()
    }

    override fun addAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable {
        catalogs.add(catalog)
        updateAllCatalogs(list)
        return FakeDisposable()
    }

    override fun getCatalogs(): Observable<MutableList<Catalog>> {
        return Observable.just(catalogs.toMutableList())
    }

    override fun removeAllCatalogs(): Disposable {
        groups.clear()
        catalogs.clear()
        return FakeDisposable()
    }

    override fun updateGroupExpandStates(
        catalogId: Long,
        expandStates: RecyclerViewExpandableItemManager.SavedState
    ): Disposable {
        return FakeDisposable()
    }

    override fun addGroup(group: Group): Disposable {
        return FakeDisposable()
    }

    override fun addGroupWithProducts(group: Group): Disposable {
        return FakeDisposable()
    }

    override fun addGroupWithProductsAndUpdateAll(group: Group, list: List<Group>): Disposable {
        return FakeDisposable()
    }

    override fun removeGroup(group: Group): Disposable {
        return FakeDisposable()
    }

    override fun updateGroup(group: Group): Disposable {
        return FakeDisposable()
    }

    override fun updateAllGroups(groups: List<Group>): Disposable {
        return FakeDisposable()
    }

    override fun removeAndUpdateGroups(group: Group, list: List<Group>): Disposable {
        return FakeDisposable()
    }

    override fun removeAllGroups(groupList: List<Group>): Disposable {
        return FakeDisposable()
    }

    override fun addAndUpdateGroups(group: Group, list: List<Group>): Disposable {
        throw NotImplementedError("implement it first!")
        return FakeDisposable()
    }

    override fun getGroups(catalogId: Long): Observable<MutableList<Group>> {
        return Observable.just(groups.toMutableList())
    }

    override fun addProduct(product: Product): Disposable {
        return FakeDisposable()
    }

    override fun removeProduct(product: Product): Disposable {
        return FakeDisposable()
    }

    override fun updateProduct(product: Product): Disposable {
        return FakeDisposable()
    }

    override fun updateProductWithDelay(product: Product, delayMillis: Long): Disposable {
        return FakeDisposable()
    }

    override fun updateAllProducts(products: List<Product>): Disposable {
        return FakeDisposable()
    }

    override fun removeAndUpdateProducts(product: Product, list: List<Product>): Disposable {
        return FakeDisposable()

    }

    override fun addAndUpdateProducts(product: Product, list: List<Product>): Disposable {
        throw NotImplementedError("implement it first!")
        return FakeDisposable()
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
        return FakeDisposable()
    }

    override fun removeAllGeofencesFromCatalog(catalogId: Long): Disposable {
        return FakeDisposable()
    }

    override fun getCatalogAlarmTime(catalogId: Long): Observable<List<Calendar>> {
        return Observable.just(listOf(Calendar.getInstance()))
    }

    override fun addCatalogAlarmTime(catalogId: Long, calendar: Calendar): Disposable {
        return FakeDisposable()
    }

    override fun removeCatalogAlarmTime(catalogId: Long): Disposable {
        return FakeDisposable()
    }
}