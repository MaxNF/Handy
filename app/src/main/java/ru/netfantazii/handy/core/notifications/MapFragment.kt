package ru.netfantazii.handy.core.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Circle
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import kotlinx.android.synthetic.main.map_fragment.*
import ru.netfantazii.handy.HandyApplication
import ru.netfantazii.handy.R
import ru.netfantazii.handy.databinding.MapFragmentBinding
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException

const val MAP_API_KEY = "3426ee1b-da34-4926-b4f4-df96fdb9a8eb"
var currentRadius: Float = 100.0f

class MapFragment : Fragment() {
    private val TAG = "MapFragment"
    private val fragmentArgs: MapFragmentArgs by navArgs()

    private lateinit var mapView: MapView
    private lateinit var viewModel: MapViewModel
    private lateinit var mapObjects: MapObjectCollection
    private val allLiveDataList = mutableListOf<LiveData<*>>()
    private var circleFillColor: Int = 0xFF21b843.toInt()
    private var circleStrokeColor: Int = 0x4A3dfc68
    private var circleStrokeWidth: Float = 0f

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

        MapKitFactory.setApiKey(MAP_API_KEY)
        MapKitFactory.initialize(context)
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
        val startingPoint = Point(59.945933, 30.320045)
        mapview.map.move(CameraPosition(startingPoint, 15.0f, 0.0f, 10.0f),
            Animation(Animation.Type.SMOOTH, 0f), null)
        mapObjects = mapView.map.mapObjects
        mapView.map.addInputListener(mapInputListener)
        subscribeToEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeFromEvents()
    }

    private fun subscribeToEvents() {
        viewModel.newDataReceived.observe(this, Observer {
            it.getContentIfNotHandled()?.let {

                val diffSearcher = CircleDiffSearcher(mapObjects, viewModel.circleMap)
                diffSearcher.getRemovedCircles().forEach { mapEntry ->
                    mapObjects.remove(mapEntry.value)
                }
                diffSearcher.getAddedCircles().forEach { mapEntry ->
                    addCircleWithId(mapEntry.key, mapEntry.value)
                    addPlacemark(mapEntry.value)
                }
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

    private fun addPlacemark(surroundingCircle: Circle) {
//todo
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
}

class CircleDiffSearcher(
    private val visibleMapObjects: MapObjectCollection,
    private val newCircles: kotlin.collections.Map<Long, Circle>
) : MapObjectVisitor {
    private val TAG = "CircleDiffSearcher"

    private var resultsReady = false
    private lateinit var removedCircles: kotlin.collections.Map<Long, CircleMapObject>
    private lateinit var addedCircles: kotlin.collections.Map<Long, Circle>

    private val oldCircles = mutableMapOf<Long, CircleMapObject>()

    public fun searchForDifferences() {
        if (!resultsReady) {
            visibleMapObjects.traverse(this)
            oldCircles.forEach { Log.d(TAG, "searchForDifferences: ${it.key}") }
            removedCircles = oldCircles.filter { newCircles[it.key] == null }
            val oldCirclesTransformed = oldCircles.mapValues { it.value.geometry }
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

    override fun onPolygonVisited(p0: PolygonMapObject) {
        // do nothing
    }

    override fun onCircleVisited(circle: CircleMapObject) {
        Log.d(TAG, "onCircleVisited: ")
        val circleData = circle.userData
        if (circleData is Long) {
            oldCircles[circleData] = circle
        } else throw IllegalArgumentException("User data is not Long type")
    }

    override fun onPolylineVisited(p0: PolylineMapObject) {
        // do nothing
    }

    override fun onColoredPolylineVisited(p0: ColoredPolylineMapObject) {
        // do nothing
    }

    override fun onPlacemarkVisited(p0: PlacemarkMapObject) {
        // do nothing
    }

    override fun onCollectionVisitEnd(p0: MapObjectCollection) {
        // do nothing
    }

    override fun onCollectionVisitStart(p0: MapObjectCollection): Boolean {
        // do nothing
        return true
    }
}