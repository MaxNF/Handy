package ru.netfantazii.handy.ui.premium

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import ru.netfantazii.handy.R
import ru.netfantazii.handy.data.model.Constants

class RedeemDialog : DialogFragment() {
    private lateinit var textField: TextInputEditText
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.redeem_dialog, null)
        textField = dialogView.findViewById(R.id.redeem_edit_text)
        return createDialog(dialogView)
    }

    private fun createDialog(dialogView: View) =
        AlertDialog.Builder(activity, R.style.BaseDialogTheme)
            .setView(dialogView)
            .setTitle(getString(R.string.redeem_dialog_title))
            .setPositiveButton(R.string.redeem_button) { _, _ -> openGooglePlayRedeemCodePage() }
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()

    private fun openGooglePlayRedeemCodePage() {
        val uri = String.format(Constants.GOOGLE_PLAY_REDEEM_DEEPLINK_URL, textField.text)
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(intent)
    }
}