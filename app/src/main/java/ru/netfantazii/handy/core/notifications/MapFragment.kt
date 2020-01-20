package ru.netfantazii.handy.core.notifications

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.FusedLocationProviderClient
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.R
import ru.netfantazii.handy.databinding.MapFragmentBinding
import java.lang.IllegalArgumentException

const val MAP_API_KEY = "3426ee1b-da34-4926-b4f4-df96fdb9a8eb"

class MapFragment : Fragment() {
    private val TAG = "MapFragment"
    private val fragmentArgs: MapFragmentArgs by navArgs()

    private lateinit var mapView: MapView
    private lateinit var viewModel: MapViewModel
    private lateinit var mapObjects: MapObjectCollection
    private lateinit var locationManager: LocationManager
    private lateinit var geofenceIconProvider: ImageProvider
    private lateinit var userIconProvider: ImageProvider

    private val allLiveDataList = mutableListOf<LiveData<*>>()
    private var circleFillColor: Int = 0xFF21b843.toInt()
    private var circleStrokeColor: Int = 0x4A3dfc68
    private var circleStrokeWidth: Float = 1.3f

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

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        MapKitFactory.setApiKey(MAP_API_KEY)
        MapKitFactory.initialize(context)
        locationManager = MapKitFactory.getInstance().createLocationManager()
        geofenceIconProvider = ImageProvider.fromResource(context, R.drawable.ic_map_logo)
        userIconProvider = ImageProvider.fromResource(context, R.drawable.ic_person_pin_circle)
        createViewModel()
        super.onCreate(savedInstanceState)
    }

    private fun createViewModel() {
        val repository = (requireContext().applicationContext as HandyApplication).localRepository
        val currentCatalogId = fragmentArgs.catalogId
        viewModel =
            ViewModelProviders.of(this,
                MapVmFactory(repository,
                    currentCatalogId))
                .get(MapViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = MapFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mapView = view.findViewById<MapView>(R.id.mapview)
        mapObjects = mapView.map.mapObjects
        mapView.map.addInputListener(mapInputListener)
        subscribeToEvents()
        if (viewModel.lastCameraPosition == null) {
            moveToLastKnownLocation()
        } else {
            mapView.map.move(viewModel.lastCameraPosition!!)
            drawPinIfNotNull(viewModel.lastPinPosition)
        }
    }

    private fun drawPinIfNotNull(point: Point?) {
        point?.let { mapObjects.addPlacemark(it, userIconProvider) }
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
        FusedLocationProviderClient(context!!).lastLocation
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
        drawPinIfNotNull(point)
        viewModel.lastPinPosition = point
    }

    private fun subscribeToEvents() {
        viewModel.newDataReceived.observe(this, Observer {
            // нужно перерисовать геозоны после поворота экрана, поэтому не обнуляем контент Ивента
            // и используем метод peekContent()
            it.peekContent().let {

                val diffSearcher = CircleDiffSearcher(mapObjects, viewModel.circleMap)
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
            }
        })
        viewModel.findMyLocationClicked.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                moveToUserLocation()
            }
        })
    }

    private fun addCircleWithId(id: Long, circle: Circle) {
        val circleMapObject = mapObjects.addCircle(circle,
            circleFillColor,
            circleStrokeWidth,
            circleStrokeColor)

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

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
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

    private fun getSimpleCamPosition(point: Point): CameraPosition {
        return CameraPosition(point, 15.0f, 0.0f, 10.0f)
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


    public fun searchForDifferences() {
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