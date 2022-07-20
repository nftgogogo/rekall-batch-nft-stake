package app.easypocket.lib.utils.livedataBus2

import androidx.lifecycle.Observer

open class ObserverWrapper<T>(observer: Observer<T>?) : Observer<T> {
    private val observer: Observer<T>? = observer
    override fun onChanged(t: T?) {
        if (observer != null) {
            if (isCallOnObserve) {
                return
            }
            observer.onChanged(t)
        }
    }

    private val isCallOnObserve: Boolean
        private get() {
            val stackTrace = Thread.currentThread().stackTrace
            if (stackTrace != null && stackTrace.isNotEmpty()) {
                for (element in stackTrace) {
                    if ("android.arch.lifecycle.LiveData" == element.className && "observeForever" == element.methodName) {
                        return true
                    }
                }
            }
            return false
        }

}