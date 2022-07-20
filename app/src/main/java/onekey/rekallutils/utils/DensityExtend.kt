package onekey.rekallutils.utils

import android.content.Context
import android.util.TypedValue

/**
 *Author: 10041
 *Date: 2020/5/26 16:57
 *Description: float dp sp px
 */
fun Float.px2dp(context: Context): Int {
    val var2: Float = context.resources.displayMetrics.density
    return (this / var2 + 0.5f).toInt()
}

fun Float.px2sp(var0: Context): Int {
    val var2 = var0.resources.displayMetrics.scaledDensity
    return (this / var2 + 0.5f).toInt()
}

fun Float.dp2px(var0: Context?): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        var0?.resources?.displayMetrics
    )
}

fun Float.sp2px(var0: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        var0.resources.displayMetrics
    ).toInt()
}