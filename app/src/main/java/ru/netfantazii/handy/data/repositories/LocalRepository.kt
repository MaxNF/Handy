package ru.netfantazii.handy.data.repositories

import androidx.lifecycle.LiveData
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.data.localdb.*
import ru.netfantazii.handy.data.model.Catalog
import ru.netfantazii.handy.data.model.Group
import ru.netfantazii.handy.data.model.Product
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface LocalRepository {
    fun addCatalog(catalog: Catalog): Disposable
    fun removeCatalog(catalog: Catalog): Disposable
    fun updateCatalog(catalog: Catalog): Disposable
    fun updateAllCatalogs(catalogs: List<Catalog>): Disposable
    fun removeAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable
    fun addAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable
    fun getCatalogs(): LiveData<MutableList<Catalog>>
    fun removeAllCatalogs(): Disposable
    fun updateGroupExpandStates(
        catalogId: Long,
        expandStates: RecyclerViewExpandableItemManager.SavedState
    ): Disposable

    fun addGroup(group: Group): Disposable
    fun addGroupWithProducts(group: Group): Disposable
    fun addGroupWithProductsAndUpdateAll(group: Group, list: List<Group>): Disposable
    fun removeGroup(group: Group): Disposable
    fun updateGroup(group: Group): Disposable
    fun updateAllGroups(groups: List<Group>): Disposable
    fun removeAndUpdateGroups(group: Group, list: List<Group>): Disposable
    fun removeAllGroups(groupList: List<Group>): Disposable
    fun addAndUpdateGroups(group: Group, list: List<Group>): Disposable
    fun getGroups(catalogId: Long): LiveData<MutableList<Group>>
    fun addProduct(product: Product): Disposable
    fun removeProduct(product: Product): Disposable
    fun updateProduct(product: Product): Disposable
    fun updateAllProducts(products: List<Product>): Disposable
    fun removeAndUpdateProducts(product: Product, list: List<Product>): Disposable
    fun addAndUpdateProducts(product: Product, list: List<Product>): Disposable
    fun addGeofence(geofence: GeofenceEntity): Single<Long>
    fun getGeofences(catalogId: Long): Observable<MutableList<GeofenceEntity>>
    fun removeGeofenceById(id: Long): Disposable
    fun removeAllGeofencesFromCatalog(catalogId: Long): Disposable
    fun getCatalogAlarmTime(catalogId: Long): Observable<List<Calendar>>
    fun addCatalogAlarmTime(catalogId: Long, calendar: Calendar): Disposable
    fun removeCatalogAlarmTime(catalogId: Long): Disposable
    fun addCatalogWithNetInfoAndProductsAndUpdatePositions(
        catalog: Catalog,
        groupList: List<Group>,
        catalogNetInfo: CatalogNetInfoEntity,
        catalogListToUpdate: List<Catalog>
    ): Single<Long>

    fun getCatalogNetInfo(catalogId: Long): Single<CatalogNetInfoEntity>
    fun getCatalogsSignleTime(): Single<MutableList<Catalog>>
    fun getAllGeofences(): Single<List<GeofenceEntity>>
    fun getTotalGeofenceCount(): Single<Int>
}

