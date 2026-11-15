package com.offcourse.picko.core.crypto

sealed class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** 키를 다시 만든 뒤에도 암호화에 실패했다. 호출하는 쪽은 디스크 저장을 포기하고 메모리에만 값을 둔다. */
class EncryptionFailedException(cause: Throwable? = null) : CryptoException("암호화 실패", cause)

/** 인증 태그가 맞지 않거나 키를 쓸 수 없다. 데이터 손상, 키 불일치, AAD 불일치이며 복구할 수 없다. */
class DecryptionAuthFailedException(cause: Throwable? = null) : CryptoException("복호화 인증 실패", cause)

/** Base64 해석에 실패하는 등 입력 형식이 잘못됐다. */
class DecryptionMalformedInputException(cause: Throwable? = null) : CryptoException("복호화 입력 형식 오류", cause)
