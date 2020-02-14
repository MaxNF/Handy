package ru.netfantazii.handy.core.contacts

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import ru.netfantazii.handy.NetworkViewModel
import ru.netfantazii.handy.R
import ru.netfantazii.handy.databinding.EditContactDialogBinding
import ru.netfantazii.handy.model.Contact
import ru.netfantazii.handy.model.ContactDialogAction

const val SECRET_LENGTH = 7
const val SECRET_PATTERN = "^a-zA-Z0-9"


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

// todo сделать работу через буфер (вью модел) а то слишком гемморно все эти данные сохранять при переворотах
class EditContactDialog() :
    BaseDialog() {
    constructor(action: ContactDialogAction, contact: Contact?) : this(){
        this.action = action
    }
    private lateinit var action: ContactDialogAction
    private var contact: Contact? = null

    private val TAG = "EditContactDialog"
    lateinit var dialog: AlertDialog
    lateinit var nameEditText: TextInputEditText
    lateinit var secretEditText: TextInputEditText


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.edit_contact_dialog, null)
        val binding = EditContactDialogBinding.bind(dialogView)

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

            // todo сделать анимации сообщений об ошибках
            if (nameHasDuplicates(name)) {
                // анимация ошибки контакт с таким именем уже есть
                return@setOnClickListener
            }
            if (secretLengthIsNotValid(secret)) {
                // анимация ошибки длина должна быть 7 символов
                return@setOnClickListener
            }
            if (secretPatternDoesNotMatch(secret)) {
                // анимация ошибки только цифры и буквы англ. алфавита
                return@setOnClickListener
            }
            applyAndDismiss(name, secret, dialog)
        }
    }

    private fun nameHasDuplicates(name: String) = dialogClickHandler.nameHasDuplicates(name)

    private fun secretLengthIsNotValid(secret: String) = secret.length != SECRET_LENGTH

    private fun secretPatternDoesNotMatch(secret: String) = Regex(SECRET_PATTERN).matches(secret)

    private fun applyAndDismiss(name: String, secret: String, dialog: AlertDialog) {
        when (action) {
            ContactDialogAction.RENAME -> {
                val renamedContact = Contact(name, contact!!.secret, contact!!.date, contact!!.isValid)
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

    private fun createDialog(dialogView: View): AlertDialog = AlertDialog.Builder(activity)
        .setTitle(R.string.dialog_buy_all_msg)
        .setView(dialogView)
        .setPositiveButton(R.string.apply_button, null)
        .setNegativeButton(R.string.dialog_cancel, null)
        .create()
}

class DeleteContactDialog(private val contact: Contact) : BaseDialog() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
            .setTitle("${contact.name} is going to be deleted. Are you sure? (this cannot be undone)")
            .setPositiveButton(R.string.dialog_yes) { _, _ ->
                dialogClickHandler.onDeleteYesClick(contact)
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }
}