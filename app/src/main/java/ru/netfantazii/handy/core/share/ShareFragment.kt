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
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.core.main.NetworkViewModel
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.contacts.EditContactDialog
import ru.netfantazii.handy.databinding.ShareFragmentBinding
import ru.netfantazii.handy.extensions.showLongToast
import ru.netfantazii.handy.data.Contact

class ShareFragment : Fragment() {
    private val fragmentArgs: ShareFragmentArgs by navArgs()
    private val allLiveDataList = mutableListOf<LiveData<*>>()

    private lateinit var shareViewModel: ShareViewModel
    private lateinit var networkViewModel: NetworkViewModel
    private lateinit var spinner: Spinner
    private lateinit var spinnerAdapter: ArrayAdapter<Contact>
    private lateinit var adScreen: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createViewModels()
    }

    private fun createViewModels() {
        val localRepository =
            (requireContext().applicationContext as HandyApplication).localRepository
        shareViewModel =
            ViewModelProviders.of(
                this,
                ShareVmFactory(fragmentArgs.catalogId,
                    fragmentArgs.catalogName,
                    fragmentArgs.totalProducts,
                    localRepository)
            ).get(ShareViewModel::class.java)
        networkViewModel = ViewModelProviders.of(activity!!).get(NetworkViewModel::class.java)
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

        if (!(activity!!.application as HandyApplication).isPremium.get()) {
            setUpAdScreen()
        }

        return binding.root
    }

    private fun setUpAdScreen() {
        adScreen = InterstitialAd(requireContext())
        adScreen.adUnitId = getString(R.string.ad_interstitial_share_unit_id)
        adScreen.loadAd(AdRequest.Builder().build())
        adScreen.adListener = object : AdListener() {
            override fun onAdClosed() {
                adScreen.loadAd(AdRequest.Builder().build())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1,
            networkViewModel.getValidContacts())
        spinner.adapter = spinnerAdapter
        subscribeToEvents()
    }

    private fun showAdScreen() {
        if (!(activity!!.application as HandyApplication).isPremium.get()) {
            if (::adScreen.isInitialized) {
                if (adScreen.isLoaded) {
                    adScreen.show()
                }
            }
        }
    }

    private fun subscribeToEvents() {
        val owner = this
        with(shareViewModel) {
            sendClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { catalogData ->
                    networkViewModel.sendCatalog(catalogData)
                    showAdScreen()
                }
            })
            allLiveDataList.add(sendClicked)

            sendClickedNoRecipient.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    showLongToast(requireContext(), getString(R.string.no_recipient_error))
                }
            })
        }

        with(networkViewModel) {
            contactsUpdated.observe(owner, Observer {
                it.getContentIfNotHandled()?.let {
                    spinnerAdapter.clear()
                    spinnerAdapter.addAll(networkViewModel.getValidContacts())
                    spinnerAdapter.notifyDataSetChanged()
                }
            })
            allLiveDataList.add(contactsUpdated)

            addContactClicked.observe(owner, Observer {
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