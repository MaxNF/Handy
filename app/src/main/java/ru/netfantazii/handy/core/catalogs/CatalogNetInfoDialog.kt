package ru.netfantazii.handy.core.catalogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.Event
import ru.netfantazii.handy.databinding.CatalogNetInfoDialogBinding
import ru.netfantazii.handy.model.Catalog
import ru.netfantazii.handy.model.database.CatalogNetInfoEntity

interface DialogClickHandler {
    val catalogAndNetInfoReceived: LiveData<Event<Pair<Catalog, CatalogNetInfoEntity>>>
}

class CatalogNetInfoDialog : DialogFragment() {
    private lateinit var dialogClickHandler: DialogClickHandler
    private lateinit var binding: CatalogNetInfoDialogBinding
    private val allLiveDataList = mutableListOf<LiveData<*>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogClickHandler =
            ViewModelProviders.of(parentFragment!!).get(CatalogsViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.catalog_net_info_dialog, null)
        binding = CatalogNetInfoDialogBinding.bind(dialogView)
        subscribeToEvents()
        return createDialog(dialogView)
    }

    private fun subscribeToEvents() {
        dialogClickHandler.catalogAndNetInfoReceived.observe(this, Observer { event ->
            val catalog = event.peekContent().first
            val catalogNetInfo = event.peekContent().second
            binding.catalog = catalog
            binding.catalogNetInfo = catalogNetInfo
        })
        allLiveDataList.add(dialogClickHandler.catalogAndNetInfoReceived)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeFromEvents()
    }

    private fun unsubscribeFromEvents() {
        allLiveDataList.forEach { it.removeObservers(this) }
    }

    private fun createDialog(dialogView: View): AlertDialog =
        AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_ok_button, null)
            .create()
}