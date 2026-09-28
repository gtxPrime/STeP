package com.step.app.security

import android.util.Base64
import android.util.Log
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * CryptoManager — Sovereign AES-256-GCM End-to-End Encryption
 * Encrypts student Personally Identifiable Information (PII) like Aadhaar, bank details,
 * and annual income before uploading to Firebase Firestore and external web endpoints.
 */
object CryptoManager {

    private const val TAG = "STePCrypto"
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BITS = 128
    private const val IV_LENGTH_BYTES = 12
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH_BITS = 256

    // Sovereign Application Pepper for MoTA cryptographic envelope
    private val SOVEREIGN_SALT = "MoTA_STEP_TRIBAL_SOVEREIGN_2026_NIC_SECURE".toByteArray(Charsets.UTF_8)
    private val secureRandom = SecureRandom()

    /**
     * Derives a deterministic 256-bit AES key for the user using PBKDF2.
     */
    private fun deriveKey(uid: String): SecretKeySpec {
        val safeUid = uid.ifEmpty { "default_scholar_step_984" }
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(safeUid.toCharArray(), SOVEREIGN_SALT, ITERATION_COUNT, KEY_LENGTH_BITS)
        val secret = factory.generateSecret(spec)
        return SecretKeySpec(secret.encoded, "AES")
    }

    /**
     * Encrypts plaintext with AES-256-GCM and returns Base64(IV + CipherText + AuthTag).
     */
    fun encrypt(plainText: String, uid: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val key = deriveKey(uid)
            val iv = ByteArray(IV_LENGTH_BYTES)
            secureRandom.nextBytes(iv)

            val cipher = Cipher.getInstance(ALGORITHM)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)

            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

            "ENC:" + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed: ${e.message}")
            plainText
        }
    }

    /**
     * Decrypts an "ENC:" prefixed Base64 string.
     * If not prefixed or decryption fails, safely returns original text.
     */
    fun decrypt(cipherTextWithPrefix: String, uid: String): String {
        if (!cipherTextWithPrefix.startsWith("ENC:")) return cipherTextWithPrefix
        return try {
            val base64Content = cipherTextWithPrefix.removePrefix("ENC:")
            val combined = Base64.decode(base64Content, Base64.NO_WRAP)

            if (combined.size < IV_LENGTH_BYTES) return cipherTextWithPrefix

            val iv = ByteArray(IV_LENGTH_BYTES)
            val cipherBytes = ByteArray(combined.size - IV_LENGTH_BYTES)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES)
            System.arraycopy(combined, IV_LENGTH_BYTES, cipherBytes, 0, cipherBytes.size)

            val key = deriveKey(uid)
            val cipher = Cipher.getInstance(ALGORITHM)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)

            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.w(TAG, "Decryption fallback to original: ${e.message}")
            cipherTextWithPrefix
        }
    }
}
