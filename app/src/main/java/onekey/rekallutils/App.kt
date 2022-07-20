package onekey.rekallutils

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.scwang.smart.refresh.footer.BallPulseFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.tencent.mmkv.MMKV
import onekey.rekallutils.utils.AppFilePath
import onekey.rekallutils.utils.ResHelper
import onekey.rekallutils.utils.ToastHelper
import onekey.rekallutils.utils.imagehelper.ImageHelper
import onekey.rekallutils.utils.imagehelper.engine.GlideImageEngine
import org.litepal.LitePal


class App : Application() {

    lateinit var cachePath:String
    override fun onCreate() {
        app = this
        cachePath = this.cacheDir.absolutePath + "/netcache"
        super.onCreate()
        ToastHelper.init(this)
        ResHelper.init(this)
        AppFilePath.init(this)
        LitePal.initialize(this)
        ImageHelper.init(ImageHelper.Builder().setEngine(GlideImageEngine()))
        MMKV.initialize(this)
    }
    companion object {

        private lateinit var app: App

        fun get(): App {
            return app
        }

        init {

            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context: Context, layout: RefreshLayout ->
                layout.setPrimaryColorsId(R.color.mainColor, R.color.white)
                val materialHeader = MaterialHeader(context)
                materialHeader.setColorSchemeResources(R.color.mainColor)
                return@setDefaultRefreshHeaderCreator materialHeader
            }

            SmartRefreshLayout.setDefaultRefreshFooterCreator { context: Context, layout: RefreshLayout ->
                layout.setPrimaryColorsId(R.color.mainColor, R.color.white)
                return@setDefaultRefreshFooterCreator BallPulseFooter(context)
            }

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}