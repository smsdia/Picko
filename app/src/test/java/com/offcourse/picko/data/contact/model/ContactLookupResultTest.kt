package com.offcourse.picko.data.contact.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactLookupResultTest {
    @Test
    fun 후보가_없으면_NotFound다() {
        assertEquals(ContactLookupResult.NotFound, emptyList<ContactModel>().toLookupResult())
    }

    @Test
    fun 후보가_하나면_Found다() {
        val mom = ContactModel(name = "엄마", phoneNumber = "010-1234-5678")

        assertEquals(ContactLookupResult.Found(mom), listOf(mom).toLookupResult())
    }

    @Test
    fun 표기만_다른_같은_번호는_한_명으로_본다() {
        val result = listOf(
            ContactModel(name = "엄마", phoneNumber = "010-1234-5678"),
            ContactModel(name = "엄마", phoneNumber = "01012345678"),
        ).toLookupResult()

        assertTrue(result is ContactLookupResult.Found)
    }

    @Test
    fun 번호가_다르면_Multiple이다() {
        val result = listOf(
            ContactModel(name = "엄마", phoneNumber = "010-1234-5678"),
            ContactModel(name = "엄마", phoneNumber = "010-9999-0000"),
        ).toLookupResult()

        assertEquals(2, (result as ContactLookupResult.Multiple).candidates.size)
    }
}
