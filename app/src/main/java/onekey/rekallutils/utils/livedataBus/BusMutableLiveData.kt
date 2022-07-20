package app.easypocket.lib.utils.livedataBus2

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.HashMap

open class BusMutableLiveData<T> : MutableLiveData<T>() {
    private val observerMap: MutableMap<Observer<in T>, Observer<in T>> = HashMap()
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, observer)
        try {
            hook(observer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun observeForever(observer: Observer<in T>) {
        if (!observerMap.containsKey(observer)) {
            observerMap[observer] = ObserverWrapper(observer)
        }
        observerMap[observer]?.let {
            super.observeForever(it)
        }

    }

    override fun removeObserver(observer: Observer<in T>) {
        var realObserver: Observer<*>? = null
        realObserver = if (observerMap.containsKey(observer)) {
            observerMap.remove(observer)
        } else {
            observer
        }
        super.removeObserver(observer)
    }

    @Throws(Exception::class)
    private fun hook(observer: Observer<in T>) {
        val classLiveData = LiveData::class.java
        val fieldObservers = classLiveData.getDeclaredField("mObservers")
        fieldObservers.isAccessible = true
        val objectObservers = fieldObservers[this]
        val classObservers: Class<*> = objectObservers.javaClass
        val methodGet = classObservers.getDeclaredMethod("get", Any::class.java)
        methodGet.isAccessible = true
        val objectWrapperEntry = methodGet.invoke(objectObservers, observer)
        var objectWrapper: Any? = null
        if (objectWrapperEntry is Map.Entry<*, *>) {
            objectWrapper = objectWrapperEntry.value
        }
        if (objectWrapper == null) {
            throw NullPointerException("Wrapper can not be bull!")
        }
        val classObserverWrapper: Class<*>? = objectWrapper.javaClass.superclass
        val fieldLastVersion = classObserverWrapper!!.getDeclaredField("mLastVersion")
        fieldLastVersion.isAccessible = true
        val fieldVersion = classLiveData.getDeclaredField("mVersion")
        fieldVersion.isAccessible = true
        val objectVersion = fieldVersion[this]
        fieldLastVersion[objectWrapper] = objectVersion
    }
}