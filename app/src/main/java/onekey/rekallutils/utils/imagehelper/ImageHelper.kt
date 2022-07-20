package net.rekall.utils.imagehelper

import androidx.recyclerview.widget.RecyclerView
import net.rekall.utils.imagehelper.engine.ImageEngine

import org.jetbrains.annotations.NotNull


class ImageHelper private constructor(var mBuilder: Builder) {
    private val imageLoadListener: RecyclerView.OnScrollListener by lazy {
        newImageLoadListener()
    }


    private fun newImageLoadListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    getEngine().resumeLoadImage(recyclerView.context)
                } else {
                    getEngine().pauseLoadImage(recyclerView.context)
                }
            }
        }
    }


    fun getEngine(): ImageEngine {
        if (mBuilder.engine == null) {
            throw RuntimeException("Please add ImageEngine!!!")
        }
        return mBuilder.engine!!
    }


    class Builder {
        internal var engine: ImageEngine? = null

        fun setEngine(engine: ImageEngine): Builder {
            this.engine = engine
            return this
        }

    }

    companion object {
        private var mBuilder: Builder? = null
        private var INSTANCE: ImageHelper? = null


        @JvmStatic
        fun init(@NotNull builder: Builder): ImageHelper {
            mBuilder = builder
            INSTANCE = ImageHelper(builder)
            return INSTANCE!!
        }


        @JvmStatic
        fun get(): ImageHelper {
            if (INSTANCE == null) {
                throw RuntimeException("Please initialize ImageHelper in Application")
            }
            return INSTANCE!!
        }


        fun isInit(): Boolean {
            return INSTANCE != null
        }

        @JvmStatic
        fun bindImageLoadScrollIdle(recyclerView: RecyclerView?) {
            if (recyclerView == null || !isInit()) {
                return
            }
            recyclerView.addOnScrollListener(get().imageLoadListener)
        }

    }
}
