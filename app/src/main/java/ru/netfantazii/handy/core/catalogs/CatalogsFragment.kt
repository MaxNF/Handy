package ru.netfantazii.handy.core.catalogs

import android.graphics.drawable.NinePatchDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.leinardi.android.speeddial.SpeedDialView
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.core.main.NetworkViewModel
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.BaseFragment
import ru.netfantazii.handy.core.main.BillingViewModel
import ru.netfantazii.handy.core.preferences.ThemeColor
import ru.netfantazii.handy.core.preferences.getThemeColor
import ru.netfantazii.handy.data.Catalog
import ru.netfantazii.handy.databinding.CatalogsFragmentBinding
import ru.netfantazii.handy.repositories.LocalRepository

class CatalogsFragment : BaseFragment<CatalogsAdapter>() {
    private val TAG = "CatalogsFragment"

    private lateinit var viewModel: CatalogsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var undoSnackbar: Snackbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = CatalogsFragmentBinding.inflate(inflater, container, false)
        binding.isPremium = (activity!!.application as HandyApplication).isPremium
        return binding.root
    }

    override fun createViewModels() {
        val repository = (requireContext().applicationContext as HandyApplication).localRepository
        viewModel =
            ViewModelProviders.of(
                this,
                CatalogsVmFactory(repository, activity!!.application)
            ).get(CatalogsViewModel::class.java)
    }

    override fun createRecyclerView(view: View) {
        dragManager = RecyclerViewDragDropManager()
        dragManager.setInitiateOnMove(false)
        dragManager.setInitiateOnTouch(false)
        dragManager.setInitiateOnLongPress(true)
        dragManager.draggingItemAlpha = 0.9f
        dragManager.draggingItemScale = 1.07f
        dragManager.dragStartItemAnimationDuration = 250
        dragManager.setDraggingItemShadowDrawable(ContextCompat.getDrawable(requireContext(),
            R.drawable.shadow) as NinePatchDrawable)

        val guardManager = RecyclerViewTouchActionGuardManager()
        guardManager.setInterceptVerticalScrollingWhileAnimationRunning(true)
        guardManager.isEnabled = true

        val swipeManager = RecyclerViewSwipeManager()

        val networkViewModel = ViewModelProviders.of(activity!!).get(NetworkViewModel::class.java)
        adapter = CatalogsAdapter(viewModel, viewModel, networkViewModel.user)
        var wrappedAdapter = dragManager.createWrappedAdapter(adapter)
        wrappedAdapter = swipeManager.createWrappedAdapter(wrappedAdapter)

        recyclerView = view.findViewById(R.id.rv_list)
        val layoutManager = LinearLayoutManager(context)

        val animator = DraggableItemAnimator()
        animator.supportsChangeAnimations = false

        recyclerView.itemAnimator = animator
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = wrappedAdapter

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
            .setActionTextColor(getThemeColor(context!!, ThemeColor.SNACK_BAR_ACTION_COLOR))
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
            newDataReceived.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    refreshRecyclerView()
                }
                if (shouldHintBeShown) showHint() else hideHint()
            })
            allLiveDataList.add(newDataReceived)

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

            catalogDragSucceeded.observe(
                owner, Observer {
                    it.getContentIfNotHandled()?.let { } // do nothing yet
                })
            allLiveDataList.add(catalogDragSucceeded)

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
        val direction = CatalogsFragmentDirections.actionCatalogsFragmentToMap(catalog.id,
            catalog.name, catalog.groupExpandStates)
        navController.navigate(direction)
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
        val direction = CatalogsFragmentDirections.actionCatalogsFragmentToShareFragment(catalog.id,
            catalog.name, catalog.totalProductCount.toString())
        navController.navigate(direction)
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