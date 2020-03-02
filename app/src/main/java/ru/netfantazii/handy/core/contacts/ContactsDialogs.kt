package ru.netfantazii.handy.core.contacts

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import ru.netfantazii.handy.core.main.NetworkViewModel
import ru.netfantazii.handy.R
import ru.netfantazii.handy.databinding.EditContactDialogBinding
import ru.netfantazii.handy.extensions.showShortToast
import ru.netfantazii.handy.data.Contact
import ru.netfantazii.handy.data.ContactDialogAction
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException

interface DialogClickHandler {
    fun onDeleteYesClick(contact: Contact)
    fun onEditYesClick(action: ContactDialogAction, contact: Contact)
    fun nameHasDuplicates(name: String): Boolean
}

open class BaseDialog : DialogFragment() {
    protected lateinit var dialogClickHandler: DialogClickHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogClickHandler = ViewModelProviders.of(
            activity!!
        ).get(NetworkViewModel::class.java)
    }
}

class EditContactDialog :
    BaseDialog() {
    private lateinit var viewModel: NetworkViewModel

    private val TAG = "EditContactDialog"
    private lateinit var dialog: AlertDialog
    private lateinit var nameEditText: TextInputEditText
    private lateinit var secretEditText: TextInputEditText
    private lateinit var action: ContactDialogAction
    private var contact: Contact? = null

    private val secretMaxLength = 7
    private val secretAllowablePattern = "[^a-zA-Z0-9]"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.edit_contact_dialog, null)
        val binding = EditContactDialogBinding.bind(dialogView)
        viewModel = ViewModelProviders.of(activity!!).get(NetworkViewModel::class.java)
        action = viewModel.dialogBuffer.first
        contact = viewModel.dialogBuffer.second

        binding.action = action
        binding.contact = contact
        nameEditText = binding.nameEditText
        secretEditText = binding.secretEditText

        dialog = createDialog(dialogView)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = nameEditText.text.toString()
            val secret = secretEditText.text.toString()

            if (name.isBlank()) {
                showShortToast(requireContext(),
                    getString(R.string.contact_dialog_name_blank_error))
                return@setOnClickListener
            }

            if (nameHasDuplicates(name)) {
                showShortToast(requireContext(),
                    getString(R.string.contact_dialog_name_duplicate_error))
                return@setOnClickListener
            }
            if (secretLengthIsNotValid(secret)) {
                showShortToast(requireContext(),
                    getString(R.string.contact_dialog_secret_length_error))
                return@setOnClickListener
            }
            if (secretPatternDoesNotMatch(secret)) {
                showShortToast(requireContext(),
                    getString(R.string.contact_dialog_secret_pattern_error))
                return@setOnClickListener
            }
            applyAndDismiss(name, secret, dialog)
        }
    }

    private fun nameHasDuplicates(name: String) = dialogClickHandler.nameHasDuplicates(name)

    private fun secretLengthIsNotValid(secret: String) = secret.length != secretMaxLength

    private fun secretPatternDoesNotMatch(secret: String) =
        Regex(secretAllowablePattern).containsMatchIn(secret)

    private fun applyAndDismiss(name: String, secret: String, dialog: AlertDialog) {
        when (action) {
            ContactDialogAction.RENAME -> {
                val renamedContact =
                    Contact(name, contact!!.secret, contact!!.date, contact!!.isValid)
                dialogClickHandler.onEditYesClick(action, renamedContact)
                dialog.dismiss()
            }
            ContactDialogAction.CREATE -> {
                val newContact = Contact(name, secret)
                dialogClickHandler.onEditYesClick(action, newContact)
                dialog.dismiss()
            }
            ContactDialogAction.RENAME_NOT_VALID -> {
                dialog.dismiss()
            }
        }
    }

    private fun createDialog(dialogView: View) =
        AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setView(dialogView)
            .setPositiveButton(R.string.apply_button, null)
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()

}

class DeleteContactDialog : BaseDialog() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel = ViewModelProviders.of(activity!!).get(NetworkViewModel::class.java)
        val action = viewModel.dialogBuffer.first
        val contact = viewModel.dialogBuffer.second
            ?: throw IllegalArgumentException("Contact can't be null here!")
        if (action != ContactDialogAction.DELETE) throw UnsupportedOperationException("Action should be \"DELETE\"")

        return AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setTitle(getString(R.string.delete_contact_dialog_title))
            .setMessage(getString(R.string.delete_contact_dialog_message, contact.name))
            .setPositiveButton(R.string.dialog_delete_action) { _, _ ->
                dialogClickHandler.onDeleteYesClick(contact)
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }
}