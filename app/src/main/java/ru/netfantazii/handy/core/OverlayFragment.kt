package ru.netfantazii.handy.core

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputEditText
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.catalogs.CatalogsFragment
import ru.netfantazii.handy.core.catalogs.CatalogsViewModel
import ru.netfantazii.handy.core.groupsandproducts.GroupsAndProductsViewModel
import ru.netfantazii.handy.databinding.OverlayFragmentBinding
import ru.netfantazii.handy.db.BaseEntity
import ru.netfantazii.handy.extensions.fadeIn
import ru.netfantazii.handy.extensions.hideKeyboard
import ru.netfantazii.handy.extensions.moveCursorToLastChar
import ru.netfantazii.handy.extensions.showKeyboard

const val BUNDLE_ACTION_KEY = "action_key"

const val OVERLAY_ACTION_CATALOG_CREATE = "overlay_catalog_create"
const val OVERLAY_ACTION_CATALOG_RENAME = "overlay_catalog_rename"
const val OVERLAY_ACTION_GROUP_CREATE = "overlay_group_create"
const val OVERLAY_ACTION_GROUP_RENAME = "overlay_group_rename"
const val OVERLAY_ACTION_PRODUCT_CREATE = "overlay_product_create"
const val OVERLAY_ACTION_PRODUCT_RENAME = "overlay_product_rename"

data class BufferObject(val action: String, var bufferObject: BaseEntity) {
    val name = ObservableField<String>()

    init {
        name.set(bufferObject.name)
        name.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                bufferObject.name = (sender as ObservableField<String>).get()!!
            }
        })
    }

    fun replaceObject(newObject: BaseEntity) {
        bufferObject = newObject
        name.set(bufferObject.name)
    }
}

interface OverlayActions {
    var overlayBuffer: BufferObject

    fun onOverlayBackgroundClick()
    fun onOverlayEnterClick()
}

class OverlayFragment : Fragment() {

    private lateinit var overlayActions: OverlayActions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayActions = if (parentFragment is CatalogsFragment) {
            ViewModelProviders.of(
                parentFragment!!
            ).get(CatalogsViewModel::class.java)
        } else {
            ViewModelProviders.of(
                parentFragment!!
            ).get(GroupsAndProductsViewModel::class.java)
        }
        overrideBackButton(overlayActions)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val overlayBinding = OverlayFragmentBinding.inflate(inflater, container, false)
        overlayBinding.overlayActions = overlayActions
        // Важно сразу установить данные в поле EditText, чтобы на момент вызова textField.moveCursorToLastChar()
        // данные уже были в поле ввода (иначе курсор не будет установлен на последнюю букву)
        overlayBinding.executePendingBindings()
        return overlayBinding.root
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        val textField = rootView.findViewById<TextInputEditText>(R.id.overlay_text_field)
        textField.requestFocus()
        textField.moveCursorToLastChar()
        addEnterButtonClickListener(textField)
        startFadeInAnimationAndShowKeyboard(rootView)
    }

    override fun onDetach() {
        super.onDetach()
        hideKeyboard(activity as Activity)
    }

    private fun startFadeInAnimationAndShowKeyboard(rootView: View) {
        rootView.fadeIn().setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                showKeyboard(activity as Activity)
            }
        })
    }

    private fun addEnterButtonClickListener(textField: TextInputEditText) {
        textField.setOnEditorActionListener { _, button: Int, _ ->
            if (button == EditorInfo.IME_ACTION_DONE) {
                if (overlayActions.overlayBuffer.name.get()!!.isEmpty()) {
                    overlayActions.onOverlayBackgroundClick()
                } else {
                    overlayActions.onOverlayEnterClick()
                }
                true
            } else {
                false
            }
        }
    }

    /**
     * Назначаем для кнопки "назад" то же действие, что я для нажатия на пустое место. При
     * необходимости можно создать новый коллбэк во вьюмодел и вызывать его.*/
    private fun overrideBackButton(overlayActions: OverlayActions) {
        requireActivity().onBackPressedDispatcher.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    overlayActions.onOverlayBackgroundClick()
                }
            })
    }


}

