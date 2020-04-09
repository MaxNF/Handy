package ru.netfantazii.handy.core.notifications.alarm

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.notifications.BUNDLE_CATALOG_ID_KEY
import ru.netfantazii.handy.core.notifications.BUNDLE_CATALOG_NAME_KEY
import ru.netfantazii.handy.core.notifications.BUNDLE_EXPAND_STATE_KEY
import ru.netfantazii.handy.databinding.AlarmFragmentBinding
import ru.netfantazii.handy.di.ViewModelFactory
import ru.netfantazii.handy.di.components.AlarmComponent
import ru.netfantazii.handy.di.modules.alarm.AlarmProvideModule
import ru.netfantazii.handy.extensions.injectViewModel
import java.util.*
import javax.inject.Inject

class AlarmFragment : Fragment() {

    private lateinit var component: AlarmComponent
    @Inject
    lateinit var factory: ViewModelFactory
    private lateinit var viewModel: AlarmViewModel

    lateinit var datePicker: DatePicker
    lateinit var timePicker: TimePicker
    private val allLiveDataList = mutableListOf<LiveData<*>>()
    private val TAG = "AlarmFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        injectDependencies()
        super.onCreate(savedInstanceState)
    }

    private fun injectDependencies() {
        val currentCatalogId = arguments!!.getLong(BUNDLE_CATALOG_ID_KEY)
        val catalogName = arguments!!.getString(BUNDLE_CATALOG_NAME_KEY)!!
        val groupExpandState =
            arguments!!.getParcelable<RecyclerViewExpandableItemManager.SavedState>(
                BUNDLE_EXPAND_STATE_KEY)!!
        component =
            (context!!.applicationContext as HandyApplication).appComponent.alarmComponent()
                .create(currentCatalogId, catalogName, groupExpandState)
        component.inject(this)
        viewModel = injectViewModel(factory)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = AlarmFragmentBinding.inflate(inflater, container, false)
        datePicker = binding.datePickerView
        timePicker = binding.timePickerView
        timePicker.setIs24HourView(DateFormat.is24HourFormat(requireContext()))
        binding.isPremium = (activity!!.application as HandyApplication).isPremium
        binding.viewModel = viewModel
        binding.datePicker = binding.datePickerView
        binding.timePicker = binding.timePickerView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscribeToEvents()
        loadAds(view)
    }

    private fun loadAds(view: View) {
        val adView = view.findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder()
            .build()
        adView.loadAd(adRequest)
    }

    private fun subscribeToEvents() {
        viewModel.newDataReceived.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it.getContentIfNotHandled()?.let { calendar ->
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                datePicker.updateDate(year, month, day)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePicker.hour = hour
                    timePicker.minute = minute
                } else {
                    timePicker.currentHour = hour
                    timePicker.currentMinute = minute
                }
            }
        })
        allLiveDataList.add(viewModel.newDataReceived)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeFromEvents()
    }

    private fun unsubscribeFromEvents() {
        allLiveDataList.forEach { it.removeObservers(this) }
    }
}