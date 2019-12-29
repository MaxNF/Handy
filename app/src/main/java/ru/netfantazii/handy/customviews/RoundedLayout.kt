package ru.netfantazii.handy.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout

class RoundedLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private var path: Path? = null
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cornerRadius = 10f
        path = Path()
        path?.addRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()),
            cornerRadius,
            cornerRadius,
            Path.Direction.CW)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        path?.let {
            canvas?.clipPath(it)
        }
        super.dispatchDraw(canvas)
    }
}