package app.easypocket.lib.utils.livedataBus2

import androidx.lifecycle.MutableLiveData
import java.util.*

class LiveDataBus2 private constructor() {
    private val bus: MutableMap<String, BusMutableLiveData<Any>>

    private object SingletonHolder {
        val DEFAULT_BUS by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            LiveDataBus2()
        }
    }

    fun <T> with(key: String, type: Class<T>?): MutableLiveData<T> {
        if (!bus.containsKey(key)) {
            bus[key] = BusMutableLiveData()
        }
        return bus[key] as MutableLiveData<T>
    }

    fun with(key: String): MutableLiveData<Any> {
        return with(key, Any::class.java)
    }




    companion object {
        fun get(): LiveDataBus2 {
            return SingletonHolder.DEFAULT_BUS
        }
    }

    init {
        bus = HashMap()
    }
}