package net.rekall.utils.wallet

import android.util.Base64
import java.nio.charset.Charset
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object AESUtils {


    fun encryptString(content: String, password: String): String {
        try {
            return encrypt(password, content) ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun decryptString(content: String, password: String): String {
        try {
            return decrypt(password, content) ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private const val KEY_LENGTH = 16

    private const val DEFAULT_VALUE = "0"


    @Throws(Exception::class)
    fun encrypt(key: String, src: String): String? {
        var src = src
        src = Base64.encodeToString(src.toByteArray(), Base64.DEFAULT)
        val rawKey = toMakeKey(key, KEY_LENGTH, DEFAULT_VALUE).toByteArray()
        var result =
            getBytes(rawKey, src.toByteArray(charset("utf-8")), Cipher.ENCRYPT_MODE)
        result = Base64.encode(result, Base64.DEFAULT)
        return String(result, Charset.defaultCharset())
    }


    @Throws(Exception::class)
    fun decrypt(key: String, encrypted: String): String? {
        val rawKey = toMakeKey(key, KEY_LENGTH, DEFAULT_VALUE).toByteArray()
        var enc: ByteArray = encrypted.toByteArray(Charset.defaultCharset())
        enc = Base64.decode(enc, Base64.DEFAULT)
        var result = getBytes(rawKey, enc, Cipher.DECRYPT_MODE)
        result = Base64.decode(result, Base64.DEFAULT)
        return String(result, Charset.forName("utf-8"))
    }


    private fun toMakeKey(
        key: String,
        length: Int,
        text: String
    ): String {
        var key = key
        val strLen = key.length
        if (strLen < length) {
            val builder = StringBuilder()
            builder.append(key)
            for (i in 0 until length - strLen) {
                builder.append(text)
            }
            key = builder.toString()
        } else if (strLen > length) {
            key = key.subSequence(0, 16).toString()
        }
        return key
    }


    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    private fun getBytes(
        key: ByteArray,
        src: ByteArray,
        mode: Int
    ): ByteArray? {
        val secretKeySpec =
            SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(mode, secretKeySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(src)
    }
}