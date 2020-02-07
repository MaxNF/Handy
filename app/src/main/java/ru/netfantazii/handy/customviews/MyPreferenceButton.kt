package ru.netfantazii.handy.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.button.MaterialButton
import ru.netfantazii.handy.R

class MyPreferenceButton(
    context: Context,
    attrs: AttributeSet
) : Preference(context, attrs) {
    private val TAG = "MyPreferenceButton"

    lateinit var buttonView: MaterialButton
        private set

    private var cachedAction: ((v: View) -> Unit)? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        buttonView = holder.itemView.findViewById(R.id.delete_account_button)!!
        if (cachedAction != null) {
            buttonView.setOnClickListener(cachedAction)
            cachedAction = null
        }
    }

    fun setButtonAction(action: (v: View) -> Unit) {
        if (::buttonView.isInitialized) buttonView.setOnClickListener(action)
        else cachedAction = action
    }

    fun removeButtonAction() {
        if (::buttonView.isInitialized) {
            buttonView.setOnClickListener(null)
        }
    }
}