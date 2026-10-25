package com.example.fotobitacora.utils

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** La contraseña NO se almacena: solo su sal aleatoria y su hash PBKDF2. */
object PasswordUtils {
    private const val ITERATIONS = 150_000
    private const val HASH_BITS = 256

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hash(password: String, saltBase64: String): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val chars = password.toCharArray()
        val spec = PBEKeySpec(chars, salt, ITERATIONS, HASH_BITS)
        return try {
            val result = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
            Base64.encodeToString(result, Base64.NO_WRAP)
        } finally {
            spec.clearPassword()
            chars.fill('\u0000')
        }
    }

    fun verify(password: String, salt: String, savedHash: String): Boolean {
        val actual = Base64.decode(hash(password, salt), Base64.NO_WRAP)
        val expected = Base64.decode(savedHash, Base64.NO_WRAP)
        return MessageDigest.isEqual(actual, expected)
    }
}
