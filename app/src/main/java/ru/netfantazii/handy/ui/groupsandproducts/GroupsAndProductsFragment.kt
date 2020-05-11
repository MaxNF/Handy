package ru.netfantazii.handy.ui.groupsandproducts

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.activity_main.*
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.ui.main.NetworkViewModel
import ru.netfantazii.handy.R
import ru.netfantazii.handy.ui.base.BaseFragment
import ru.netfantazii.handy.utils.ThemeColor
import ru.netfantazii.handy.utils.getThemeColor
import ru.netfantazii.handy.customviews.RecyclerViewDecorator
import ru.netfantazii.handy.data.model.GroupType
import ru.netfantazii.handy.utils.extensions.dpToPx
import ru.netfantazii.handy.data.model.User
import ru.netfantazii.handy.databinding.ProductsFragmentBinding
import ru.netfantazii.handy.di.ViewModelFactory
import ru.netfantazii.handy.di.components.GroupsAndProductsComponent
import ru.netfantazii.handy.di.modules.groupsandproducts.GroupsAndProductsProvideModule
import ru.netfantazii.handy.utils.extensions.injectViewModel
import java.lang.UnsupportedOperationException
import javax.inject.Inject

class GroupsAndProductsFragment : BaseFragment<GroupsAndProductsAdapter>() {
    private val TAG = "GroupsAndProducts"

    private val fragmentArgs: GroupsAndProductsFragmentArgs by navArgs()
    private lateinit var component: GroupsAndProductsComponent
    @Inject
    lateinit var factory: ViewModelFactory
    private lateinit var viewModel: GroupsAndProductsViewModel
    @Inject
    lateinit var expandManager: RecyclerViewExpandableItemManager

