package net.rekall.utils.imagehelper.drawable

import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

/**
 * <pre>
 * author : leo
 * time   : 2019/07/18
 * desc   : URL Drawable
 * </pre>
 */
class URLDrawable : BitmapDrawable() {
    private var drawable: Drawable? = null
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (drawable != null) {
            drawable!!.draw(canvas)
        }
    }

    fun setDrawable(drawable: Drawable?): URLDrawable {
        this.drawable = drawable
        return this
    }
}