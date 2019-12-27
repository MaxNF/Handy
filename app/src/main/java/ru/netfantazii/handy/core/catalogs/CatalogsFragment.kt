package ru.netfantazii.handy.core.catalogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.help_fragment.*
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.R
import ru.netfantazii.handy.customviews.RecyclerViewDecorator
import ru.netfantazii.handy.core.BaseFragment
import ru.netfantazii.handy.core.preferences.ThemeColor
import ru.netfantazii.handy.core.preferences.getThemeColor
import ru.netfantazii.handy.extensions.doWithDelay
import ru.netfantazii.handy.extensions.fadeIn
import ru.netfantazii.handy.extensions.fadeOut

class CatalogsFragment : BaseFragment() {
    private val TAG = "CatalogsFragment"

    private lateinit var viewModel: CatalogsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CatalogsAdapter
    private lateinit var dragManager: RecyclerViewDragDropManager
    private lateinit var undoSnackbar: Snackbar
    private lateinit var hint: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.catalogs_fragment, container, false)
    }

    override fun createViewModel() {
        val repository = (requireContext().applicationContext as HandyApplication).localRepository
        viewModel =
            ViewModelProviders.of(
                this,
                CatalogsVmFactory(repository)
            ).get(CatalogsViewModel::class.java)
    }

    override fun createRecyclerView(view: View) {
        dragManager = RecyclerViewDragDropManager()
        dragManager.setInitiateOnMove(false)
        dragManager.setInitiateOnTouch(false)
        dragManager.setInitiateOnLongPress(true)

        val guardManager = RecyclerViewTouchActionGuardManager()
        guardManager.setInterceptVerticalScrollingWhileAnimationRunning(true)
        guardManager.isEnabled = true

        val animator = DraggableItemAnimator()
        animator.supportsChangeAnimations = false

        val swipeManager = RecyclerViewSwipeManager()

        adapter = CatalogsAdapter(viewModel, viewModel)
        var wrappedAdapter = dragManager.createWrappedAdapter(adapter)
        wrappedAdapter = swipeManager.createWrappedAdapter(wrappedAdapter)

        recyclerView = view.findViewById(R.id.rv_list)
        val layoutManager = LinearLayoutManager(context)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = wrappedAdapter
        recyclerView.itemAnimator = animator

        guardManager.attachRecyclerView(recyclerView)
        swipeManager.attachRecyclerView(recyclerView)
        dragManager.attachRecyclerView(recyclerView)

        recyclerView.addItemDecoration(RecyclerViewDecorator())
    }

    override fun createSnackbars(view: View) {
        val coordinatorLayout = view.findViewById<CoordinatorLayout>(R.id.coordinator_layout)
        undoSnackbar = Snackbar.make(
            coordinatorLayout,
            getString(R.string.catalog_undo_label),
            Snackbar.LENGTH_LONG
        )
            .setAction(getString(R.string.undo_action)) { viewModel.undoRemoval() }
            .setActionTextColor(getThemeColor(context!!, ThemeColor.SNACK_BAR_ACTION_COLOR))
            .setBehavior(object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View) = false
            })
    }

    override fun createHints(view: View) {
        hint = view.findViewById(R.id.hint_group)
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
        viewModel.newDataReceived.observe(this, Observer {
            it.getContentIfNotHandled()?.let { shouldHintBeShown ->
                Log.d(TAG, "newDataReceived: ")
                refreshRecyclerView()
                if (shouldHintBeShown) {
                    showHint()
                } else {
                    hideHint()
                }
            }
        })

        viewModel.catalogClicked.observe(this, Observer {
            it.getContentIfNotHandled()?.let { catalog -> enterCatalog(catalog.id) }
        })

        viewModel.catalogSwipeStarted.observe(this, Observer {
            // на время свайпа нужно отключить drag режим, иначе возможен конфликт и зависание анимации
            it.getContentIfNotHandled()?.let { disableDragAndDrop() }
        })

        viewModel.catalogSwipePerformed.observe(
            this,
            Observer { it.getContentIfNotHandled()?.let {} })

        viewModel.catalogSwipeFinished.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                showRemovalSnackbar()
                enableDragAndDrop()
            }
        })

        viewModel.catalogSwipeCanceled.observe(
            this,
            Observer { it.getContentIfNotHandled()?.let { enableDragAndDrop() } })

        viewModel.catalogEditClicked.observe(
            this,
            Observer { it.getContentIfNotHandled()?.let { showOverlay() } })

        viewModel.catalogDragSucceeded.observe(
            this,
            Observer { it.getContentIfNotHandled()?.let {} })

        viewModel.createCatalogClicked.observe(
            this,
            Observer { it.getContentIfNotHandled()?.let { showOverlay() } })

        viewModel.overlayBackgroundClicked.observe(
            this,
            Observer { it.getContentIfNotHandled()?.let { closeOverlay() } })

        viewModel.overlayEnterClicked.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                closeOverlay()
                scrollToFirstElement()
            }
        })
    }

    override fun unsubscribeFromEvents() {
        viewModel.newDataReceived.removeObservers(this)
        viewModel.catalogClicked.removeObservers(this)
        viewModel.catalogSwipeStarted.removeObservers(this)
        viewModel.catalogSwipePerformed.removeObservers(this)
        viewModel.catalogSwipeFinished.removeObservers(this)
        viewModel.catalogSwipeCanceled.removeObservers(this)
        viewModel.catalogEditClicked.removeObservers(this)
        viewModel.catalogDragSucceeded.removeObservers(this)
        viewModel.createCatalogClicked.removeObservers(this)
        viewModel.overlayBackgroundClicked.removeObservers(this)
        viewModel.overlayEnterClicked.removeObservers(this)
    }

    private fun showRemovalSnackbar() {
        Log.d(TAG, "showRemovalSnackbar: ")
        undoSnackbar.show()
        undoSnackbar.show()
    }

    private fun refreshRecyclerView() {
        Log.d(TAG, "refreshRecyclerView: ")
        adapter.notifyDataSetChanged()
    }

    private fun enterCatalog(id: Long) {
        Log.d(TAG, "enterCatalog: ")
    }

    private fun disableDragAndDrop() {
        dragManager.setInitiateOnLongPress(false)
    }

    private fun enableDragAndDrop() {
        // перед включением drag режима нужна задержка, т.к. иначе иногда возникает глитч с
        // анимацией после отмены свайпа
        doWithDelay(300) {
            dragManager.setInitiateOnLongPress(
                true
            )
        }
    }

    private fun scrollToFirstElement() {
        recyclerView.scrollToPosition(0)
    }

    private fun showHint() {
        hint.fadeIn()
    }

    private fun hideHint() {
        hint.fadeOut()
    }
}