package com.offcourse.picko.core.crypto

interface CryptoManager {
    /** [plaintext]를 암호화한 Base64 문자열. 실패하면 [EncryptionFailedException]. */
    fun encrypt(plaintext: String, aadContext: AadContext): String

    /** [encrypt]가 만든 문자열을 복호화한다. 실패하면 [CryptoException]. */
    fun decrypt(ciphertext: String, aadContext: AadContext): String
}
