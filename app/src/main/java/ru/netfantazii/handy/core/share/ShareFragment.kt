package ru.netfantazii.handy.core.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.NetworkViewModel
import ru.netfantazii.handy.databinding.ShareFragmentBinding

class ShareFragment : Fragment() {
    private val fragmentArgs: ShareFragmentArgs by navArgs()
    private val allLiveDataList = mutableListOf<LiveData<*>>()

    lateinit var shareViewModel: ShareViewModel
    lateinit var networkViewModel: NetworkViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createViewModels()
    }

    private fun createViewModels() {
        val localRepository =
            (requireContext().applicationContext as HandyApplication).localRepository
        val remoteRepository =
            (requireContext().applicationContext as HandyApplication).remoteRepository
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        val owner = this
        with(shareViewModel) {
            sendClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { catalogData ->
                    networkViewModel.sendCatalog(catalogData)
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeFromEvents()
    }

    private fun unsubscribeFromEvents() {
        allLiveDataList.forEach { it.removeObservers(this) }
    }
}