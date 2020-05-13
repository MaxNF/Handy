package ru.netfantazii.handy.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import ru.netfantazii.handy.R

class ProductCounterLine : FrameLayout {
    private var stripeBackgroundColor = 0
    private var stripeForegroundColor = 0
    private var stripeBackgroundAlpha = 0f
    private var stripeForegroundAlpha = 0f
    private var stripeForegroundWidthFraction = 0f

    private val stripeBackgroundView: View
    private val stripeForegroundView: View

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
        stripeBackgroundView = View(context)
        stripeForegroundView = createStripeForegroundView()

        this.addView(stripeBackgroundView, 0,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        this.addView(stripeForegroundView, 1,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setStripeBackgroundColor(stripeBackgroundColor)
        setStripeForegroundColor(stripeForegroundColor)
        setStripeBackgroundAlpha(stripeBackgroundAlpha)
        setStripeForegroundAlpha(stripeForegroundAlpha)
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

    fun setStripeForegroundWidthFraction(fraction: Float) {
        stripeForegroundWidthFraction = fraction
    }


    private fun setAttributes(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ProductCounterLine, 0, 0)
        stripeBackgroundColor =
            a.getColor(R.styleable.ProductCounterLine_stripeBackgroundColor,
                defaultStripeBackgroundColor())
        stripeForegroundColor =
            a.getColor(R.styleable.ProductCounterLine_stripeForegroundColor,
                defaultStripeForegroundColor())
        stripeBackgroundAlpha =
            a.getFloat(R.styleable.ProductCounterLine_stripeBackgroundAlpha, 0f).coerceIn(0f, 1f)
        stripeForegroundAlpha =
            a.getFloat(R.styleable.ProductCounterLine_stripeForegroundAlpha, 1f).coerceIn(0f, 1f)
        stripeForegroundWidthFraction =
            a.getFloat(R.styleable.ProductCounterLine_stripeForegroundWidthFraction, 0f)
                .coerceIn(0f, 1f)
        a.recycle()
    }

    private fun defaultStripeBackgroundColor() =
        ContextCompat.getColor(context, android.R.color.white)

    private fun defaultStripeForegroundColor() =
        ContextCompat.getColor(context, android.R.color.holo_blue_light)

    private fun createStripeForegroundView() = object : View(context) {
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val height = MeasureSpec.getSize(heightMeasureSpec)
            val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
            val width = (stripeForegroundWidthFraction * maxWidth).toInt()
            setMeasuredDimension(width, height)
        }
    }
}