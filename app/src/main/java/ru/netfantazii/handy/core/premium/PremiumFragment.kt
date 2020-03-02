package ru.netfantazii.handy.core.premium

import android.os.Bundle
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

class PremiumFragment : Fragment() {
    private lateinit var billingViewModel: BillingViewModel
    private val allLiveDataList = mutableListOf<LiveData<*>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingViewModel = ViewModelProviders.of(activity!!).get(BillingViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.premium_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).checkMenuItem(R.id.premiumFragment)
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        val owner = this
        with(billingViewModel) {
            oneMonthButtonClicked.observe(this@PremiumFragment, Observer {
                it.getContentIfNotHandled()?.let {
                    billingViewModel.launchBillingFlow(activity!!, BillingPurchaseTypes.ONE_MONTH)
                }
            })
            allLiveDataList.add(oneMonthButtonClicked)
            oneYearButtonClicked.observe(this@PremiumFragment, Observer {
                it.getContentIfNotHandled()?.let {
                    billingViewModel.launchBillingFlow(activity!!, BillingPurchaseTypes.ONE_YEAR)
                }
            })
            allLiveDataList.add(oneYearButtonClicked)
            foreverButtonClicked.observe(this@PremiumFragment, Observer {
                it.getContentIfNotHandled()?.let {
                    billingViewModel.launchBillingFlow(activity!!, BillingPurchaseTypes.FOREVER)
                }
            })
            allLiveDataList.add(foreverButtonClicked)
        }
    }
}