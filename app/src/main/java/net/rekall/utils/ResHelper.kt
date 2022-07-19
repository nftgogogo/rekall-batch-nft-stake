package net.rekall.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.*


class ResHelper private constructor() {

    var context: Context? = null
        private set


    protected fun setContext(context: Context?): ResHelper {
        this.context = context
        return this
    }

    internal object LazyHolder {
        @Volatile
        var helper = ResHelper()
    }

    val contextResources: Resources
        get() {
            if (context == null) {
                throw RuntimeException("Please initialize at Applications")
            }
            return context!!.resources
        }

    companion object {

        @JvmStatic
        fun init(context: Context): ResHelper {
            get().setContext(context.applicationContext)
            return get()
        }


        @JvmStatic
        protected fun get(): ResHelper {
            return LazyHolder.helper
        }


        @JvmStatic
        fun getDrawable(@DrawableRes id: Int): Drawable {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                resources.getDrawable(id, null)
            } else resources.getDrawable(
                id
            )
        }

        @JvmStatic
        fun getColor(@ColorRes id: Int): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                resources.getColor(id, null)
            } else resources.getColor(id)
        }


        @JvmStatic
        fun getString(@StringRes id: Int): String {
            return resources.getString(id)
        }


        @JvmStatic
        fun getString(@StringRes id: Int, vararg formatArgs: Any?): String {
            return resources.getString(id, *formatArgs)
        }


        @JvmStatic
        fun getStringArray(@ArrayRes resId: Int): Array<String> {
            return resources.getStringArray(resId)
        }


        @JvmStatic
        fun getDimensionPixelOffsets(@DimenRes id: Int): Int {
            return resources.getDimensionPixelOffset(id)
        }


        @JvmStatic
        fun getDimension(@DimenRes id: Int): Float {
            return resources.getDimension(id)
        }


        @JvmStatic
        fun getDimensionPixelSize(@DimenRes id: Int): Int {
            return resources.getDimensionPixelSize(id)
        }


        @JvmStatic
        val resources: Resources
            get() = get().contextResources
    }
}