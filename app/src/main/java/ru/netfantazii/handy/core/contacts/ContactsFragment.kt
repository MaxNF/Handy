package ru.netfantazii.handy.core.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import ru.netfantazii.handy.*
import ru.netfantazii.handy.core.preferences.ThemeColor
import ru.netfantazii.handy.core.preferences.getThemeColor
import ru.netfantazii.handy.databinding.ContactsFragmentBinding
import ru.netfantazii.handy.extensions.showLongToast
import ru.netfantazii.handy.model.Contact
import ru.netfantazii.handy.model.ContactDialogAction

class ContactsFragment : Fragment() {
    private lateinit var hint: View
    private lateinit var adapter: ContactsAdapter
    private lateinit var deleteSnackbar: Snackbar
    private lateinit var viewModel: NetworkViewModel
    private val allLiveDataList = mutableListOf<LiveData<*>>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(activity!!).get(NetworkViewModel::class.java)
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
        subscribeToErrors()
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
        val coordinatorLayout = view.findViewById<CoordinatorLayout>(R.id.coordinatorLayout)
        deleteSnackbar = Snackbar.make(
            coordinatorLayout,
            getString(R.string.contact_undo_label),
            Snackbar.LENGTH_SHORT)
            .setActionTextColor(getThemeColor(context!!, ThemeColor.SNACK_BAR_ACTION_COLOR))
            .setBehavior(object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View) = false
            })
    }

    private fun createHints(view: View) {
        hint = view.findViewById(R.id.hint_group)
    }

    private fun subscribeToEvents() {
        val owner = this
        with(viewModel) {
            contactsUpdated.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    adapter.notifyDataSetChanged()
                }
            })
            allLiveDataList.add(contactsUpdated)

            contactSwipePerformed.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { contact ->
                    launchDeleteDialog(contact)
                }
            })
            allLiveDataList.add(contactSwipePerformed)

            contactEditClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { contact ->
                    val action =
                        if (contact.isValid) ContactDialogAction.RENAME else ContactDialogAction.RENAME_NOT_VALID
                    launchEditDialog(action, contact)
                }
            })
            allLiveDataList.add(contactEditClicked)

            addContactClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    launchEditDialog(ContactDialogAction.CREATE, null)
                }
            })
            allLiveDataList.add(addContactClicked)
        }
    }

    private fun subscribeToErrors() {
        val owner = this
        with(viewModel) {
            retrievingContactsError.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showLongToast(requireContext(), getString(R.string.contacts_retrieve_error))
                }
            })
            allLiveDataList.add(retrievingContactsError)

            updatingContactError.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showLongToast(requireContext(), getString(R.string.contacts_update_error))
                }
            })
            allLiveDataList.add(updatingContactError)

            creatingContactError.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showLongToast(requireContext(), getString(R.string.contacts_create_error))
                }
            })
            allLiveDataList.add(creatingContactError)

            removingContactError.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showLongToast(requireContext(), getString(R.string.contacts_remove_error))
                }
            })
            allLiveDataList.add(removingContactError)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeFromEvents()
    }

    private fun unsubscribeFromEvents() {
        allLiveDataList.forEach { it.removeObservers(this) }
    }

    private fun launchEditDialog(action: ContactDialogAction, contact: Contact?) {
        EditContactDialog(action, contact).show(childFragmentManager, "edit_dialog")
    }

    private fun launchDeleteDialog(contact: Contact) {
        DeleteContactDialog(contact).show(childFragmentManager, "edit_dialog")
    }
}