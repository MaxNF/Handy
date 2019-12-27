package ru.netfantazii.handy.core

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ru.netfantazii.handy.R

open abstract class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        createRecyclerView(view)
        createSnackbars(view)
        createHints(view)
        setUpFab(view)
        subscribeToEvents()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeFromEvents()
    }

    protected fun showOverlay() {
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

    protected abstract fun createViewModel()
    protected abstract fun createRecyclerView(view: View)
    protected abstract fun subscribeToEvents()
    protected abstract fun unsubscribeFromEvents()
    protected abstract fun setUpFab(view: View)
    protected abstract fun createSnackbars(view: View)
    protected abstract fun createHints(view: View)
}