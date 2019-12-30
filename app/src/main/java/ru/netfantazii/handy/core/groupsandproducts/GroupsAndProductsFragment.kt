package ru.netfantazii.handy.core.groupsandproducts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.BaseFragment
import ru.netfantazii.handy.core.preferences.ThemeColor
import ru.netfantazii.handy.core.preferences.getThemeColor
import ru.netfantazii.handy.customviews.RecyclerViewDecorator
import ru.netfantazii.handy.extensions.dpToPx
import java.lang.UnsupportedOperationException

class GroupsAndProductsFragment : BaseFragment<GroupsAndProductsAdapter>() {
    val fragmentArgs: GroupsAndProductsFragmentArgs by navArgs()

    private lateinit var viewModel: GroupsAndProductsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var expandManager: RecyclerViewExpandableItemManager
    private lateinit var productUndoSnackbar: Snackbar
    private lateinit var groupUndoSnackbar: Snackbar
    private lateinit var hint: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.products_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCatalogName(view)
    }

    private fun setCatalogName(rootView: View) {
        val currentCatalogName = fragmentArgs.catalogName
        val catalogNameTextView = rootView.findViewById<TextView>(R.id.catalog_label)
        catalogNameTextView.text = currentCatalogName
    }

    override fun createViewModel() {
        val repository = (requireContext().applicationContext as HandyApplication).localRepository
        val currentCatalogId = fragmentArgs.catalogId
        viewModel =
            ViewModelProviders.of(this, GroupsAndProductsVmFactory(repository, currentCatalogId))
                .get(GroupsAndProductsViewModel::class.java)
    }

    override fun createRecyclerView(view: View) {
        dragManager = RecyclerViewDragDropManager()
        dragManager.setInitiateOnMove(false)
        dragManager.setInitiateOnTouch(false)
        dragManager.setInitiateOnLongPress(true)
        expandManager = RecyclerViewExpandableItemManager(null)
        val swipeManager = RecyclerViewSwipeManager()
        val layoutManager = LinearLayoutManager(context)
        val guardManager = RecyclerViewTouchActionGuardManager()
        guardManager.setInterceptVerticalScrollingWhileAnimationRunning(true)
        guardManager.isEnabled = true

        adapter = GroupsAndProductsAdapter(viewModel, viewModel, viewModel)
        var wrappedAdapter = expandManager.createWrappedAdapter(adapter)
        wrappedAdapter = dragManager.createWrappedAdapter(wrappedAdapter)
        wrappedAdapter = swipeManager.createWrappedAdapter(wrappedAdapter)

        recyclerView = view.findViewById(R.id.rv_list)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = wrappedAdapter

        val animator = SwipeDismissItemAnimator()
        animator.supportsChangeAnimations = false
        recyclerView.itemAnimator = animator
        recyclerView.addItemDecoration(RecyclerViewDecorator())
        recyclerView.setHasFixedSize(true)

        guardManager.attachRecyclerView(recyclerView)
        swipeManager.attachRecyclerView(recyclerView)
        dragManager.attachRecyclerView(recyclerView)
        expandManager.attachRecyclerView(recyclerView)
    }

    override fun subscribeToEvents() {
        subscribeToProductEvents()
        subscribeToGroupEvents()
        val owner = this
        with(viewModel) {
            newDataReceived.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { shouldHintBeShown ->
                    refreshRecyclerView()
                    if (shouldHintBeShown) {
                        showHint()
                    } else {
                        hideHint()
                    }
                }
            })
            allLiveDataList.add(newDataReceived)

            overlayBackgroundClicked.observe(owner,
                Observer { it.getContentIfNotHandled()?.let { closeOverlay() } })
            allLiveDataList.add(overlayBackgroundClicked)

            overlayEnterClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    closeOverlay()
                }
            })
            allLiveDataList.add(overlayEnterClicked)
        }
    }

    private fun subscribeToProductEvents() {
        val owner = this
        with(viewModel) {
            productClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { } // do nothing yet
            })
            allLiveDataList.add(productClicked)

            productSwipeStarted.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { disableDragAndDrop() }
            })
            allLiveDataList.add(productSwipeStarted)

            productSwipePerformed.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { } // do nothing yet
            })
            allLiveDataList.add(productSwipePerformed)

            productSwipeFinished.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showProductRemovalSnackbar()
                    enableDragAndDrop()
                }
            })
            allLiveDataList.add(productSwipeFinished)

            productSwipeCanceled.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { enableDragAndDrop() }
            })
            allLiveDataList.add(productSwipeCanceled)

            productEditClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { showOverlay() }
            })
            allLiveDataList.add(productEditClicked)

            productDragSucceed.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { } // do nothing yet
            })
            allLiveDataList.add(productDragSucceed)

            createProductClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showOverlay()
                    scrollToBeginOfList()
                }
            })
            allLiveDataList.add(createProductClicked)
        }
    }

    private fun subscribeToGroupEvents() {
        val owner = this
        with(viewModel) {
            groupClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { groupPosition ->
                    if (expandManager.isGroupExpanded(groupPosition)) {
                        expandManager.collapseGroup(groupPosition)
                    } else {
                        expandManager.collapseGroup(groupPosition)
                    }
                }
            })
            allLiveDataList.add(groupClicked)

            groupSwipeStarted.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { disableDragAndDrop() }
            })
            allLiveDataList.add(groupSwipeStarted)

            groupSwipePerformed.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { } // do nothing yet
            })
            allLiveDataList.add(groupSwipePerformed)

            groupSwipeFinished.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showGroupRemovalSnackbar()
                    enableDragAndDrop()
                }
            })
            allLiveDataList.add(groupSwipeFinished)

            groupSwipeCanceled.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { enableDragAndDrop() }
            })
            allLiveDataList.add(groupSwipeCanceled)

            groupEditClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { group ->
                    showOverlay()
                    scrollToGroup(group.position)
                }
            })
            allLiveDataList.add(groupEditClicked)

            groupDragSucceed.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { } // do nothing yet
            })
            allLiveDataList.add(groupDragSucceed)

            createGroupClicked.observe(owner, Observer {
                showOverlay()
                scrollToGroup(1)
            })
            allLiveDataList.add(createGroupClicked)
        }
    }

    override fun setUpFab(view: View) {
        val fab = view.findViewById<SpeedDialView>(R.id.speedDial)
        with(fab) {

            addActionItem(SpeedDialActionItem.Builder(R.id.fab_add_recipe,
                R.drawable.ic_fab_action_add_recipe)
                .setFabImageTintColor(getThemeColor(context, ThemeColor.FAB_ICON_TINT))
                .setLabel(getString(R.string.fab_create_recipe_action))
                .setLabelClickable(true)
                .setLabelBackgroundColor(getThemeColor(context,
                    ThemeColor.SPEED_DIAL_LABEL_BACKGROUND))
                .setLabelColor(getThemeColor(context, ThemeColor.SPEED_DIAL_LABEL_COLOR))
                .create())

            addActionItem(SpeedDialActionItem.Builder(R.id.fab_add_buy,
                R.drawable.ic_fab_action_add_buy)
                .setFabImageTintColor(getThemeColor(context, ThemeColor.FAB_ICON_TINT))
                .setLabel(getString(R.string.fab_create_buy_action))
                .setLabelClickable(true)
                .setLabelBackgroundColor(getThemeColor(context,
                    ThemeColor.SPEED_DIAL_LABEL_BACKGROUND))
                .setLabelColor(getThemeColor(context, ThemeColor.SPEED_DIAL_LABEL_COLOR))
                .create())

            setOnActionSelectedListener {
                when (it.id) {
                    R.id.fab_add_recipe -> {
                        viewModel.onCreateGroupClick()
                        true
                    }
                    R.id.fab_add_buy -> {
                        viewModel.onCreateProductClick()
                        true
                    }
                    else -> throw UnsupportedOperationException("Unknown action id")
                }
            }
        }
    }

    override fun createSnackbars(view: View) {
        val coordinatorLayout = view.findViewById<CoordinatorLayout>(R.id.coordinator_layout)
        productUndoSnackbar = Snackbar.make(coordinatorLayout,
            getString(R.string.buy_undo_label),
            Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo_action)) { viewModel.undoProductRemoval() }
            .setActionTextColor(getThemeColor(context!!, ThemeColor.SNACK_BAR_ACTION_COLOR))
            .setBehavior(object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View): Boolean = false
            })

        groupUndoSnackbar = Snackbar.make(coordinatorLayout,
            getString(R.string.recipe_undo_label),
            Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo_action)) { viewModel.undoGroupRemoval() }
            .setActionTextColor(getThemeColor(context!!, ThemeColor.SNACK_BAR_ACTION_COLOR))
            .setBehavior(object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View): Boolean = false
            })
    }

    private fun showProductRemovalSnackbar() {
        productUndoSnackbar.show()
    }

    private fun showGroupRemovalSnackbar() {
        groupUndoSnackbar.show()
    }

    private fun scrollToGroup(groupPosition: Int) {
        expandManager.scrollToGroup(groupPosition, dpToPx(50).toInt())
    }

    private fun scrollToBeginOfList() {
        recyclerView.scrollToPosition(0)
    }
}