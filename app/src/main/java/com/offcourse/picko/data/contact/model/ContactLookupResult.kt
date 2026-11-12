package com.offcourse.picko.data.contact.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
sealed interface ContactLookupResult {
    data class Found(val contact: ContactModel) : ContactLookupResult

    data object NotFound : ContactLookupResult

    // TODO: 부분 일치(LIKE)로 찾고 후보 목록에서 고르게 하는 선택 UI를 만든다. 지금은 화면이 후보 개수만 보여준다.
    data class Multiple(val candidates: ImmutableList<ContactModel>) : ContactLookupResult
}

/** 같은 번호는 한 명으로 보고, 후보가 0명이면 NotFound, 1명이면 Found, 2명 이상이면 Multiple */
fun List<ContactModel>.toLookupResult(): ContactLookupResult {
    val distinct = distinctBy { contact -> contact.phoneNumber.filter(Char::isDigit) }

    return when (distinct.size) {
        0 -> ContactLookupResult.NotFound
        1 -> ContactLookupResult.Found(distinct.first())
        else -> ContactLookupResult.Multiple(distinct.toImmutableList())
    }
}
