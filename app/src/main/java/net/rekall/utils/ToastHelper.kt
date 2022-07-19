package net.rekall.utils

import android.app.Application
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import kotlinx.coroutines.*

object ToastHelper {
    var appContext: Application? = null
    var toast: Toast? = null


    fun init(application: Application?) {
        appContext = application
    }


    internal fun checkInit() {
        if (appContext == null) {
            throw RuntimeException("Please initialize at Application")
        }
    }


    fun showMessage(@StringRes strRes: Int) {
        showMessage(ResHelper.getString(strRes), Toast.LENGTH_SHORT)
    }

    fun showMessage(@StringRes strRes: Int, duration: Int) {
        showMessage(ResHelper.getString(strRes), duration)
    }

    @JvmOverloads
    fun showMessage(
        charSequence: CharSequence?,
        duration: Int = Toast.LENGTH_SHORT
    ) {

        GlobalScope.launch(Dispatchers.Main) {
            checkInit()
            toast?.cancel()
            toast = Toast.makeText(appContext, "", Toast.LENGTH_SHORT)
            toast?.run {
                setText(charSequence)
                setDuration(duration)
                setGravity(
                    Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM,
                    0,
                    64f.dp2px(appContext!!).toInt()
                )
            }
            toast?.show()
        }


    }


    fun showMessageGravity(@StringRes strRes: Int, gravity: Int) {
        showMessageGravity(ResHelper.getString(strRes), gravity)
    }



    fun showMessageGravity(charSequence: CharSequence?, gravity: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            checkInit()
            toast?.cancel()
            toast = Toast.makeText(appContext, "", Toast.LENGTH_SHORT)
            toast?.run {
                setText(charSequence)
                setGravity(gravity, 0, 0)
            }
            toast?.show()
        }
    }
}