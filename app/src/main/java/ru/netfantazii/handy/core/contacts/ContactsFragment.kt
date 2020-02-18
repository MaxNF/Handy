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
    private lateinit var binding: ContactsFragmentBinding

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
        binding = ContactsFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createRecyclerView()
        createSnackbars()
        createHints()
        subscribeToEvents()
        (activity as MainActivity).uncheckActiveMenuItem()
    }

    private fun createRecyclerView() {
        adapter = ContactsAdapter(viewModel, viewModel, viewModel.inputFilter)
        recyclerView = binding.contactsRecyclerView
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }

    private fun createSnackbars() {
        val coordinatorLayout = binding.coordinatorLayout
        deleteSnackbar = Snackbar.make(
            coordinatorLayout,
            getString(R.string.contact_undo_label),
            Snackbar.LENGTH_SHORT)
            .setActionTextColor(getThemeColor(context!!, ThemeColor.SNACK_BAR_ACTION_COLOR))
            .setBehavior(object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View) = false
            })
    }

    private fun createHints() {
        hint = binding.hintGroup
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
                it.getContentIfNotHandled()?.let {
                    showDeleteDialog()
                }
            })
            allLiveDataList.add(contactSwipePerformed)

            contactEditClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showEditDialog()
                }
            })
            allLiveDataList.add(contactEditClicked)

            addContactClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showEditDialog()
                }
            })
            allLiveDataList.add(addContactClicked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeFromEvents()
    }

    private fun unsubscribeFromEvents() {
        allLiveDataList.forEach { it.removeObservers(this) }
    }

    private fun showEditDialog() {
        EditContactDialog().show(childFragmentManager, "edit_dialog")
    }

    private fun showDeleteDialog() {
        DeleteContactDialog().show(childFragmentManager, "edit_dialog")
    }
}