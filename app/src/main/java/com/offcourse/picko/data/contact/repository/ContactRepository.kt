package com.offcourse.picko.data.contact.repository

import com.offcourse.picko.data.contact.model.ContactLookupResult

interface ContactRepository {
    /**
     * 이름이 정확히 같은 연락처를 찾는다.
     * READ_CONTACTS 권한이 없으면 SecurityException 을 던진다.
     */
    suspend fun findByName(name: String): ContactLookupResult
}
