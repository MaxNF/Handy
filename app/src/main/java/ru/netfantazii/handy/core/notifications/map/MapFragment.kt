package ru.netfantazii.handy.core.notifications.map

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import androidx.core.graphics.drawable.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.MenuItem
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavHostController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.LocationServices
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.*
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.network.NetworkError
import com.yandex.runtime.network.RemoteError
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.core.main.NetworkViewModel
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.notifications.BUNDLE_CATALOG_ID_KEY
import ru.netfantazii.handy.core.notifications.BUNDLE_CATALOG_NAME_KEY
import ru.netfantazii.handy.core.notifications.BUNDLE_EXPAND_STATE_KEY
import ru.netfantazii.handy.databinding.MapFragmentBinding
import ru.netfantazii.handy.di.ViewModelFactory
import ru.netfantazii.handy.di.components.MapComponent
import ru.netfantazii.handy.di.components.NotificationComponent
import java.lang.IllegalArgumentException
import javax.inject.Inject

class MapFragment : Fragment(), Session.SearchListener,
    SuggestSession.SuggestListener {
    private val TAG = "MapFragment"

    private lateinit var mapView: MapView
    private lateinit var component: MapComponent
    @Inject
    lateinit var factory: ViewModelFactory
    private lateinit var viewModel: MapViewModel

    private lateinit var mapObjects: MapObjectCollection
    private lateinit var locationManager: LocationManager
    private lateinit var geofenceIconProvider: ImageProvider
    private lateinit var userIconProvider: ImageProvider
    private lateinit var searchObjectIconProvider: ImageProvider
    private lateinit var searchObjectCollection: MapObjectCollection
    private lateinit var searchManager: SearchManager

    private val allLiveDataList = mutableListOf<LiveData<*>>()
    private var circleFillColor: Int = 0
    private var circleStrokeColor: Int = 0
    private val circleStrokeWidth: Float = 1.3f
    private lateinit var pinObjectCollection: MapObjectCollection
    private val suggestAreaSize = 0.2
    private val suggestResultLimit = 5
    private val suggestOptions =
        SuggestOptions().setSuggestTypes(SuggestType.BIZ.value or SuggestType.GEO.value or SuggestType.TRANSIT.value)
    private lateinit var searchView: SearchView
    private var showSuggestions = false

    private val circleTapListener = MapObjectTapListener { mapObject, point ->
        if (mapObject is CircleMapObject) {
            val geofenceId = mapObject.userData as Long
            viewModel.onCircleClick(geofenceId)
            true
        } else false
    }

    private val mapInputListener = object : InputListener {
        override fun onMapLongTap(map: Map, point: Point) {
            viewModel.onMapLongClick(point)
        }

        override fun onMapTap(p0: Map, p1: Point) {
            // do nothing
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        component =
            (context.applicationContext as HandyApplication).appComponent.mapComponent().create()
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        setHasOptionsMenu(true)

        val networkViewModel = ViewModelProviders.of(activity!!).get(NetworkViewModel::class.java)
        MapKitFactory.setApiKey(networkViewModel.user.get()!!.yandexMapApiKey)

        MapKitFactory.initialize(context)
        SearchFactory.initialize(context)
        locationManager = MapKitFactory.getInstance().createLocationManager()
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
        geofenceIconProvider = ImageProvider.fromResource(context, R.drawable.ic_map_logo)
        userIconProvider = ImageProvider.fromBitmap(resources.getDrawable(R.drawable.ic_person_pin,
            null).toBitmap())
        searchObjectIconProvider =
            ImageProvider.fromBitmap(resources.getDrawable(R.drawable.ic_search_location,
                null).toBitmap())
        createViewModel()
        getColorsFromRes()
    }

    private fun getColorsFromRes() {
        circleFillColor = ContextCompat.getColor(requireContext(), R.color.circleFillColor)
        circleStrokeColor = ContextCompat.getColor(requireContext(), R.color.circleStrokeColor)
    }

    private fun createViewModel() {
        val currentCatalogId = arguments!!.getLong(BUNDLE_CATALOG_ID_KEY)
        val catalogName = arguments!!.getString(BUNDLE_CATALOG_NAME_KEY)!!
        val groupExpandState =
            arguments!!.getParcelable<RecyclerViewExpandableItemManager.SavedState>(
                BUNDLE_EXPAND_STATE_KEY)!!

        viewModel =
            ViewModelProviders.of(this,
                factory)
                .get(MapViewModel::class.java)
        viewModel.initialize(currentCatalogId, catalogName, groupExpandState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        val binding = MapFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        mapView = binding.mapview
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: ")
        mapObjects = mapView.map.mapObjects
        subscribeToEvents()
        searchObjectCollection = mapObjects.addCollection()
        pinObjectCollection = mapObjects.addCollection()
        restoreSearchResults()
        restoreCameraPosition()
    }

    private fun restoreSearchResults() {
        searchObjectCollection.clear()
        viewModel.lastSearchPoints?.let { points ->
            points.forEach { point ->
                point?.let { addSearchPlacemark(it) }
            }
        }
    }

    private fun restoreCameraPosition() {
        if (viewModel.lastCameraPosition == null) {
            moveToLastKnownLocation()
        } else {
            mapView.map.move(viewModel.lastCameraPosition!!)
            reDrawPinIfNotNull(viewModel.lastPinPosition)
        }
    }

    private fun clearFocusAndSearch(searchValue: String) {
        searchView.clearFocus()
        beginSearch(searchValue)
    }

    private fun reDrawPinIfNotNull(point: Point?) {
        pinObjectCollection.clear()
        point?.let { pinObjectCollection.addPlacemark(it, userIconProvider) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.lastCameraPosition = mapView.map.cameraPosition
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeFromEvents()
    }

    private fun moveToLastKnownLocation() {
        val locationProvider = LocationServices.getFusedLocationProviderClient(context!!)
        locationProvider.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.d(TAG, "moveToLastKnownLocation: 1")
                    moveCameraWithPin(location.latitude, location.longitude)
                } else {
                    Log.d(TAG, "moveToLastKnownLocation: 2")
                    moveToUserLocation()
                }
            }
    }

    private fun moveToUserLocation() {
        locationManager.requestSingleUpdate(object : LocationListener {
            override fun onLocationStatusUpdated(p0: LocationStatus) {
                // do nothing
            }

            override fun onLocationUpdated(location: Location) {
                Log.d(TAG, "onLocationUpdated: ")
                moveCameraWithPin(location.position.latitude,
                    location.position.longitude)
            }
        })
    }

    private fun moveCameraWithPin(latitude: Double, longitude: Double) {
        val point = Point(latitude, longitude)
        mapView.map.move(getSimpleCamPosition(point),
            Animation(Animation.Type.SMOOTH, 0.5f), null)
        reDrawPinIfNotNull(point)
        viewModel.lastPinPosition = point
    }

    private fun subscribeToEvents() {
        viewModel.newDataReceived.observe(viewLifecycleOwner, Observer {
            // нужно перерисовать геозоны после поворота экрана, поэтому не обнуляем контент Ивента
            val diffSearcher =
                CircleDiffSearcher(
                    mapObjects,
                    viewModel.circleMap)
            diffSearcher.getRemovedCircles().forEach { mapEntry ->
                val circleToRemove = mapEntry.value
                val placemarkToRemove = diffSearcher.visiblePlacemarks[mapEntry.key]
                mapObjects.remove(circleToRemove)
                mapObjects.remove(placemarkToRemove as MapObject)
            }
            diffSearcher.getAddedCircles().forEach { mapEntry ->
                addCircleWithId(mapEntry.key, mapEntry.value)
                addPlacemarkWithId(mapEntry.key, mapEntry.value.center)
            }
        })
        allLiveDataList.add(viewModel.newDataReceived)

        viewModel.findMyLocationClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                moveToUserLocation()
            }
        })
        allLiveDataList.add(viewModel.findMyLocationClicked)

        viewModel.newSearchValueReceived.observe(viewLifecycleOwner, Observer {
            if (showSuggestions) {
                requestSuggest(it.peekContent())
            }
            showSuggestions = true
        })
        allLiveDataList.add(viewModel.newSearchValueReceived)

        viewModel.geofenceLimitForFreeVersionReached.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                showGeofenceLimitForFreeVersionDialog()
            }
        })

        mapView.map.addInputListener(mapInputListener)

        viewModel.zoomInClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                zoomIn()
            }
        })
        allLiveDataList.add(viewModel.zoomInClicked)

        viewModel.zoomOutClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                zoomOut()
            }
        })
        allLiveDataList.add(viewModel.zoomOutClicked)
    }

    private fun addCircleWithId(id: Long, circle: Circle) {
        val circleMapObject = mapObjects.addCircle(circle,
            circleStrokeColor,
            circleStrokeWidth,
            circleFillColor)

        circleMapObject.userData = id
        circleMapObject.addTapListener(circleTapListener)
    }

    private fun addPlacemarkWithId(id: Long, circleCenter: Point) {
        val placemarkMapObject =
            mapObjects.addPlacemark(circleCenter, geofenceIconProvider)
        placemarkMapObject.userData = id
    }

    private fun unsubscribeFromEvents() {
        allLiveDataList.forEach { it.removeObservers(this) }
    }

    private fun requestSuggest(query: String) {
        val centerLatitude = mapView.map.cameraPosition.target.latitude
        val centerLongitude = mapView.map.cameraPosition.target.longitude
        val suggestArea = BoundingBox(
            Point(centerLatitude - suggestAreaSize, centerLongitude - suggestAreaSize),
            Point(centerLatitude + suggestAreaSize, centerLongitude + suggestAreaSize))
        searchManager.createSuggestSession()
            .suggest(query, suggestArea, suggestOptions, this)
    }

    override fun onStart() {
        Log.d(TAG, "onStart: ")
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        Log.d(TAG, "onStop: ")
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_toolbar_menu, menu)
        val searchMenuItem = menu.findItem(R.id.map_toolbar_search)
        searchView = searchMenuItem.actionView as SearchView
        searchView.suggestionsAdapter = createEmptyAdapter()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                clearFocusAndSearch(searchView.query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                requestSuggest(newText)
                return true
            }
        })
        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                // do nothing
                return true
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val searchValue =
                    searchView.suggestionsAdapter.cursor.getString(1) // забираем строку, которая находится в текущем ряду во второй колонке (text)
                clearFocusAndSearch(searchValue)
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.delete_all_geofences) {
            viewModel.onClearAllClick()
            true
        } else false
    }

    private fun getSimpleCamPosition(point: Point): CameraPosition {
        return CameraPosition(point, 15.0f, 0.0f, 10.0f)
    }

    private fun beginSearch(searchQuery: String) {
        if (searchQuery.isEmpty()) {
            searchObjectCollection.clear()
        } else {
            searchManager.submit(searchQuery,
                VisibleRegionUtils.toPolygon(mapView.map.visibleRegion),
                SearchOptions(),
                this)
        }
    }

    private fun zoomIn() {
        val cameraPosition = mapView.map.cameraPosition
        val target = cameraPosition.target
        val azimuth = cameraPosition.azimuth
        val tilt = cameraPosition.tilt

        var zoom = cameraPosition.zoom
        if (++zoom > mapView.map.maxZoom) {
            zoom = mapView.map.maxZoom
        }
        val newCameraPosition = CameraPosition(target, zoom, azimuth, tilt)
        mapView.map.move(newCameraPosition, Animation(Animation.Type.SMOOTH, 0.5f), null)
    }

    private fun zoomOut() {
        val cameraPosition = mapView.map.cameraPosition
        val target = cameraPosition.target
        val azimuth = cameraPosition.azimuth
        val tilt = cameraPosition.tilt

        var zoom = cameraPosition.zoom
        if (--zoom < mapView.map.minZoom) {
            zoom = mapView.map.minZoom
        }
        val newCameraPosition = CameraPosition(target, zoom, azimuth, tilt)
        mapView.map.move(newCameraPosition, Animation(Animation.Type.SMOOTH, 0.5f), null)
    }

    override fun onSearchError(error: Error) {
        showErrorMessage(error)
    }

    override fun onSearchResponse(response: Response) {
        searchObjectCollection.clear()
        val searchResultPoints =
            response.collection.children.map { it.obj!!.geometry[0].point }
        viewModel.lastSearchPoints = searchResultPoints
        searchResultPoints.forEach { point -> point?.let { addSearchPlacemark(it) } }
    }

    override fun onResponse(allSuggestions: MutableList<SuggestItem>) {
        val resultsToAdd = allSuggestions.slice(0 until
                suggestResultLimit.coerceAtMost(allSuggestions.size))
            .map { it.displayText ?: "" }
            .toSet()
        searchView.suggestionsAdapter.swapCursor(createNewCursor(resultsToAdd))
    }

    private fun createEmptyAdapter(): SimpleCursorAdapter {
        val columnNames = arrayOf("_id", "text")
        val cursor = MatrixCursor(columnNames)
        return SimpleCursorAdapter(requireContext(),
            R.layout.lv_suggest_element,
            cursor,
            arrayOf("text"),
            intArrayOf(R.id.suggestion_text),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER)
    }

    private fun createNewCursor(resultsToAdd: Set<String>): Cursor {
        Log.d(TAG, "makeCursorAdapterFromSuggestions: size: ${resultsToAdd.size}")
        val columnNames = arrayOf("_id", "text")
        val cursor = MatrixCursor(columnNames)
        resultsToAdd.forEachIndexed { index, string ->
            cursor.addRow(listOf(index.toString(), string))
        }
        return cursor
    }

    override fun onError(error: Error) {
        showErrorMessage(error)
    }

    private fun showErrorMessage(error: Error) {
        val errorMessage = when (error) {
            is RemoteError -> getString(R.string.map_remote_error)
            is NetworkError -> getString(R.string.map_network_error)
            else -> getString(R.string.map_unknown_error)
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun addSearchPlacemark(point: Point) {
        searchObjectCollection.addPlacemark(point, searchObjectIconProvider)
    }

    private fun showGeofenceLimitForFreeVersionDialog() {
        GeofenceLimitDialog().show(childFragmentManager, "geofence_limit_dialog")
    }
}

/**
 * Утилитный класс для поиска разницы между полученным списком геозон и тех геозон, что отображены
 * на экране в данный момент. Совмещает в себе итератор по элементам карты (MapObjectVisitor). Для работы,
 * требуется, чтобы метод MapObjectVisitor'а onCollectionVisitStart(...) возвращал true (если false, то
 * коллекция объектов будет пропущена итератором.) */
class CircleDiffSearcher(
    private val visibleMapObjects: MapObjectCollection,
    private val newCircles: kotlin.collections.Map<Long, Circle>
) : MapObjectVisitor {
    private val TAG = "CircleDiffSearcher"

    private var resultsReady = false
    private var traversed = false
    private lateinit var removedCircles: kotlin.collections.Map<Long, CircleMapObject>
    private lateinit var addedCircles: kotlin.collections.Map<Long, Circle>

    val visibleCircles = mutableMapOf<Long, CircleMapObject>()
    val visiblePlacemarks = mutableMapOf<Long, PlacemarkMapObject>()


    private fun searchForDifferences() {
        if (!resultsReady) {
            if (!traversed) visibleMapObjects.traverse(this)
            visibleCircles.forEach { Log.d(TAG, "searchForDifferences: ${it.key}") }
            removedCircles = visibleCircles.filter { newCircles[it.key] == null }
            val oldCirclesTransformed = visibleCircles.mapValues { it.value.geometry }
            addedCircles = newCircles.filter { oldCirclesTransformed[it.key] == null }
            resultsReady = true
        }
    }

    fun getRemovedCircles(): kotlin.collections.Map<Long, CircleMapObject> {
        Log.d(TAG, "getRemovedCircles: ")
        if (!resultsReady) searchForDifferences()
        return removedCircles
    }

    fun getAddedCircles(): kotlin.collections.Map<Long, Circle> {
        Log.d(TAG, "getAddedCircles: ")
        if (!resultsReady) searchForDifferences()
        return addedCircles
    }

    fun getAllVisibleCircles() = if (traversed) visibleCircles else {
        visibleMapObjects.traverse(this)
        visibleCircles
    }

    fun getAllVisiblePlacemarks() = if (traversed) visiblePlacemarks else {
        visibleMapObjects.traverse(this)
        visiblePlacemarks
    }

    override fun onPolygonVisited(p0: PolygonMapObject) {
        // do nothing
    }

    override fun onCircleVisited(circle: CircleMapObject) {
        Log.d(TAG, "onCircleVisited: ")
        val circleData = circle.userData
        if (circleData is Long) {
            visibleCircles[circleData] = circle
        } else throw IllegalArgumentException("User data is not Long type")
    }

    override fun onPolylineVisited(p0: PolylineMapObject) {
        // do nothing
    }

    override fun onColoredPolylineVisited(p0: ColoredPolylineMapObject) {
        // do nothing
    }

    override fun onPlacemarkVisited(placemark: PlacemarkMapObject) {
        val placemarkData = placemark.userData ?: return
        if (placemarkData is Long) {
            visiblePlacemarks[placemarkData] = placemark
        } else throw IllegalArgumentException("User data is not Long type")
    }

    override fun onCollectionVisitEnd(p0: MapObjectCollection) {
        // do nothing
        traversed = true
    }

    override fun onCollectionVisitStart(p0: MapObjectCollection): Boolean {
        // do nothing
        return true
    }
}


class GeofenceLimitDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setTitle(R.string.premium_is_required_message)
            .setMessage(R.string.dialog_geofence_limit_for_free_version)
            .setIcon(R.drawable.ic_crown)
            .setPositiveButton(R.string.buy_premium_version_button) { _, _ ->
                val navController = NavHostFragment.findNavController(this)
                navController.navigate(R.id.premiumFragment)
                this@GeofenceLimitDialog.dismiss()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }
}