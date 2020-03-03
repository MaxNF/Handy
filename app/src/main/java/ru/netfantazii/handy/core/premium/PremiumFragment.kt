package ru.netfantazii.handy.core.premium

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ru.netfantazii.handy.core.main.MainActivity
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.main.BillingViewModel
import ru.netfantazii.handy.data.BillingPurchaseTypes
import ru.netfantazii.handy.databinding.PremiumFragmentBinding

class PremiumFragment : Fragment() {
    private lateinit var billingViewModel: BillingViewModel
    private val allLiveDataList = mutableListOf<LiveData<*>>()
    private val TAG = "PremiumFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingViewModel = ViewModelProviders.of(activity!!).get(BillingViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = PremiumFragmentBinding.inflate(inflater, container, false)
        binding.billingViewModel = billingViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).checkMenuItem(R.id.premiumFragment)
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        val owner = this
        with(billingViewModel) {
            oneMonthButtonClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { billingObject ->
                    Log.d(TAG, "subscribeToEvents: ")
                    billingViewModel.launchBillingFlow(activity!!, billingObject)
                }
            })
            allLiveDataList.add(oneMonthButtonClicked)
            oneYearButtonClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { billingObject ->
                    Log.d(TAG, "subscribeToEvents: ")
                    billingViewModel.launchBillingFlow(activity!!, billingObject)
                }
            })
            allLiveDataList.add(oneYearButtonClicked)
            foreverButtonClicked.observe(owner, Observer {
                it.getContentIfNotHandled()?.let { billingObject ->
                    Log.d(TAG, "subscribeToEvents: ")
                    billingViewModel.launchBillingFlow(activity!!, billingObject)
                }
            })
            allLiveDataList.add(foreverButtonClicked)
        }
    }
}