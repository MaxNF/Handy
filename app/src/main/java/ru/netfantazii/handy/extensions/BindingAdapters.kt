package ru.netfantazii.handy.extensions

import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import ru.netfantazii.handy.R
import ru.netfantazii.handy.core.preferences.ThemeColor
import ru.netfantazii.handy.core.preferences.getThemeColor
import ru.netfantazii.handy.model.HintType
import java.text.DateFormat
import java.util.*


private val TAG = "BindingAdapters"

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
    textView.alpha = if (dimmed) 0.5f else 1f
}

@BindingAdapter("app:stripeWidth")
fun setStripeWidth(view: View, percent: Float) {
    view.post {
        val maxWidth = (view.parent as View).width
        val widthToSet = (percent * maxWidth).toInt()
        val params = FrameLayout.LayoutParams(view.layoutParams)
        params.width = widthToSet
        view.layoutParams = params
    }
}

@BindingAdapter("app:formatCalendar")
fun formatCalendar(view: TextView, calendar: Calendar) {
    val formatter = DateFormat.getDateInstance(
        DateFormat.LONG)
    formatter.timeZone = calendar.timeZone
    val formatted = formatter.format(calendar.time)
    view.text = formatted
}

@BindingAdapter("app:downloadImageFromUri")
fun downloadWithGlide(view: ImageView, uri: Uri?) {
    if (uri == null) {
        view.setImageResource(R.mipmap.ic_launcher_round)
    } else {
        Glide.with(view).load(uri).transform(CropCircleWithBorderTransformation(0, 0))
            .into(view)
    }
}
