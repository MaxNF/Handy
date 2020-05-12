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
    private var stripeBackground = 0
    private var stripeColor = 0
    private var backgroundAlpha = 0f
    private var stripeAlpha = 0f
    private var stripeWidthPercent = 0f
    private var stripeHeight = 0

    private lateinit var stripeBackgroundView: View
    private lateinit var stripeForegroundView: View

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

    /**
     * @param dp height in dp format  */
    fun setStripeHeight(dp: Int) {
        val params = layoutParams
        params.height = dpToPx(dp).toInt()
        this.layoutParams = params
    }

    fun setForegroundWidthPercent(percent: Float) {
        val maxWidth = width
        val widthToSet = (percent.coerceIn(0f, 100f) * maxWidth).toInt()
        val params = stripeForegroundView.layoutParams
        params.width = widthToSet
        stripeForegroundView.layoutParams = params
    }

    private fun setAttributes(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ProductCounterLine, 0, 0)
        stripeBackground =
            a.getColor(R.styleable.ProductCounterLine_stripeBackground, defaultBackground())
        stripeColor =
            a.getColor(R.styleable.ProductCounterLine_stripeBackground, defaultColor())
        backgroundAlpha = a.getFloat(R.styleable.ProductCounterLine_backgroundAlpha, 0f)
        stripeAlpha = a.getFloat(R.styleable.ProductCounterLine_stripeAlpha, 0f)
        stripeWidthPercent = a.getFloat(R.styleable.ProductCounterLine_stripeWidthPercent, 0f)
        stripeHeight = a.getInt(R.styleable.ProductCounterLine_stripeHeight, dpToPx(2).toInt())
        a.recycle()
    }

    private fun defaultBackground() = ContextCompat.getColor(context, android.R.color.white)
    private fun defaultColor() = ContextCompat.getColor(context, android.R.color.holo_blue_light)


}