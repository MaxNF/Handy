package ru.netfantazii.handy.ui.catalogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.leinardi.android.speeddial.SpeedDialView
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.R
import ru.netfantazii.handy.ui.base.BaseFragment
import ru.netfantazii.handy.utils.ThemeColor
import ru.netfantazii.handy.utils.getThemeColor
import ru.netfantazii.handy.data.model.Catalog
import ru.netfantazii.handy.databinding.CatalogsFragmentBinding
import ru.netfantazii.handy.di.ViewModelFactory
import ru.netfantazii.handy.di.components.CatalogsComponent
import ru.netfantazii.handy.di.modules.catalogs.CatalogsProvideModule
import ru.netfantazii.handy.utils.extensions.injectViewModel
import javax.inject.Inject

class CatalogsFragment : BaseFragment<CatalogsAdapter>() {
    private val TAG = "CatalogsFragment"

    private lateinit var component: CatalogsComponent
    @Inject
    lateinit var factory: ViewModelFactory

    @Inject
    lateinit var viewModel: CatalogsViewModel
    private lateinit var undoSnackbar: Snackbar

    override fun injectDependencies() {
        component =
            (context!!.applicationContext as HandyApplication).appComponent.catalogsComponent()
                .create(CatalogsProvideModule(this))
        component.inject(this)
        viewModel = injectViewModel(factory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = CatalogsFragmentBinding.inflate(inflater, container, false)
        binding.isPremium = (activity!!.application as HandyApplication).isPremium
        return binding.root
    }

    override fun createRecyclerView(view: View) {
        super.createRecyclerView(view)
        guardManager.attachRecyclerView(recyclerView)
        swipeManager.attachRecyclerView(recyclerView)
        dragManager.attachRecyclerView(recyclerView)
    }

    override fun createSnackbars(view: View) {
        val parent = view.findViewById<ConstraintLayout>(R.id.constraint_layout)
        undoSnackbar = Snackbar.make(
            parent,
            getString(R.string.catalog_undo_label),
            Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo_action)) { viewModel.undoRemoval() }
            .setActionTextColor(getThemeColor(context!!,
                ThemeColor.SNACK_BAR_ACTION_COLOR))
            .setBehavior(object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View) = false
            })
    }

    override fun setUpFab(view: View) {
        val fab = view.findViewById<SpeedDialView>(R.id.fab_create_catalog)
        fab.setOnChangeListener(object : SpeedDialView.OnChangeListener {
            override fun onMainActionSelected(): Boolean {
                viewModel.onCreateCatalogClick()
                return true
            }

            override fun onToggleChanged(isOpen: Boolean) {}
        })
    }

    override fun subscribeToEvents() {
        val owner = this

        with(viewModel) {
            redrawRecyclerView.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    refreshRecyclerView()
                }
                if (shouldHintBeShown) showHint() else hideHint()
            })
            allLiveDataList.add(redrawRecyclerView)

            catalogClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { catalog -> enterCatalog(catalog) }
            })
            allLiveDataList.add(catalogClicked)

            catalogSwipeStarted.observe(owner, Observer {
                // на время свайпа нужно отключить drag режим, иначе возможен конфликт и зависание анимации
                it.getContentIfNotHandled()?.let { disableDragAndDrop() }
            })
            allLiveDataList.add(catalogSwipeStarted)

            catalogSwipePerformed.observe(
                owner,
                Observer { it.getContentIfNotHandled()?.let { } }) // do nothing yet
            allLiveDataList.add(catalogSwipePerformed)

            catalogSwipeFinished.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showCatalogRemovalSnackbar()
                    enableDragAndDrop()
                }
            })
            allLiveDataList.add(catalogSwipeFinished)

            catalogSwipeCanceled.observe(
                owner, Observer {
                    it.getContentIfNotHandled()?.let { enableDragAndDrop() }
                })
            allLiveDataList.add(catalogSwipeCanceled)

            catalogEditClicked.observe(
                owner,
                Observer { it.getContentIfNotHandled()?.let { showOverlay() } })
            allLiveDataList.add(catalogEditClicked)

            createCatalogClicked.observe(
                owner,
                Observer { it.getContentIfNotHandled()?.let { showOverlay() } })
            allLiveDataList.add(createCatalogClicked)

            overlayBackgroundClicked.observe(
                owner,
                Observer { it.getContentIfNotHandled()?.let { closeOverlay() } })
            allLiveDataList.add(overlayBackgroundClicked)

            overlayEnterClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    closeOverlay()
                    scrollToBeginOfList()
                }
            })
            allLiveDataList.add(overlayEnterClicked)

            catalogNotificationClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { catalog ->
                    openNotificationFragment(catalog)
                }
            })
            allLiveDataList.add(catalogNotificationClicked)

            catalogShareClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { catalog ->
                    openShareFragment(catalog)
                }
            })
            allLiveDataList.add(catalogShareClicked)

            catalogEnvelopeClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showNetInfoDialog()
                }
            })
            allLiveDataList.add(catalogEnvelopeClicked)
        }
    }

    private fun openNotificationFragment(catalog: Catalog) {
        if (navController.currentDestination?.id == R.id.catalogs_fragment) {
            val direction = CatalogsFragmentDirections.actionCatalogsFragmentToMap(catalog.id,
                catalog.name, catalog.groupExpandStates)
            navController.navigate(direction)
        }
    }

    private fun showCatalogRemovalSnackbar() {
        Log.d(TAG, "showRemovalSnackbar: ")
        undoSnackbar.show()
    }

    private fun enterCatalog(catalog: Catalog) {
        Log.d(TAG, "enterCatalog: ")
        if (navController.currentDestination?.id == R.id.catalogs_fragment) {
            val direction =
                CatalogsFragmentDirections.actionCatalogsFragmentToProductsFragment(catalog.name,
                    catalog.id, catalog.groupExpandStates)
            navController.navigate(direction)
        }
    }

    private fun openShareFragment(catalog: Catalog) {
        if (navController.currentDestination?.id == R.id.catalogs_fragment) {
            val direction =
                CatalogsFragmentDirections.actionCatalogsFragmentToShareFragment(catalog.id,
                    catalog.name, catalog.totalProductCount.toString())
            navController.navigate(direction)
        }
    }

    private fun scrollToBeginOfList() {
        recyclerView.scrollToPosition(0)
    }

    private fun showNetInfoDialog() {
        CatalogNetInfoDialog().show(childFragmentManager, "net_info_dialog")
    }

    override fun hideSnackbars() {
        undoSnackbar.dismiss()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onFragmentStop()
    }
}