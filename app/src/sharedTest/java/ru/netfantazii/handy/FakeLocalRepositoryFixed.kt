package ru.netfantazii.handy

import androidx.lifecycle.LiveData
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.Group
import ru.netfantazii.handy.data.GroupType
import ru.netfantazii.handy.data.Product
import ru.netfantazii.handy.data.database.CatalogNetInfoEntity
import ru.netfantazii.handy.data.database.GeofenceEntity
import ru.netfantazii.handy.repositories.LocalRepository
import java.util.*

class FakeLocalRepositoryFixed : FakeLocalDatabase(), LocalRepository {

    override fun addCatalog(catalog: Catalog): Disposable {
        catalogs.addNotify(catalog)
        return Disposables.empty()
    }

    override fun removeCatalog(catalog: Catalog): Disposable {
        catalogs.removeNotify(catalog)
        return Disposables.empty()
    }

    override fun updateCatalog(catalog: Catalog): Disposable {
        catalogs.updateNotify(catalog)
        return Disposables.empty()
    }

    override fun updateAllCatalogs(catalogs: List<Catalog>): Disposable {
        this.catalogs.updateAllNotify(catalogs)
        return Disposables.empty()
    }

    override fun removeAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable {
        catalogs.removeNoNotify(catalog)
        catalogs.updateAllNotify(list)
        return Disposables.empty()
    }

    override fun addAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable {
        catalogs.addNoNotify(catalog)
        catalogs.updateAllNotify(list)
        return Disposables.empty()
    }

    override fun getCatalogs(): LiveData<MutableList<Catalog>> {
        return catalogsLiveData
    }

    override fun removeAllCatalogs(): Disposable {
        groups.clearNoNotify()
        catalogs.clearNotify()
        return Disposables.empty()
    }

    override fun updateGroupExpandStates(
        catalogId: Long,
        expandStates: RecyclerViewExpandableItemManager.SavedState
    ): Disposable {
        catalogs.updateExpandStatesNotify(catalogId, expandStates)
        return Disposables.empty()
    }

    override fun addGroup(group: Group): Disposable {
        groups.addNotify(group)
        return Disposables.empty()
    }

    override fun addGroupWithProducts(group: Group): Disposable {
        groups.addNoNotify(group)
        products.addAllNotify(group.productList)
        return Disposables.empty()
    }

    override fun addGroupWithProductsAndUpdateAll(group: Group, list: List<Group>): Disposable {
        groups.addNoNotify(group)
        products.addAllNoNotify(group.productList)
        groups.updateAllNotify(list)
        return Disposables.empty()
    }

    override fun removeGroup(group: Group): Disposable {
        groups.removeNotify(group)
        return Disposables.empty()
    }

    override fun updateGroup(group: Group): Disposable {
        groups.updateNotify(group)
        return Disposables.empty()
    }

    override fun updateAllGroups(groups: List<Group>): Disposable {
        this.groups.updateAllNotify(groups)
        return Disposables.empty()
    }

    override fun removeAndUpdateGroups(group: Group, list: List<Group>): Disposable {
        groups.removeNotify(group)
        groups.updateAllNotify(list)
        return Disposables.empty()
    }

    override fun removeAllGroups(groupList: List<Group>): Disposable {
        groups.removeAllNoNotify(groupList.filterNot { it.groupType == GroupType.ALWAYS_ON_TOP })
        products.removeAllNotify(products)
        return Disposables.empty()
    }

    override fun addAndUpdateGroups(group: Group, list: List<Group>): Disposable {
        groups.addNoNotify(group)
        groups.updateAllNotify(list)
        return Disposables.empty()
    }

    override fun getGroups(catalogId: Long): LiveData<MutableList<Group>> {
        return groupsLiveData
    }

    override fun addProduct(product: Product): Disposable {
        products.addNotify(product)
        return Disposables.empty()
    }

    override fun removeProduct(product: Product): Disposable {
        products.removeNotify(product)
        return Disposables.empty()
    }

    override fun updateProduct(product: Product): Disposable {
        products.updateNotify(product)
        return Disposables.empty()
    }

    override fun updateAllProducts(products: List<Product>): Disposable {
        this.products.updateAllNotify(products)
        return Disposables.empty()
    }

    override fun removeAndUpdateProducts(product: Product, list: List<Product>): Disposable {
        products.removeNoNotify(product)
        products.updateAllNotify(list)
        return Disposables.empty()
    }

    override fun addAndUpdateProducts(product: Product, list: List<Product>): Disposable {
        products.addNoNotify(product)
        products.updateAllNotify(list)
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