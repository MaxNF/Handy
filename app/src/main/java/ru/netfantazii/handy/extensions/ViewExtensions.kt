package ru.netfantazii.handy.extensions

import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.LinearInterpolator
import android.widget.EditText

fun View.fadeIn(): ViewPropertyAnimator {
    val animator = animate()
    if (alpha < 1f) {
        animator
            .alpha(1f)
            .setInterpolator(LinearInterpolator())
            .start()
    }
    return animator
}

fun View.fadeOut(): ViewPropertyAnimator {
    val animator = animate()
    if (alpha > 0f) {
        animator
            .alpha(0f)
            .setInterpolator(LinearInterpolator())
            .start()
    }
    return animator
}

fun View.shrink(): ViewPropertyAnimator {
    scaleX = 1f
    scaleY = 1f
    val animator = animate()
    animator
        .scaleX(0f)
        .scaleY(0f)
        .start()
    return animator
}

fun View.enlarge(): ViewPropertyAnimator {
    scaleX = 0f
    scaleY = 0f
    val animator = animate()
    animator
        .scaleX(1f)
        .scaleY(1f)
        .start()
    return animator
}

fun EditText.moveCursorToLastChar() {
    setSelection(length())
}