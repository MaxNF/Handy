package ru.netfantazii.handy

import io.reactivex.Observable
import ru.netfantazii.handy.db.*

class LocalRepository(db: ProductDatabase) {
    private val catalogDao: CatalogDao = db.getCatalogDao()
    private val groupDao: GroupDao = db.getGroupDao()
    private val productDao: ProductDao = db.getProductDao()

    fun addCatalog(catalog: Catalog) = catalogDao.add(catalog).subscribe()

    fun updateCatalog(catalog: Catalog) = catalogDao.update(catalog).subscribe()

    fun updateAllCatalogs(catalogs: List<Catalog>) = catalogDao.updateAll(catalogs).subscribe()

    fun getCatalogs(): Observable<List<Catalog>> = catalogDao.getCatalogs()

    fun addGroup(group: Group) = groupDao.add(group).subscribe()

    fun updateGroup(group: Group) = groupDao.update(group).subscribe()

    fun updateAllGroups(groups: List<Group>) = groupDao.updateAll(groups).subscribe()

    fun getGroups(catalogId: Long) : Observable<List<Group>> = groupDao.getGroups(catalogId)

    fun addProduct(product: Product) = productDao.add(product).subscribe()

    fun updateProduct(product: Product) = productDao.update(product).subscribe()

    fun updateAllProducts(products: List<Product>) = productDao.updateAll(products).subscribe()
}