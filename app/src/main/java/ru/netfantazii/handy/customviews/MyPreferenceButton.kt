package ru.netfantazii.handy.customviews

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.button.MaterialButton
import ru.netfantazii.handy.R

class MyPreferenceButton(
    context: Context,
    attrs: AttributeSet
) : Preference(context, attrs) {

    private val TAG = "MyPreferenceButton"

    private lateinit var delAccButton: MaterialButton
    private lateinit var copySecretButton: ImageButton
    private lateinit var getNewSecretButton: ImageButton
    private lateinit var secretCodeTextView: TextView

    private var cachedDeleteAccAction: ((v: View) -> Unit)? = null
    private var cachedCopySecretAction: ((v: View) -> Unit)? = null
    private var cachedGetNewSecretAction: ((v: View) -> Unit)? = null
    private var cachedSecretCode: String? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val threadName = Thread.currentThread().name
        Log.d(TAG, "onBindViewHolder: $threadName")
        delAccButton = holder.itemView.findViewById(R.id.delete_account_button)!!
        copySecretButton = holder.itemView.findViewById(R.id.copy_secret_code_button)!!
        getNewSecretButton = holder.itemView.findViewById(R.id.new_secret_code_button)!!
        secretCodeTextView = holder.itemView.findViewById(R.id.secret_code)!!

        if (cachedDeleteAccAction != null) {
            delAccButton.setOnClickListener(cachedDeleteAccAction)
            cachedDeleteAccAction = null
        }

        if (cachedCopySecretAction != null) {
            copySecretButton.setOnClickListener(cachedCopySecretAction)
            cachedCopySecretAction = null
        }

        if (cachedGetNewSecretAction != null) {
            getNewSecretButton.setOnClickListener(cachedGetNewSecretAction)
            cachedGetNewSecretAction = null
        }

        if (cachedSecretCode != null) {
            secretCodeTextView.text = cachedSecretCode
            cachedSecretCode = null
        }
    }

    fun setDeleteAccountAction(action: (v: View) -> Unit) {
        if (::delAccButton.isInitialized) delAccButton.setOnClickListener(action)
        else cachedDeleteAccAction = action
    }

    fun setCopySecretAction(action: (v: View) -> Unit) {
        if (::copySecretButton.isInitialized) copySecretButton.setOnClickListener(action)
        else cachedCopySecretAction = action
    }

    fun setGetNewSecretAction(action: (v: View) -> Unit) {
        if (::getNewSecretButton.isInitialized) getNewSecretButton.setOnClickListener(action)
        else cachedGetNewSecretAction = action
    }

    fun setNewSecretToView(secret: String) {
        if (::secretCodeTextView.isInitialized) {
            secretCodeTextView.text = secret
        } else cachedSecretCode = secret
    }

    fun removeDeleteAccountAction() {
        if (::delAccButton.isInitialized) {
            delAccButton.setOnClickListener(null)
        }
    }

    fun removeCopySecretAction() {
        if (::copySecretButton.isInitialized) {
            copySecretButton.setOnClickListener(null)
        }
    }

    fun removeGetNewSecretAction() {
        if (::getNewSecretButton.isInitialized) {
            getNewSecretButton.setOnClickListener(null)
        }
    }
}