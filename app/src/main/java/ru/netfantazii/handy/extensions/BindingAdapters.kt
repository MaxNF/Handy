package ru.netfantazii.handy.extensions

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netfantazii.handy.R

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

@BindingAdapter("app:productBackground")
fun setProductBackground(view: View, isBought: Boolean) {
    view.setBackgroundResource(
        if (isBought) R.drawable.bg_product_bought_w_ripple else R.drawable.bg_product_not_bought_w_ripple
    )
}

@BindingAdapter("app:marginTop")
fun setFrameLayoutMargin(view: View, value: Int)  {
    val layoutParams = view.layoutParams as RecyclerView.LayoutParams
    layoutParams.topMargin = dpToPx(value).toInt()
    view.layoutParams = layoutParams
}

@BindingAdapter("app:clipToOutline")
fun setClipToOutline(view: View, boolean: Boolean) {
    view.clipToOutline = boolean
}