package com.offcourse.picko.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import timber.log.Timber

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "picko_session_key"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val KEY_SIZE_BITS = 256
private const val IV_SIZE_BYTES = 12
private const val TAG_SIZE_BYTES = 16
private const val BITS_PER_BYTE = 8
private val Charset = StandardCharsets.UTF_8

/**
 * Android Keystore의 AES-256-GCM 키로 암호화한다. 키는 Keystore 밖으로 나오지 않고 하드웨어 보호는 강제하지 않는다.
 * 암호문 형식: Base64(IV 12바이트 + 암호문 + GCM 태그)
 */
class CryptoManagerImpl @Inject constructor() : CryptoManager {
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    /** 키를 쓰지 못하면 키를 지우고 다시 만들어 한 번만 재시도한다. */
    override fun encrypt(plaintext: String, aadContext: AadContext): String {
        try {
            return encryptWith(getOrCreateKey(), plaintext, aadContext)
        } catch (e: Exception) {
            Timber.w(e, "암호화 실패. 키를 다시 만들어 재시도한다")
        }

        return try {
            resetKey()
            encryptWith(getOrCreateKey(), plaintext, aadContext)
        } catch (e: Exception) {
            throw EncryptionFailedException(e)
        }
    }

    override fun decrypt(ciphertext: String, aadContext: AadContext): String {
        val bytes = try {
            Base64.decode(ciphertext, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            throw DecryptionMalformedInputException(e)
        }
        if (bytes.size <= IV_SIZE_BYTES + TAG_SIZE_BYTES) throw DecryptionMalformedInputException()

        // 키를 얻지 못하면 기존 암호문은 다시 풀 수 없다.
        val key = try {
            getOrCreateKey()
        } catch (e: Exception) {
            throw DecryptionAuthFailedException(e)
        }

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BYTES * BITS_PER_BYTE, bytes, 0, IV_SIZE_BYTES))
                updateAAD(aadContext.raw.toByteArray(Charset))
            }
            String(cipher.doFinal(bytes, IV_SIZE_BYTES, bytes.size - IV_SIZE_BYTES), Charset)
        } catch (e: GeneralSecurityException) {
            throw DecryptionAuthFailedException(e)
        }
    }

    private fun encryptWith(key: SecretKey, plaintext: String, aadContext: AadContext): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
            updateAAD(aadContext.raw.toByteArray(Charset))
        }
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charset))

        return Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    }

    @Synchronized
    private fun getOrCreateKey(): SecretKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey ?: createKey()

    @Synchronized
    private fun resetKey() {
        if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
    }

    private fun createKey(): SecretKey =
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setKeySize(KEY_SIZE_BITS)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build(),
            )
        }.generateKey()
}
