package ru.netfantazii.handy.core.notifications.map

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.SphericalUtil
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
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private val SEARCH_MARKER_TAG_VALUE = -1L
    private val SEARCH_BOUNDS_DISTANCE = 10000.0

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
            moveCameraToLastKnownLocation()
            subscribeToEvents()
            restoreSearchResults()
        }
    }

    private fun restoreSearchResults() {
        val tempMarkerList = mutableListOf<Marker>()
        viewModel.searchMarkersList.forEach {
            drawSearchMarker(it.position, it.title, tempMarkerList)
        }
        viewModel.searchMarkersList.clear()
        viewModel.searchMarkersList.addAll(tempMarkerList)
    }

    private fun drawFoundPlaceOnMap(place: Place) {
        val placeLatLng = place.latLng
        placeLatLng?.let { latLng ->
            drawSearchMarker(latLng, place.name, viewModel.searchMarkersList)
        }
    }

    private fun drawSearchMarker(
        latLng: LatLng,
        title: String?,
        listForMarkers: MutableList<Marker>
    ) {
        val marker = map.addMarker(MarkerOptions()
            .position(latLng)
            .title(title))
        marker.tag = SEARCH_MARKER_TAG_VALUE
        listForMarkers.add(marker)
    }

    private fun moveCameraToLastKnownLocation() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(activity!!)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,
                location.longitude), DEFAULT_ZOOM))
        }

    }

    private fun subscribeToEvents() {
        viewModel.newDataReceived.observe(this, Observer { event ->
            viewModel.geofenceIconList.forEach { it.remove() }
            viewModel.geofenceIconList.clear()
            viewModel.circleList.forEach { it.remove() }
            viewModel.circleList.clear()
            val handyIconOptions = MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_logo))
                .anchor(0.5f, 0.5f)
            for ((id, circleOptions) in event.peekContent()) {
                val circle = map.addCircle(circleOptions)
                circle.tag = id
                viewModel.circleList.add(circle)

                handyIconOptions.position(circle.center)
                val marker = map.addMarker(handyIconOptions)
                marker.tag = id
                viewModel.geofenceIconList.add(marker)
            }
        })
        allLiveDataList.add(viewModel.newDataReceived)

//        viewModel.findMyLocationClicked.observe(this, Observer {
//            it.getContentIfNotHandled()?.let {
//
//            }
//        })
//        allLiveDataList.add(viewModel.findMyLocationClicked)

//        viewModel.newSearchValueReceived.observe(this, Observer {
//
//        })
//        allLiveDataList.add(viewModel.newSearchValueReceived)

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
            val markerTag = marker.tag as Long
            if (markerTag != -1L) {
                viewModel.onCircleClick(markerTag)
                true
            } else {
                false
            }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_toolbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_all_geofences -> {
                viewModel.onClearAllClick()
                true
            }
            R.id.map_search -> {
                launchAutocompleteIntent()
                true
            }
            R.id.map_clear_search -> {
                clearSearchResults()
                true
            }
            else -> false
        }
    }

    private fun clearSearchResults() {
        viewModel.searchMarkersList.forEach { it.remove() }
        viewModel.searchMarkersList.clear()
    }

    private fun launchAutocompleteIntent() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val bounds = calculateBounds(map.cameraPosition)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setLocationBias(bounds)
            .setTypeFilter(TypeFilter.ESTABLISHMENT)
            .build(requireContext())
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        //todo сконфигурировать интент
    }

    private fun calculateBounds(cameraPosition: CameraPosition): RectangularBounds {
        map.projection.visibleRegion.latLngBounds.northeast
        val northEastBound =
            SphericalUtil.computeOffset(cameraPosition.target, SEARCH_BOUNDS_DISTANCE, 45.0)
        val southWestBound =
            SphericalUtil.computeOffset(cameraPosition.target, SEARCH_BOUNDS_DISTANCE, 225.0)
        return RectangularBounds.newInstance(southWestBound, northEastBound)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(it)
                        clearSearchResults()
                        drawFoundPlaceOnMap(place)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // do nothing
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(it)
                        showLongToast(requireContext(),
                            getString(R.string.autocomplete_error_message, status.statusCode))
                    }
                }
            }
        }
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