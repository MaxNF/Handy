package ru.netfantazii.handy.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import ru.netfantazii.handy.R
import ru.netfantazii.handy.utils.extensions.dpToPx

class ProductCounterLine : FrameLayout {
    private var stripeBackgroundColor = 0
    private var stripeForegroundColor = 0
    private var stripeBackgroundAlpha = 0f
    private var stripeForegroundAlpha = 0f
    private var stripeForegroundWidthPercent = 0f

    private lateinit var stripeBackgroundView: View
    private lateinit var stripeForegroundView: View

    constructor(context: Context) : super(context) {
        setAttributes(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        setAttributes(attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        setAttributes(attrs)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.product_counter_line, this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
//        val backgroundView = getChildAt(0) as View
//        val backgroundParams = backgroundView.layoutParams
//        val stripeView = getChildAt(1) as View
//        val stripeParams = stripeView.layoutParams

        stripeBackgroundView = findViewById(R.id.stripe_background)
        stripeForegroundView = findViewById(R.id.stripe_foreground)


    }

    fun setStripeBackgroundColor(color: Int) {
        stripeBackgroundView.setBackgroundColor(color)
    }

    fun setStripeForegroundColor(color: Int) {
        stripeForegroundView.setBackgroundColor(color)
    }

    fun setStripeBackgroundAlpha(alpha: Float) {
        stripeBackgroundView.alpha = alpha
    }

    fun setStripeForegroundAlpha(alpha: Float) {
        stripeForegroundView.alpha = alpha
    }

    fun setStripeForegroundWidthPercent(percent: Float) {
        val maxWidth = width
        val widthToSet = (percent.coerceIn(0f, 100f) * maxWidth).toInt()
        val params = stripeForegroundView.layoutParams
        params.width = widthToSet
        stripeForegroundView.layoutParams = params
    }

    private fun setAttributes(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ProductCounterLine, 0, 0)
        stripeBackgroundColor =
            a.getColor(R.styleable.ProductCounterLine_stripeBackgroundColor, defaultBackground())
        stripeForegroundColor =
            a.getColor(R.styleable.ProductCounterLine_stripeBackgroundColor, defaultColor())
        stripeBackgroundAlpha = a.getFloat(R.styleable.ProductCounterLine_stripeBackgroundAlpha, 0f)
        stripeForegroundAlpha = a.getFloat(R.styleable.ProductCounterLine_stripeForegroundAlpha, 0f)
        stripeForegroundWidthPercent = a.getFloat(R.styleable.ProductCounterLine_stripeForegroundWidthPercent, 0f)
        a.recycle()
    }

    private fun defaultBackground() = ContextCompat.getColor(context, android.R.color.white)
    private fun defaultColor() = ContextCompat.getColor(context, android.R.color.holo_blue_light)


}