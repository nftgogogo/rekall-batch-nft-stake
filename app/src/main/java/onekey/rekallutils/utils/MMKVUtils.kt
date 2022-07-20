package onekey.rekallutils.utils

import android.os.Parcelable
import com.tencent.mmkv.MMKV

class MMKVUtils {

    companion object {

        private val instance: MMKV by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            MMKV.defaultMMKV()
        }

        @JvmStatic
        fun get(): MMKV {
            return instance
        }

        @JvmStatic
        fun save(key: String, value: Any) {
            when (value) {
                is Int -> get().encode(key, value)
                is Float -> get().encode(key, value)
                is ByteArray -> get().encode(key, value)
                is Long -> get().encode(key, value)
                is Double -> get().encode(key, value)
                is String -> get().encode(key, value)
                is Boolean -> get().encode(key, value)
                is Parcelable -> get().encode(key, value)
                is Set<*> -> {
                    throw RuntimeException("pealse to use saveSet Function")
                }
                else -> {
                    throw  RuntimeException("it is not support this value now")
                }
            }
        }

        @JvmStatic
        fun getInt(key: String, value: Int): Int {
            return get().decodeInt(key, value)
        }

        @JvmStatic
        fun getDouble(key: String, value: Double): Double {
            return get().decodeDouble(key, value)
        }

        @JvmStatic
        fun getBool(key: String, boolean: Boolean): Boolean {
            return get().decodeBool(key, boolean)
        }

        @JvmStatic
        fun getString(key: String, str: String = ""): String {
            return get().decodeString(key, str)?:str
        }

        @JvmStatic
        fun getLong(key: String, long: Long): Long {
            return get().decodeLong(key, long)
        }

        @JvmStatic
        fun <T : Parcelable> getParcelable(key: String, value: T): T {
            return get().decodeParcelable(key, value::class.java)?:value
        }

        @JvmStatic
        fun getSet(key: String): Set<String> {
            return get().decodeStringSet(key)?: setOf()
        }


    }
}