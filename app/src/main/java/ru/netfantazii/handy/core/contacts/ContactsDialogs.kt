package ru.netfantazii.handy.core.contacts

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders

interface DialogClickHandler {
    fun onDeleteYesClick()
    fun onEditYesClick()
}

open class BaseDialog : DialogFragment() {
    protected lateinit var dialogClickHandler: DialogClickHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogClickHandler = ViewModelProviders.of(
            parentFragment!!
        ).get(ContactsViewModel::class.java)
    }
}