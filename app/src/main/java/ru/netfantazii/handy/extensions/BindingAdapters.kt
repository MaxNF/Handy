package ru.netfantazii.handy.extensions

import android.graphics.Paint
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
import ru.netfantazii.handy.data.BillingPurchaseTypes
import ru.netfantazii.handy.data.HintType
import ru.netfantazii.handy.data.ShopItem
import java.text.DateFormat
import java.util.*


private val TAG = "BindingAdapters"

@BindingAdapter("app:marginTop")
fun setFrameLayoutMargin(view: View, value: Int) {
    val layoutParams = view.layoutParams as RecyclerView.LayoutParams
    layoutParams.topMargin = dpToPx(value).toInt()
    view.layoutParams = layoutParams
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
            R.color.inactiveWindowColor) else
            ContextCompat.getColor(view.context, android.R.color.white))
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
fun formatCalendar(view: TextView, calendar: Calendar?) {
    if (calendar != null) {
        val formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
        formatter.timeZone = calendar.timeZone
        view.text = formatter.format(calendar.time)
    } else {
        view.text = view.context.getString(R.string.value_not_available)
    }
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

@BindingAdapter("app:isTextCrossed")
fun crossText(view: TextView, isCrossed: Boolean) {
    view.apply {
        paintFlags = if (isCrossed) {
            paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            paintFlags and (Paint.STRIKE_THRU_TEXT_FLAG).inv()
        }
    }
}

@BindingAdapter("app:premiumPlan")
fun setPremiumPlanValue(view: TextView, premium: ShopItem?) {
    when (premium?.type) {
        BillingPurchaseTypes.ONE_MONTH -> {
            view.text = view.context.getString(R.string.one_month_sub_plan)
        }
        BillingPurchaseTypes.ONE_YEAR -> {
            view.text = view.context.getString(R.string.one_year_sub_plan)
        }
        BillingPurchaseTypes.FOREVER -> {
            view.text = view.context.getString(R.string.forever_sub_plan)
        }
    }
}
