package ru.netfantazii.handy.core.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.main.MainActivity
import ru.netfantazii.handy.generated.callback.OnClickListener

class AboutFragment : Fragment() {
    companion object {
        const val DEVELOPER_EMAIL = "mailto:handyshoppingapp@gmail.com"
    }

    private val TAG = "AboutFragment"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.about_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).checkMenuItem(R.id.contactsFragment)
        val textView = view.findViewById<TextView>(R.id.about_text_view)
        val button = view.findViewById<MaterialButton>(R.id.contact_developer_button)
        button.setOnClickListener { contactDeveloper() }
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun contactDeveloper() {
        Log.d(TAG, "contactDeveloper: ")
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse(DEVELOPER_EMAIL)
        startActivity(Intent.createChooser(intent, "Send email"))
    }
}