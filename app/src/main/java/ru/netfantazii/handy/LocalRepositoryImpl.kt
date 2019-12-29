package ru.netfantazii.handy

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.netfantazii.handy.db.*

interface LocalRepository {
    fun addCatalog(catalog: Catalog): Disposable
    fun removeCatalog(catalog: Catalog): Disposable
    fun updateCatalog(catalog: Catalog): Disposable
    fun updateAllCatalogs(catalogs: List<Catalog>): Disposable
    fun removeAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable
    fun addAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>): Disposable
    fun getCatalogs(): Observable<MutableList<Catalog>>
    fun removeAllCatalogs(): Disposable
    fun addGroup(group: Group): Disposable
    fun removeGroup(group: Group): Disposable
    fun updateGroup(group: Group): Disposable
    fun updateAllGroups(groups: List<Group>): Disposable
    fun removeAndUpdateGroups(group: Group, list: List<Group>): Disposable
    fun addAndUpdateGroups(group: Group, list: List<Group>): Disposable
    fun getGroups(catalogId: Long): Observable<MutableList<Group>>
    fun addProduct(product: Product): Disposable
    fun removeProduct(product: Product): Disposable
    fun updateProduct(product: Product): Disposable
    fun updateAllProducts(products: List<Product>): Disposable
    fun removeAndUpdateProducts(product: Product, list: List<Product>): Disposable
    fun addAndUpdateProducts(product: Product, list: List<Product>): Disposable
}

class LocalRepositoryImpl(db: ProductDatabase) : LocalRepository {
    private val catalogDao: CatalogDao = db.getCatalogDao()
    private val groupDao: GroupDao = db.getGroupDao()
    private val productDao: ProductDao = db.getProductDao()

    override fun addCatalog(catalog: Catalog) =
        catalogDao.add(catalog).subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeCatalog(catalog: Catalog) =
        catalogDao.remove(catalog).subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateCatalog(catalog: Catalog) =
        catalogDao.update(catalog).subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateAllCatalogs(catalogs: List<Catalog>) =
        catalogDao.updateAll(catalogs).subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeAndUpdateCatalogs(catalog: Catalog, list: List<Catalog>) =
        catalogDao.removeAndUpdateAll(catalog, list).subscribeOn(Schedulers.io()).subscribe()!!

    override fun addAndUpdateCatalogs(
        catalog: Catalog,
        list: List<Catalog>
    ) =
        catalogDao.addAndUpdateAll(catalog, list).subscribeOn(Schedulers.io()).subscribe()!!

    override fun getCatalogs(): Observable<MutableList<Catalog>> = catalogDao.getCatalogs()

    override fun removeAllCatalogs(): Disposable =
        catalogDao.removeAllCatalogs().subscribeOn(Schedulers.io()).subscribe()!!

    override fun addGroup(group: Group) = groupDao.add(group).subscribe()!!

    override fun removeGroup(group: Group) =
        groupDao.remove(group).subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateGroup(group: Group) =
        groupDao.update(group).subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateAllGroups(groups: List<Group>) =
        groupDao.updateAll(groups).subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeAndUpdateGroups(group: Group, list: List<Group>) =
        groupDao.removeAndUpdateAll(group, list).subscribeOn(Schedulers.io()).subscribe()!!

    override fun addAndUpdateGroups(group: Group, list: List<Group>) =
        groupDao.addAndUpdateAll(group, list).subscribeOn(Schedulers.io()).subscribe()!!


    override fun getGroups(catalogId: Long): Observable<MutableList<Group>> = groupDao.getGroups(catalogId)

    override fun addProduct(product: Product) =
        productDao.add(product).subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeProduct(product: Product) =
        productDao.remove(product).subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateProduct(product: Product) =
        productDao.update(product).subscribeOn(Schedulers.io()).subscribe()!!

    override fun updateAllProducts(products: List<Product>) =
        productDao.updateAll(products).subscribeOn(Schedulers.io()).subscribe()!!

    override fun removeAndUpdateProducts(product: Product, list: List<Product>) =
        productDao.removeAndUpdateAll(product, list).subscribeOn(Schedulers.io()).subscribe()!!

    override fun addAndUpdateProducts(product: Product, list: List<Product>) =
        productDao.addAndUpdateAll(product, list).subscribeOn(Schedulers.io()).subscribe()!!


}