@Singleton
class LocalRepositoryImpl @Inject constructor(db: ProductDatabase) :
    LocalRepository {
    private val catalogDao = db.getCatalogDao()
    private val groupDao = db.getGroupDao()
    private val productDao = db.getProductDao()
    private val geofenceDao = db.getGeofenceDao()
    private val netInfoDao = db.getCatalogNetInfoDao()

    override fun addCatalog(catalog: Catalog) =
        Completable.fromRunnable { catalogDao.addWithDefaultGroup(catalog) }
            .subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeCatalog(catalog: Catalog) =
        Completable.fromRunnable { catalogDao.remove(catalog) }.subscribeOn(Schedulers.io())
            .subscribe()!!

    override fun updateCatalog(catalog: Catalog) =
        Completable.fromRunnable { catalogDao.update(catalog) }.subscribeOn(Schedulers.io())
            .subscribe()!!

    override fun updateAllCatalogs(catalogs: List<Catalog>) =
        Completable.fromRunnable { catalogDao.updateAll(catalogs) }.subscribeOn(Schedulers.io())
            .subscribe()!!

    override fun removeAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>) =
        Completable.fromRunnable { catalogDao.removeAndUpdateAll(catalog, list) }.subscribeOn(
            Schedulers.io()).subscribe()!!

    override fun addAndUpdateCatalogs(
        catalog: Catalog,
        list: List<Catalog>
    ) =
        Completable.fromRunnable { catalogDao.addCatalogAndUpdateAll(catalog, list) }.subscribeOn(
            Schedulers.io()).subscribe()!!

    override fun updateGroupExpandStates(
        catalogId: Long,
        expandStates: RecyclerViewExpandableItemManager.SavedState
    ) =
        Completable.fromRunnable {
            catalogDao.getCatalogByIdAndUpdateStates(catalogId,
                expandStates)
        }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun getCatalogs(): LiveData<MutableList<Catalog>> = catalogDao.getCatalogs()

    override fun removeAllCatalogs(): Disposable =
        catalogDao.removeAllCatalogs().subscribeOn(Schedulers.io()).subscribe()!!

    override fun addGroup(group: Group) =
        Completable.fromRunnable { groupDao.add(group) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeGroup(group: Group) =
        Completable.fromRunnable { groupDao.remove(group) }.subscribeOn(Schedulers.io())
            .subscribe()!!

    override fun updateGroup(group: Group) =
        Completable.fromRunnable { groupDao.update(group) }.subscribeOn(Schedulers.io())
            .subscribe()!!

    override fun updateAllGroups(groups: List<Group>) =
        Completable.fromRunnable { groupDao.updateAll(groups) }.subscribeOn(Schedulers.io())
            .subscribe()!!

    override fun removeAndUpdateGroups(group: Group, list: List<Group>) =
        Completable.fromRunnable {
            groupDao.removeAndUpdateAll(group,
                list)
        }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeAllGroups(groupList: List<Group>) =
        Completable.fromRunnable { groupDao.removeAllGroups(groupList) }
            .subscribeOn(Schedulers.io()).subscribe()!!


    override fun addAndUpdateGroups(group: Group, list: List<Group>) =
        Completable.fromRunnable { groupDao.addAndUpdateAll(group, list) }
            .subscribeOn(Schedulers.io()).subscribe()!!

    override fun addGroupWithProducts(group: Group): Disposable =
        Completable.fromRunnable { groupDao.addGroupWithProducts(group) }
            .subscribeOn(Schedulers.io()).subscribe()!!

    override fun addGroupWithProductsAndUpdateAll(group: Group, list: List<Group>): Disposable =
        Completable.fromRunnable {
            groupDao.addGroupWithProductsAndUpdateAll(group,
                list)
        }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun getGroups(catalogId: Long): LiveData<MutableList<Group>> =
        groupDao.getGroups(catalogId)

    override fun addProduct(product: Product) =
        Completable.fromRunnable { productDao.add(product) }.subscribeOn(Schedulers.io())
            .subscribe()!!

    override fun removeProduct(product: Product) =
        Completable.fromRunnable { productDao.remove(product) }.subscribeOn(Schedulers.io())
            .subscribe()!!

    override fun updateProduct(product: Product) =
        Completable.fromRunnable { productDao.update(product) }.subscribeOn(Schedulers.io())
            .subscribe()!!

    override fun updateAllProducts(products: List<Product>) =
        Completable.fromRunnable { productDao.updateAll(products) }.subscribeOn(Schedulers.io())
            .subscribe()!!

    override fun removeAndUpdateProducts(product: Product, list: List<Product>) =
        Completable.fromRunnable { productDao.removeAndUpdateAll(product, list) }.subscribeOn(
            Schedulers.io()).subscribe()!!

    override fun addAndUpdateProducts(product: Product, list: List<Product>) =
        Completable.fromRunnable { productDao.addAndUpdateAll(product, list) }.subscribeOn(
            Schedulers.io()).subscribe()!!

    override fun addGeofence(geofence: GeofenceEntity): Single<Long> =
        geofenceDao.addGeofenceAndGetId(geofence).subscribeOn(Schedulers.io())

    override fun getGeofences(catalogId: Long): Observable<MutableList<GeofenceEntity>> =
        geofenceDao.getGeofences(catalogId)

    override fun removeGeofenceById(id: Long): Disposable =
        geofenceDao.removeGeofenceById(id).subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeAllGeofencesFromCatalog(catalogId: Long): Disposable =
        geofenceDao.removeAllGeofencesFromCatalog(catalogId).subscribeOn(Schedulers.io())
            .subscribe()!!

    override fun getCatalogAlarmTime(catalogId: Long): Observable<List<Calendar>> =
        catalogDao.getCatalogAlarmTime(catalogId)

    override fun addCatalogAlarmTime(catalogId: Long, calendar: Calendar): Disposable =
        catalogDao.addAlarmTime(catalogId, calendar).subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeCatalogAlarmTime(catalogId: Long): Disposable =
        catalogDao.removeAlarmTime(catalogId).subscribeOn(Schedulers.io()).subscribe()

    override fun addCatalogWithNetInfoAndProductsAndUpdatePositions(
        catalog: Catalog,
        groupList: List<Group>,
        catalogNetInfo: CatalogNetInfoEntity,
        catalogListToUpdate: List<Catalog>
    ) =
        Single.fromCallable<Long> {
            catalogDao.addCatalogWithNetInfoAndProductsAndUpdatePositions(catalog,
                groupList,
                catalogNetInfo,
                catalogListToUpdate)
        }

    override fun getCatalogNetInfo(catalogId: Long): Single<CatalogNetInfoEntity> =
        netInfoDao.getCatalogNetInfo(catalogId)

    override fun getCatalogsSignleTime(): Single<MutableList<Catalog>> =
        catalogDao.getCatalogsSingleTime()

    override fun getAllGeofences(): Single<List<GeofenceEntity>> = geofenceDao.getAllGeofences()

    override fun getTotalGeofenceCount(): Single<Int> = geofenceDao.getTotalGeofenceCount()

}