    private lateinit var productUndoSnackbar: Snackbar
    private lateinit var groupUndoSnackbar: Snackbar
    private lateinit var shareMenuButton: MenuItem
    private val shareButtonCallback = object :
        Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            val user = (sender as ObservableField<User?>).get()
            shareMenuButton.isVisible = user != null
        }
    }
    private lateinit var networkViewModel: NetworkViewModel

    override fun injectDependencies() {
        component =
            (context!!.applicationContext as HandyApplication).appComponent.groupsAndProductsComponent()
                .create(fragmentArgs.catalogId,
                    fragmentArgs.groupExpandStates,
                    GroupsAndProductsProvideModule(this))
        component.inject(this)
        viewModel = injectViewModel(factory)
        networkViewModel = requireActivity().injectViewModel(factory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = ProductsFragmentBinding.inflate(inflater, container, false)
        binding.isPremium = (activity!!.application as HandyApplication).isPremium
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCatalogName()
    }

    private fun restoreExpandState(groupExpandStates: RecyclerViewExpandableItemManager.SavedState) {
        expandManager.restoreState(groupExpandStates)
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveExpandStateToDb(expandManager.savedState as RecyclerViewExpandableItemManager.SavedState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::expandManager.isInitialized) {
            saveExpandState()
        }
    }

    private fun setCatalogName() {
        activity!!.toolbar.title = fragmentArgs.catalogName
    }

    override fun createRecyclerView(view: View) {
        super.createRecyclerView(view)
        recyclerView.addItemDecoration(RecyclerViewDecorator())
        recyclerView.setHasFixedSize(false)
        guardManager.attachRecyclerView(recyclerView)
        swipeManager.attachRecyclerView(recyclerView)
        dragManager.attachRecyclerView(recyclerView)
        expandManager.attachRecyclerView(recyclerView)
    }

    override fun subscribeToEvents() {
        subscribeToProductEvents()
        subscribeToGroupEvents()
        subscribeToDialogEvents()
        val owner = this
        with(viewModel) {
            redrawRecyclerView.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    refreshRecyclerView()
                    Log.d(TAG, "subscribeToEvents: ")
                }
                if (shouldHintBeShown) showHint() else hideHint()
                restoreExpandState(viewModel.groupExpandStates)
            })

            allLiveDataList.add(redrawRecyclerView)

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
        networkViewModel.user.addOnPropertyChangedCallback(shareButtonCallback)

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
                    if (viewModel.getGroupList()
                            .isNotEmpty() && viewModel.getGroupList()[0].groupType == GroupType.ALWAYS_ON_TOP
                    ) {
                        expandManager.expandGroup(0)
                        saveExpandState()
                    } // открываем ALWAYS_ON_TOP группу, если она видна на экране
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
                        Log.d(TAG, "subscribeToGroupEvents: collapsed")
                        expandManager.collapseGroup(groupPosition)
                    } else {
                        Log.d(TAG, "subscribeToGroupEvents: expanded")
                        expandManager.expandGroup(groupPosition)
                        saveExpandState()
                    }
                    Log.d(TAG, "subscribeToGroupEvents: ")
                    saveExpandState()
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
                it.getContentIfNotHandled()?.let { groupIndex ->
                    showOverlay()
                    scrollToGroup(groupIndex)
                }
            })
            allLiveDataList.add(groupEditClicked)

            groupDragSucceed.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { } // do nothing yet
            })
            allLiveDataList.add(groupDragSucceed)

            createGroupClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showOverlay()
                    scrollToBeginOfList()
                }
            })
            allLiveDataList.add(createGroupClicked)

            groupCreateProductClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { groupPosition ->
                    showOverlay()
                    expandManager.expandGroup(groupPosition)
                    scrollToGroup(groupPosition)
                    saveExpandState()
                }
            })
        }
    }

    private fun subscribeToDialogEvents() {
        val owner = this
        with(viewModel) {
            deleteAllClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { showDeleteAllDialog() }
            })
            allLiveDataList.add(deleteAllClicked)

            cancelAllClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { showCancelAllDialog() }
            })
            allLiveDataList.add(cancelAllClicked)

            buyAllClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { showBuyAllDialog() }
            })
            allLiveDataList.add(buyAllClicked)
        }
    }

    override fun setUpFab(view: View) {
        val fab = view.findViewById<SpeedDialView>(R.id.speedDial)
        with(fab) {

            addActionItem(SpeedDialActionItem.Builder(R.id.fab_add_recipe,
                R.drawable.ic_fab_action_add_recipe)
                .setFabImageTintColor(getThemeColor(
                    context,
                    ThemeColor.FAB_ICON_TINT))
                .setLabel(getString(R.string.fab_create_recipe_action))
                .setLabelClickable(true)
                .setLabelBackgroundColor(getThemeColor(
                    context,
                    ThemeColor.SPEED_DIAL_LABEL_BACKGROUND))
                .setLabelColor(getThemeColor(context,
                    ThemeColor.SPEED_DIAL_LABEL_COLOR))
                .create())

            addActionItem(SpeedDialActionItem.Builder(R.id.fab_add_buy,
                R.drawable.ic_fab_action_add_buy)
                .setFabImageTintColor(getThemeColor(
                    context,
                    ThemeColor.FAB_ICON_TINT))
                .setLabel(getString(R.string.fab_create_buy_action))
                .setLabelClickable(true)
                .setLabelBackgroundColor(getThemeColor(
                    context,
                    ThemeColor.SPEED_DIAL_LABEL_BACKGROUND))
                .setLabelColor(getThemeColor(context,
                    ThemeColor.SPEED_DIAL_LABEL_COLOR))
                .create())

            setOnActionSelectedListener {
                when (it.id) {
                    R.id.fab_add_recipe -> {
                        viewModel.onCreateGroupClick()
                        fab.close()
                        true
                    }
                    R.id.fab_add_buy -> {
                        viewModel.onCreateProductClick()
                        fab.close()
                        true
                    }
                    else -> throw UnsupportedOperationException("Unknown action id")
                }
            }
        }
    }

    override fun createSnackbars(view: View) {
        val constraintLayout = view.findViewById<ConstraintLayout>(R.id.constraint_layout)
        productUndoSnackbar = Snackbar.make(constraintLayout,
            getString(R.string.buy_undo_label),
            Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo_action)) { viewModel.undoProductRemoval() }
            .setActionTextColor(getThemeColor(context!!,
                ThemeColor.SNACK_BAR_ACTION_COLOR))
            .setBehavior(object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View): Boolean = false
            })

        groupUndoSnackbar = Snackbar.make(constraintLayout,
            getString(R.string.recipe_undo_label),
            Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo_action)) { viewModel.undoGroupRemoval() }
            .setActionTextColor(getThemeColor(context!!,
                ThemeColor.SNACK_BAR_ACTION_COLOR))
            .setBehavior(object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View): Boolean = false
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.buy_all -> {
                viewModel.onBuyAllClick()
                true
            }
            R.id.cancel_all -> {
                viewModel.onCancelAllClick()
                true
            }
            R.id.delete_all -> {
                viewModel.onDeleteAllClick()
                true
            }
            R.id.share_menu_button -> {
                openShareFragment()
                true
            }
            R.id.notification_menu_button -> {
                openNotificationFragment()
                true
            }
            else -> false
        }
    }

    private fun openShareFragment() {
        val direction =
            GroupsAndProductsFragmentDirections.actionProductsFragmentToShareFragment(fragmentArgs.catalogId,
                fragmentArgs.catalogName, viewModel.allFilteredProducts.size.toString())
        navController.navigate(direction)
    }

    private fun openNotificationFragment() {
        val direction =
            GroupsAndProductsFragmentDirections.actionProductsFragmentToNotificationsFragment(
                fragmentArgs.catalogId,
                fragmentArgs.catalogName,
                fragmentArgs.groupExpandStates)
        navController.navigate(direction)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.groups_toolbar_menu, menu)
        shareMenuButton = menu.findItem(R.id.share_menu_button)
        shareMenuButton.isVisible = networkViewModel.user.get() != null
    }

    private fun saveExpandState() {
        viewModel.groupExpandStates =
            expandManager.savedState as RecyclerViewExpandableItemManager.SavedState
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

    private fun showBuyAllDialog() {
        BuyAllDialog().show(childFragmentManager, "buyAllDialog")
    }

    private fun showCancelAllDialog() {
        CancelAllDialog().show(childFragmentManager, "cancelAllDialog")
    }

    private fun showDeleteAllDialog() {
        DeleteAllDialog().show(childFragmentManager, "deleteAllDialog")
    }

    private fun scrollToBeginOfList() {
        recyclerView.scrollToPosition(0)
    }

    override fun hideSnackbars() {
        productUndoSnackbar.dismiss()
        groupUndoSnackbar.dismiss()
    }

    override fun unsubscribeFromEvents() {
        super.unsubscribeFromEvents()
        networkViewModel.user.removeOnPropertyChangedCallback(shareButtonCallback)
    }
}