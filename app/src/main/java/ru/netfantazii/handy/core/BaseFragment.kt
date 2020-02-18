package ru.netfantazii.handy.core

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import ru.netfantazii.handy.MainActivity
import ru.netfantazii.handy.R
import ru.netfantazii.handy.extensions.doWithDelay
import ru.netfantazii.handy.extensions.fadeIn
import ru.netfantazii.handy.extensions.fadeOut

open abstract class BaseFragment<Adapter : RecyclerView.Adapter<out RecyclerView.ViewHolder>> : Fragment() {
    private lateinit var hint: View

    protected lateinit var adapter : Adapter
    protected lateinit var dragManager: RecyclerViewDragDropManager
    protected val allLiveDataList = mutableListOf<LiveData<*>>()
    protected lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createViewModel()
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        createRecyclerView(view)
        createSnackbars(view)
        createHints(view)
        setUpFab(view)
        subscribeToEvents()
        initNavController()
        (activity as MainActivity).uncheckActiveMenuItem()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeFromEvents()
    }

    override fun onStop() {
        super.onStop()
        hideSnackbars()
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

    protected abstract fun createViewModel()
    protected abstract fun createRecyclerView(view: View)
    protected abstract fun subscribeToEvents()
    protected abstract fun setUpFab(view: View)
    protected abstract fun createSnackbars(view: View)
    protected abstract fun hideSnackbars()
}