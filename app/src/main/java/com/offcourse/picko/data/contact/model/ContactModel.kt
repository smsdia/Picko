package com.offcourse.picko.data.contact.model

import androidx.compose.runtime.Immutable

@Immutable
data class ContactModel(
    val name: String,
    val phoneNumber: String,
)
