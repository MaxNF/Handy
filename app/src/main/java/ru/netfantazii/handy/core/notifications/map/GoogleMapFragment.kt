package ru.netfantazii.handy.core.notifications.map

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.main.NetworkViewModel
import ru.netfantazii.handy.core.notifications.BUNDLE_CATALOG_ID_KEY
import ru.netfantazii.handy.core.notifications.BUNDLE_CATALOG_NAME_KEY
import ru.netfantazii.handy.core.notifications.BUNDLE_EXPAND_STATE_KEY
import ru.netfantazii.handy.databinding.GoogleMapFragmentBinding
import ru.netfantazii.handy.extensions.showLongToast

class GoogleMapFragment : Fragment() {
    private val TAG = "GoogleMapFragment"

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private lateinit var viewModel: MapViewModel
    private lateinit var placesClient: PlacesClient

    private val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    private val DEFAULT_ZOOM = 15.0f

    private val allLiveDataList = mutableListOf<LiveData<*>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createViewModel()
        initializePlacesSdk()
        setHasOptionsMenu(true)
    }

    private fun initializePlacesSdk() {
        val networkViewModel = ViewModelProviders.of(activity!!).get(NetworkViewModel::class.java)
        Places.initialize(activity!!.applicationContext,
            networkViewModel.user.get()!!.placesApiKey)
        placesClient = Places.createClient(requireContext())
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
            moveCameraToLastKnownLocation(map)
            subscribeToEvents()
        }

        setUpAutocompleteWidget()
    }

    private fun setUpAutocompleteWidget() {
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                drawFoundPlaceOnMap(place)
                Log.d(TAG,
                    "onPlaceSelected: ${place.name}, ${place.latLng?.latitude}, ${place.latLng?.longitude}")
            }

            override fun onError(status: Status) {
                handleAutocompleteError(status)
            }
        })
    }

    private fun drawFoundPlaceOnMap(place: Place) {
        val placeLatLng = place.latLng
        placeLatLng?.let { latLng ->
            map.addMarker(MarkerOptions()
                .position(latLng)
                .title(place.name))
        }
    }

    private fun moveCameraToLastKnownLocation(map: GoogleMap) {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(activity!!)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,
                location.longitude), DEFAULT_ZOOM))
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
            // todo сделать проверку какой это маркер, мой или поисковый (если поисковый то краш, т.к. ид не установлен), возможно сделать дефолтное поведение при поисковом маркере
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

    private fun handleAutocompleteError(status: Status) {
        showLongToast(requireContext(), getString(R.string.autocomplete_error_message))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.delete_all_geofences) {
            viewModel.onClearAllClick()
            true
        } else false
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