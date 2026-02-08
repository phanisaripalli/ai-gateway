package org.saripalli.aigateway.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class EncryptionService(
    @Value("\${encryption.key:#{null}}")
    private val encryptionKeyRaw: String?
) {

    private val algorithm = "AES/GCM/NoPadding"
    private val ivLength = 12 // 12 bytes for GCM
    private val tagLength = 128 // 128-bit auth tag
    private val secureRandom = SecureRandom()

    private val secretKey: SecretKeySpec by lazy {
        val raw = encryptionKeyRaw
            ?: throw IllegalStateException("ENCRYPTION_KEY is not configured. Set it via environment variable or encryption.key property.")
        // Hash to ensure exactly 32 bytes (AES-256)
        val keyBytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plainText: String): String {
        val iv = ByteArray(ivLength)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(tagLength, iv))
        val cipherText = cipher.doFinal(plainText.toByteArray())

        // Prepend IV to ciphertext
        val combined = iv + cipherText
        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(encryptedText: String): String {
        val combined = Base64.getDecoder().decode(encryptedText)

        if (combined.size < ivLength) {
            throw SecurityException("Invalid encrypted data")
        }

        val iv = combined.copyOfRange(0, ivLength)
        val cipherText = combined.copyOfRange(ivLength, combined.size)

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(tagLength, iv))

        return try {
            String(cipher.doFinal(cipherText))
        } catch (e: Exception) {
            throw SecurityException("Decryption failed: corrupted data or wrong encryption key")
        }
    }
}