package ru.netfantazii.handy.extensions

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.preferences.ThemeColor
import ru.netfantazii.handy.core.preferences.getThemeColor
import ru.netfantazii.handy.db.HintType

@BindingAdapter("app:backgroundSrc")
fun setViewHolderBackground(view: View, colorResId: Int) {
    view.setBackgroundResource(colorResId)
}

@BindingAdapter("app:groupBackground")
fun setGroupBackground(view: View, isBought: Boolean) {
    view.setBackgroundResource(
        if (isBought) R.drawable.bg_group_bought else R.drawable.bg_group_not_bought
    )
}

@BindingAdapter("app:hasRipple")
fun setRipple(view: View, hasRipple: Boolean) {
    view.setBackgroundResource(
//        if (hasRipple) R.drawable.bg_group_ripple else android.R.color.transparent
        R.drawable.bg_group_ripple
    )
}

@BindingAdapter("app:productBackground")
fun setProductBackground(view: View, isBought: Boolean) {
    view.setBackgroundResource(
        if (isBought) R.drawable.bg_product_bought_w_ripple else R.drawable.bg_product_not_bought_w_ripple
    )
}

@BindingAdapter("app:marginTop")
fun setFrameLayoutMargin(view: View, value: Int) {
    val layoutParams = view.layoutParams as RecyclerView.LayoutParams
    layoutParams.topMargin = dpToPx(value).toInt()
    view.layoutParams = layoutParams
}

@BindingAdapter("app:clipToOutline")
fun setClipToOutline(view: View, boolean: Boolean) {
    view.clipToOutline = boolean
}

@BindingAdapter("app:hintText")
fun setHintText(view: TextView, type: HintType) {
    when (type) {
        HintType.COLLAPSE -> view.text = view.context.getString(R.string.hint_recipe_collapse)
        HintType.EXPAND -> view.text = view.context.getString(R.string.hint_recipe_expand)
        HintType.EMPTY_UNSORTED -> view.text =
            view.context.getString(R.string.hint_recipe_empty_unsorted)
        HintType.EMPTY -> view.text = view.context.getString(R.string.hint_recipe_empty)
    }
}

@BindingAdapter("app:isViewDimmed")
fun setViewDimmed(view: View, dimmed: Boolean) {
    view.setBackgroundColor(
        if (dimmed) ContextCompat.getColor(view.context,
            R.color.inactiveWindowColor) else getThemeColor(view.context,
            ThemeColor.MAIN_BACKGROUND_COLOR)
    )
}

@BindingAdapter("app:isTextDimmed")
fun setTextDimmed(textView: TextView, dimmed: Boolean) {
//    textView.setTextColor(
//        if (dimmed) ContextCompat.getColor(textView.context,
//            R.color.textColorGray) else getThemeColor(textView.context,
//            ThemeColor.TOOLBAR_ELEMENT_COLOR)
//    )
    textView.alpha = if (dimmed) 0.5f else 1f
}
