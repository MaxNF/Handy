package ru.netfantazii.handy.core.notifications.map

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.notifications.BUNDLE_CATALOG_ID_KEY
import ru.netfantazii.handy.core.notifications.BUNDLE_CATALOG_NAME_KEY
import ru.netfantazii.handy.core.notifications.BUNDLE_EXPAND_STATE_KEY
import ru.netfantazii.handy.databinding.GoogleMapFragmentBinding

const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

class GoogleMapFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private lateinit var viewModel: MapViewModel

    private val allLiveDataList = mutableListOf<LiveData<*>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createViewModel()
    }

    private fun createViewModel() {
        val repository =
            (requireContext().applicationContext as HandyApplication).localRepository
        val currentCatalogId = arguments!!.getLong(BUNDLE_CATALOG_ID_KEY)
        val catalogName = arguments!!.getString(BUNDLE_CATALOG_NAME_KEY)!!
        val groupExpandState =
            arguments!!.getParcelable<RecyclerViewExpandableItemManager.SavedState>(
                BUNDLE_EXPAND_STATE_KEY)!!

        viewModel =
            ViewModelProviders.of(this,
                MapVmFactory(
                    repository,
                    currentCatalogId,
                    catalogName,
                    groupExpandState,
                    activity!!.application))
                .get(MapViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = GoogleMapFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var mapViewBundle: Bundle? = null
        savedInstanceState?.let {
            mapViewBundle = it.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mapView = view.findViewById(R.id.mapview)
        mapView.onCreate(mapViewBundle)

        mapView.getMapAsync {
            map = it
            map.uiSettings.isZoomControlsEnabled = true
            map.isMyLocationEnabled = true
            subscribeToEvents()
        }
    }

    private fun subscribeToEvents() {
        viewModel.newDataReceived.observe(this, Observer {
            map.clear()
            val handyIconOptions = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_logo))
                .anchor(0.5f, 0.5f)
            for ((id, circleOptions) in it.peekContent()) {
                val circle = map.addCircle(circleOptions)
                circle.tag = id
                handyIconOptions.position(circle.center)
                val marker = map.addMarker(handyIconOptions)
                marker.tag = id
            }
        })
        allLiveDataList.add(viewModel.newDataReceived)

        viewModel.findMyLocationClicked.observe(this, Observer {
            it.getContentIfNotHandled()?.let {

            }
        })
        allLiveDataList.add(viewModel.findMyLocationClicked)

        viewModel.newSearchValueReceived.observe(this, Observer {

        })
        allLiveDataList.add(viewModel.newSearchValueReceived)

        viewModel.geofenceLimitForFreeVersionReached.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                showGeofenceLimitForFreeVersionDialog()
            }
        })

        map.setOnMapLongClickListener { viewModel.onMapLongClick(it.latitude, it.longitude) }
        map.setOnCircleClickListener { circle ->
            val geofenceId = circle.tag as Long
            viewModel.onCircleClick(geofenceId)
        }
        map.setOnMarkerClickListener { marker ->
            val geofenceId = marker.tag as Long
            viewModel.onCircleClick(geofenceId)
            true
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            outState.putBundle(MAPVIEW_BUNDLE_KEY, Bundle())
        }
        mapView.onSaveInstanceState(outState)
    }

    private fun showGeofenceLimitForFreeVersionDialog() {
        GeofenceLimitDialog().show(childFragmentManager, "geofence_limit_dialog")
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}

class GeofenceLimitDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setMessage(R.string.dialog_geofence_limit_for_free_version)
            .setPositiveButton(R.string.buy_premium_version_button) { _, _ -> } //todo добавить действие при выборе купить премиум
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }
}