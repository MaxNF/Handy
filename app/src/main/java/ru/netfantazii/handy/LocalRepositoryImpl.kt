package ru.netfantazii.handy

import androidx.room.RxRoom
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.db.*
import java.util.concurrent.TimeUnit

interface LocalRepository {
    fun addCatalog(catalog: Catalog): Disposable
    fun removeCatalog(catalog: Catalog): Disposable
    fun updateCatalog(catalog: Catalog): Disposable
    fun updateAllCatalogs(catalogs: List<Catalog>): Disposable
    fun removeAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable
    fun addAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable
    fun getCatalogs(): Observable<MutableList<Catalog>>
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
    fun getGroups(catalogId: Long): Observable<MutableList<Group>>
    fun addProduct(product: Product): Disposable
    fun removeProduct(product: Product): Disposable
    fun updateProduct(product: Product): Disposable
    fun updateProductWithDelay(product: Product, delayMillis: Long): Disposable
    fun updateAllProducts(products: List<Product>): Disposable
    fun removeAndUpdateProducts(product: Product, list: List<Product>): Disposable
    fun addAndUpdateProducts(product: Product, list: List<Product>): Disposable
}

class LocalRepositoryImpl(db: ProductDatabase) : LocalRepository {
    private val catalogDao: CatalogDao = db.getCatalogDao()
    private val groupDao: GroupDao = db.getGroupDao()
    private val productDao: ProductDao = db.getProductDao()

    override fun addCatalog(catalog: Catalog) =
        Completable.fromRunnable { catalogDao.addWithDefaultGroup(catalog) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeCatalog(catalog: Catalog) =
        Completable.fromRunnable { catalogDao.remove(catalog) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateCatalog(catalog: Catalog) =
        Completable.fromRunnable { catalogDao.update(catalog) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateAllCatalogs(catalogs: List<Catalog>) =
        Completable.fromRunnable { catalogDao.updateAll(catalogs) }.subscribeOn(Schedulers.io()).subscribe()!!

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

    override fun getCatalogs(): Observable<MutableList<Catalog>> = catalogDao.getCatalogs()

    override fun removeAllCatalogs(): Disposable =
        catalogDao.removeAllCatalogs().subscribeOn(Schedulers.io()).subscribe()!!

    override fun addGroup(group: Group) =
        Completable.fromRunnable { groupDao.add(group) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeGroup(group: Group) =
        Completable.fromRunnable { groupDao.remove(group) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateGroup(group: Group) =
        Completable.fromRunnable { groupDao.update(group) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateAllGroups(groups: List<Group>) =
        Completable.fromRunnable { groupDao.updateAll(groups) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeAndUpdateGroups(group: Group, list: List<Group>) =
        Completable.fromRunnable {
            groupDao.removeAndUpdateAll(group,
                list)
        }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeAllGroups(groupList: List<Group>) =
        Completable.fromRunnable { groupDao.removeAllGroups(groupList) }.subscribeOn(Schedulers.io()).subscribe()!!


    override fun addAndUpdateGroups(group: Group, list: List<Group>) =
        Completable.fromRunnable {
            groupDao.addAndUpdateAll(group,
                list)
        }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun addGroupWithProducts(group: Group): Disposable =
        Completable.fromRunnable { groupDao.addGroupWithProducts(group) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun addGroupWithProductsAndUpdateAll(group: Group, list: List<Group>): Disposable =
        Completable.fromRunnable {
            groupDao.addGroupWithProductsAndUpdateAll(group,
                list)
        }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun getGroups(catalogId: Long): Observable<MutableList<Group>> =
        groupDao.getGroups(catalogId)

    override fun addProduct(product: Product) =
        Completable.fromRunnable { productDao.add(product) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeProduct(product: Product) =
        Completable.fromRunnable { productDao.remove(product) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateProduct(product: Product) =
        Completable.fromRunnable { productDao.update(product) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateProductWithDelay(product: Product, delayMillis: Long) =
        Completable.fromRunnable { productDao.update(product) }.subscribeOn(Schedulers.io()).delaySubscription(
            delayMillis,
            TimeUnit.MILLISECONDS).subscribe()!!

    override fun updateAllProducts(products: List<Product>) =
        Completable.fromRunnable { productDao.updateAll(products) }.subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeAndUpdateProducts(product: Product, list: List<Product>) =
        Completable.fromRunnable { productDao.removeAndUpdateAll(product, list) }.subscribeOn(
            Schedulers.io()).subscribe()!!

    override fun addAndUpdateProducts(product: Product, list: List<Product>) =
        Completable.fromRunnable { productDao.addAndUpdateAll(product, list) }.subscribeOn(
            Schedulers.io()).subscribe()!!
}