package ru.netfantazii.handy.core.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.MainActivity
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.preferences.ThemeColor
import ru.netfantazii.handy.core.preferences.getThemeColor
import ru.netfantazii.handy.databinding.ContactsFragmentBinding

class ContactsFragment : Fragment() {
    private lateinit var hint: View
    private lateinit var adapter: ContactsAdapter
    private lateinit var undoSnackbar: Snackbar
    private lateinit var viewModel: ContactsViewModel
    private val allLiveDataList = mutableListOf<LiveData<*>>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createViewModel()
        setHasOptionsMenu(true)
    }

    private fun createViewModel() {
        val remoteRepository =
            (requireContext().applicationContext as HandyApplication).remoteRepository
        viewModel =
            ViewModelProviders.of(
                this,
                ContactsVmFactory(remoteRepository)
            ).get(ContactsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = ContactsFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createRecyclerView(view)
        createSnackbars(view)
        createHints(view)
        subscribeToEvents()
        (activity as MainActivity).uncheckActiveMenuItem()
    }

    private fun createRecyclerView(view: View) {
        val guardManager = RecyclerViewTouchActionGuardManager()
        guardManager.setInterceptVerticalScrollingWhileAnimationRunning(true)
        guardManager.isEnabled = true

        val swipeManager = RecyclerViewSwipeManager()

        adapter = ContactsAdapter(viewModel, viewModel)
        val wrappedAdapter = swipeManager.createWrappedAdapter(adapter)

        recyclerView = view.findViewById(R.id.contacts_recycler_view)
        val layoutManager = LinearLayoutManager(context)

        val animator = SwipeDismissItemAnimator()
        animator.supportsChangeAnimations = false

        recyclerView.itemAnimator = animator
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = wrappedAdapter

        guardManager.attachRecyclerView(recyclerView)
        swipeManager.attachRecyclerView(recyclerView)
    }

    private fun createSnackbars(view: View) {
        val coordinatorLayout = view.findViewById<CoordinatorLayout>(R.id.coordinator_layout)
        undoSnackbar = Snackbar.make(
            coordinatorLayout,
            getString(R.string.contact_undo_label),
            Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo_action)) { viewModel.undoRemoval() }
            .setActionTextColor(getThemeColor(context!!, ThemeColor.SNACK_BAR_ACTION_COLOR))
            .setBehavior(object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View) = false
            })
    }

    private fun createHints(view: View) {
        hint = view.findViewById(R.id.hint_group)
    }

    private fun subscribeToEvents() {}

    private fun unsubscribeFromEvents() {
        allLiveDataList.forEach { it.removeObservers(this) }
    }
}