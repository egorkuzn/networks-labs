package org.example.socks.auth

import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.SecretKeySpec


class PasswordCipher(key: String) {
    private val secretKey: SecretKey
    private val encoder: Base64.Encoder
    private val decoder: Base64.Decoder

    init {
        secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
        encoder = Base64.getEncoder()
        decoder = Base64.getDecoder()
    }

    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    fun encrypt(plainText: String): String {
        val plainTextByte = plainText.toByteArray()
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedByte = cipher.doFinal(plainTextByte)
        return encoder.encodeToString(encryptedByte)
    }

    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class
    )
    fun decrypt(encrypted: String?): String {
        val encryptedByte = decoder.decode(encrypted)
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return cipher.doFinal(encryptedByte).decodeToString()
    }
}
