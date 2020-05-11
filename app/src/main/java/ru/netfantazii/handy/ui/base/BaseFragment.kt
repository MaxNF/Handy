package ru.netfantazii.handy.ui.base

import android.graphics.drawable.NinePatchDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import ru.netfantazii.handy.ui.main.MainActivity
import ru.netfantazii.handy.R
import ru.netfantazii.handy.di.WrappedAdapter
import ru.netfantazii.handy.utils.extensions.doWithDelay
import ru.netfantazii.handy.utils.extensions.fadeIn
import ru.netfantazii.handy.utils.extensions.fadeOut
import javax.inject.Inject

open abstract class BaseFragment<Adapter : RecyclerView.Adapter<out RecyclerView.ViewHolder>> :
    Fragment() {
    private lateinit var hint: View

    @Inject
    lateinit var adapter: Adapter

    protected val allLiveDataList = mutableListOf<LiveData<*>>()
    protected lateinit var navController: NavController
    protected lateinit var recyclerView: RecyclerView

    @Inject
    protected lateinit var dragManager: RecyclerViewDragDropManager
    @Inject
    protected lateinit var guardManager: RecyclerViewTouchActionGuardManager
    @Inject
    protected lateinit var swipeManager: RecyclerViewSwipeManager
    @Inject
    @WrappedAdapter
    protected lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    @Inject
    protected lateinit var animator: RecyclerView.ItemAnimator
    @Inject
    protected lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        injectDependencies()
        createRecyclerView(view)
        createSnackbars(view)
        createHints(view)
        setUpFab(view)
        subscribeToEvents()
        initNavController()
        (activity as MainActivity).uncheckMenuItems()
        loadAds(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeFromEvents()
    }

    override fun onStop() {
        super.onStop()
        hideSnackbars()
    }

    private fun loadAds(view: View) {
        val adView = view.findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder()
            .build()
        adView.loadAd(adRequest)
    }

    protected fun showOverlay() {
        hideSnackbars()
        val overlayFragment = OverlayFragment()
        childFragmentManager.beginTransaction()
            .add(R.id.overlay_container, overlayFragment)
            .commit()
    }

    protected fun closeOverlay() {
        childFragmentManager.beginTransaction()
            .remove(childFragmentManager.fragments[0])
            .commit()
    }

    protected open fun unsubscribeFromEvents() {
        allLiveDataList.forEach { it.removeObservers(this) }
    }

    private fun createHints(view: View) {
        hint = view.findViewById(R.id.hint_group)
    }

    private fun initNavController() {
        navController = NavHostFragment.findNavController(this)
    }

    protected fun disableDragAndDrop() {
        dragManager.setInitiateOnLongPress(false)
    }

    protected fun enableDragAndDrop() {
        // перед включением drag режима нужна задержка, т.к. иначе иногда возникает баг с
        // анимацией после отмены свайпа
        doWithDelay(300) {
            dragManager.setInitiateOnLongPress(
                true
            )
        }
    }

    protected fun showHint() {
        hint.fadeIn()
    }

    protected fun hideHint() {
        hint.fadeOut()
    }

    protected fun refreshRecyclerView() {
        adapter.notifyDataSetChanged()
    }

    protected open fun createRecyclerView(view: View) {
        dragManager.setInitiateOnMove(false)
        dragManager.setInitiateOnTouch(false)
        dragManager.setInitiateOnLongPress(true)
        dragManager.draggingItemAlpha = 0.9f
        dragManager.draggingItemScale = 1.07f
        dragManager.dragStartItemAnimationDuration = 250
        dragManager.setDraggingItemShadowDrawable(ContextCompat.getDrawable(requireContext(),
            R.drawable.shadow_round_corners) as NinePatchDrawable)

        guardManager.setInterceptVerticalScrollingWhileAnimationRunning(true)
        guardManager.isEnabled = true

        recyclerView = view.findViewById(R.id.rv_list)
        recyclerView.adapter = wrappedAdapter
        recyclerView.itemAnimator = animator
        recyclerView.layoutManager = linearLayoutManager
    }

    /**
     * Следует вызвать когда вьюшка фрагмента уже будет сформирована (в onCreateView или onViewCreated).
     * Зависимости, которые внедряются тесно связаны с ресайклервью, который возможно получить только из
     * сформированной корневой вьюшки.
     * */
    protected abstract fun injectDependencies()
    protected abstract fun subscribeToEvents()
    protected abstract fun setUpFab(view: View)
    protected abstract fun createSnackbars(view: View)
    protected abstract fun hideSnackbars()
}