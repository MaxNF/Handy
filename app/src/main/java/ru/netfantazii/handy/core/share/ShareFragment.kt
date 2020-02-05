package ru.netfantazii.handy.core.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.R
import ru.netfantazii.handy.databinding.ShareFragmentBinding

class ShareFragment : Fragment() {
    private val fragmentArgs: ShareFragmentArgs by navArgs()

    lateinit var viewModel: ShareViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createViewModel()
    }

    private fun createViewModel() {
        val localRepository =
            (requireContext().applicationContext as HandyApplication).localRepository
        val remoteRepository =
            (requireContext().applicationContext as HandyApplication).remoteRepository
        viewModel =
            ViewModelProviders.of(
                this,
                ShareVmFactory(fragmentArgs.catalogId,
                    fragmentArgs.catalogName,
                    fragmentArgs.totalProducts,
                    localRepository,
                    remoteRepository)
            ).get(ShareViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = ShareFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}