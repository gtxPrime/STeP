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
 * Remediated under DPDP Act 2023 & MoTA Security Standard:
 * - Dynamic cryptographically random 16-byte salt per payload (zero hardcoded salt)
 * - 12-byte cryptographically random IV per encryption
 * - AES-256 with Galois/Counter Mode (GCM) and 128-bit authentication tag
 * - Zero plaintext fallbacks: prevents data leakage on failure
 * - Backward compatibility with legacy ENC: tokens
 */
object CryptoManager {

    private const val TAG = "STePCrypto"
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BITS = 128
    private const val IV_LENGTH_BYTES = 12
    private const val SALT_LENGTH_BYTES = 16
    private const val ITERATION_COUNT = 15000
    private const val KEY_LENGTH_BITS = 256

    private val secureRandom = SecureRandom()

    // Legacy fallback salt kept strictly for decrypting historical v1 data
    private val LEGACY_SALT = "MoTA_STEP_TRIBAL_SOVEREIGN_2026_NIC_SECURE".toByteArray(Charsets.UTF_8)

    /**
     * Derives a 256-bit AES key from the user identifier and dynamic salt using PBKDF2WithHmacSHA256.
     */
    private fun deriveKey(uid: String, salt: ByteArray): SecretKeySpec {
        val safeUid = uid.ifEmpty { "sovereign_scholar_default_step" }
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(safeUid.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BITS)
        val secret = factory.generateSecret(spec)
        return SecretKeySpec(secret.encoded, "AES")
    }

    /**
     * Encrypts plaintext with AES-256-GCM and a newly generated 16-byte random salt.
     * Returns: "ENC:v2:" + Base64(Salt[16] + IV[12] + CipherText + AuthTag[16]).
     * Fails securely: throws or returns empty string, NEVER returns unencrypted plaintext.
     */
    fun encrypt(plainText: String, uid: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val salt = ByteArray(SALT_LENGTH_BYTES)
            val iv = ByteArray(IV_LENGTH_BYTES)
            secureRandom.nextBytes(salt)
            secureRandom.nextBytes(iv)

            val key = deriveKey(uid, salt)
            val cipher = Cipher.getInstance(ALGORITHM)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)

            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(salt.size + iv.size + cipherBytes.size)

            System.arraycopy(salt, 0, combined, 0, salt.size)
            System.arraycopy(iv, 0, combined, salt.size, iv.size)
            System.arraycopy(cipherBytes, 0, combined, salt.size + iv.size, cipherBytes.size)

            "ENC:v2:" + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Sovereign encryption failed securely: ${e.message}")
            // Hardened: NEVER return plainText on failure
            ""
        }
    }

    /**
     * Decrypts an encrypted token. Supports:
     * - "ENC:v2:" (Dynamic Salt + IV + Ciphertext + GCM Tag)
     * - "ENC:" (Legacy v1 format for existing Firestore records)
     */
    fun decrypt(cipherTextWithPrefix: String, uid: String): String {
        if (!cipherTextWithPrefix.startsWith("ENC:")) return cipherTextWithPrefix

        return try {
            if (cipherTextWithPrefix.startsWith("ENC:v2:")) {
                val base64Content = cipherTextWithPrefix.removePrefix("ENC:v2:")
                val combined = Base64.decode(base64Content, Base64.NO_WRAP)

                val minLength = SALT_LENGTH_BYTES + IV_LENGTH_BYTES
                if (combined.size <= minLength) return ""

                val salt = ByteArray(SALT_LENGTH_BYTES)
                val iv = ByteArray(IV_LENGTH_BYTES)
                val cipherBytes = ByteArray(combined.size - minLength)

                System.arraycopy(combined, 0, salt, 0, SALT_LENGTH_BYTES)
                System.arraycopy(combined, SALT_LENGTH_BYTES, iv, 0, IV_LENGTH_BYTES)
                System.arraycopy(combined, minLength, cipherBytes, 0, cipherBytes.size)

                val key = deriveKey(uid, salt)
                val cipher = Cipher.getInstance(ALGORITHM)
                val parameterSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)

                val decryptedBytes = cipher.doFinal(cipherBytes)
                String(decryptedBytes, Charsets.UTF_8)
            } else {
                // Backward-compatible v1 decryption for existing records
                val base64Content = cipherTextWithPrefix.removePrefix("ENC:")
                val combined = Base64.decode(base64Content, Base64.NO_WRAP)
                if (combined.size < IV_LENGTH_BYTES) return ""

                val iv = ByteArray(IV_LENGTH_BYTES)
                val cipherBytes = ByteArray(combined.size - IV_LENGTH_BYTES)
                System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES)
                System.arraycopy(combined, IV_LENGTH_BYTES, cipherBytes, 0, cipherBytes.size)

                val key = deriveKey(uid, LEGACY_SALT)
                val cipher = Cipher.getInstance(ALGORITHM)
                val parameterSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)

                val decryptedBytes = cipher.doFinal(cipherBytes)
                String(decryptedBytes, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sovereign decryption failed securely: ${e.message}")
            ""
        }
    }
}
