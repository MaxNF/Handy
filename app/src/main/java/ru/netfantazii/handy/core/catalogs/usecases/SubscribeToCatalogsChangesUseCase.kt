package ru.netfantazii.handy.core.catalogs.usecases

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.data.PendingRemovedObject
import ru.netfantazii.handy.di.FragmentScope
import ru.netfantazii.handy.repositories.LocalRepository
import javax.inject.Inject

@FragmentScope
class SubscribeToCatalogsChangesUseCase @Inject constructor(
    localRepository: LocalRepository,
    private val pendingRemovedObject: PendingRemovedObject
) {

    private val notFilteredCatalogSource = localRepository.getCatalogs()
    private val _filteredAndNotFilteredCatalogs =
        MediatorLiveData<Pair<List<Catalog>, List<Catalog>>>()
    val filteredAndNotFilteredCatalogs: LiveData<Pair<List<Catalog>, List<Catalog>>>
        get() = _filteredAndNotFilteredCatalogs

    init {
        startFilteringCatalogs()
        pendingRemovedObject.addListener { initiateDataFlow() }
    }

    /**
     * Подписывает на изменения в базе данных. Список выходящих каталогов фильтруется. В лайв дату
     * помещает первым аргументом фильтрованный список, вторым - нефильтрованный.
     * */
    private fun startFilteringCatalogs() =
        _filteredAndNotFilteredCatalogs.addSource(notFilteredCatalogSource) { catalogList ->
            _filteredAndNotFilteredCatalogs.value = Pair(getFilteredList(catalogList), catalogList)
        }

    /**
     * Запускает поток данных вручную без тригера со стороны базы данных (полезно, если изменился фильтр)
     * */
    private fun initiateDataFlow() {
        _filteredAndNotFilteredCatalogs.value =
            Pair(getFilteredList(notFilteredCatalogSource.value!!),
                notFilteredCatalogSource.value!!)
    }

    private fun getFilteredList(catalogList: List<Catalog>): List<Catalog> {
        return catalogList.filterNot { catalog ->
            catalog == (pendingRemovedObject.entity as? Catalog)
        }
    }
}