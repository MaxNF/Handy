package ru.netfantazii.handy.core.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.core.main.NetworkViewModel
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.contacts.EditContactDialog
import ru.netfantazii.handy.core.main.MainActivity
import ru.netfantazii.handy.databinding.ShareFragmentBinding
import ru.netfantazii.handy.extensions.showLongToast
import ru.netfantazii.handy.data.Contact
import ru.netfantazii.handy.di.ViewModelFactory
import ru.netfantazii.handy.di.components.ShareComponent
import ru.netfantazii.handy.extensions.injectViewModel
import javax.inject.Inject

class ShareFragment : Fragment() {

    private lateinit var component: ShareComponent
    @Inject
    lateinit var factory: ViewModelFactory

    private val fragmentArgs: ShareFragmentArgs by navArgs()
    private val allLiveDataList = mutableListOf<LiveData<*>>()
    private lateinit var shareViewModel: ShareViewModel
    private lateinit var networkViewModel: NetworkViewModel
    private lateinit var spinner: Spinner
    private lateinit var spinnerAdapter: ArrayAdapter<Contact>

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
    }

    private fun injectDependencies() {
        component =
            (context!!.applicationContext as HandyApplication).appComponent.shareComponent()
                .create(fragmentArgs.catalogId,
                    fragmentArgs.catalogName, fragmentArgs.totalProducts)
        component.inject(this)
        shareViewModel = injectViewModel(factory)
        networkViewModel = activity!!.injectViewModel(factory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = ShareFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = shareViewModel
        binding.networkViewModel = networkViewModel
        spinner = binding.recipientSpinner
        binding.spinner = spinner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            networkViewModel.getValidContacts())
        spinner.adapter = spinnerAdapter
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        with(shareViewModel) {
            sendClicked.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let { catalogData ->
                    networkViewModel.sendCatalog(catalogData)
                    (activity as MainActivity).showAdScreen()
                }
            })
            allLiveDataList.add(sendClicked)

            sendClickedNoRecipient.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    showLongToast(requireContext(), getString(R.string.no_recipient_error))
                }
            })
        }

        with(networkViewModel) {
            contactsUpdated.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    spinnerAdapter.clear()
                    spinnerAdapter.addAll(networkViewModel.getValidContacts())
                    spinnerAdapter.notifyDataSetChanged()
                }
            })
            allLiveDataList.add(contactsUpdated)

            addContactClicked.observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let {
                    showEditDialog()
                }
            })
            allLiveDataList.add(addContactClicked)
        }
    }

    private fun showEditDialog() {
        EditContactDialog().show(childFragmentManager, "edit_dialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeFromEvents()
    }

    private fun unsubscribeFromEvents() {
        allLiveDataList.forEach { it.removeObservers(this) }
    }
}