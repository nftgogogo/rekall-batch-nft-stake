package onekey.rekallutils.utils.wallet

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 *
 */
object Md5Utils {
    @JvmStatic
    fun md5(plainText: String): String {
        var secretBytes: ByteArray? = null
        secretBytes = try {
            MessageDigest.getInstance("md5").digest(
                plainText.toByteArray()
            )
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("has not md5 ")
        }
        var md5code = BigInteger(1, secretBytes).toString(16)
        for (i in 0 until 32 - md5code.length) {
            md5code = "0$md5code"
        }
        return md5code
    }

    fun a(var0: String): String {
        return try {
            val var1 = MessageDigest.getInstance("MD5")
            var1.update(var0.toByteArray())
            b(var1.digest())
        } catch (var2: NoSuchAlgorithmException) {
            var2.printStackTrace()
            ""
        }
    }

    fun a(var0: ByteArray?): String {
        return try {
            val var1 = MessageDigest.getInstance("MD5")
            var1.update(var0)
            b(var1.digest())
        } catch (var2: NoSuchAlgorithmException) {
            var2.printStackTrace()
            ""
        }
    }

    private fun b(var0: ByteArray): String {
        val var1 = StringBuffer(var0.size * 2)
        for (var2 in var0.indices) {
            var1.append(Character.forDigit((var0[var2].toInt() and 240) shr 4, 16))
            var1.append(Character.forDigit(var0[var2].toInt() and 15, 16))
        }
        return var1.toString()
    }
}