package ru.netfantazii.handy.core.groupsandproducts

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import ru.netfantazii.handy.R

interface DialogClickHandler {
    fun onCancelAllYesClick()
    fun onBuyAllYesClick()
    fun onDeleteAllYesClick()
}

open class BaseDialog : DialogFragment() {
    protected lateinit var dialogClickHandler: DialogClickHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogClickHandler = ViewModelProviders.of(
            parentFragment!!
        ).get(GroupsAndProductsViewModel::class.java)
    }
}

class BuyAllDialog : BaseDialog() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setTitle(R.string.dialog_buy_all_msg)
            .setPositiveButton(R.string.dialog_mark_as_bought_action) { _, _ -> dialogClickHandler.onBuyAllYesClick() }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }
}

class CancelAllDialog : BaseDialog() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setTitle(R.string.dialog_cancel_all_msg)
            .setPositiveButton(R.string.dialog_mark_as_non_bought_action) { _, _ -> dialogClickHandler.onCancelAllYesClick() }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }
}

class DeleteAllDialog : BaseDialog() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setTitle(R.string.dialog_delete_all_msg)
            .setPositiveButton(R.string.dialog_clear_action) { _, _ -> dialogClickHandler.onDeleteAllYesClick() }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }
}