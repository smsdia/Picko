package com.offcourse.picko.data.contact.repositoryimpl

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.offcourse.picko.data.contact.model.ContactLookupResult
import com.offcourse.picko.data.contact.model.ContactModel
import com.offcourse.picko.data.contact.model.toLookupResult
import com.offcourse.picko.data.contact.repository.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ContactRepository {
    override suspend fun findByName(name: String): ContactLookupResult = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<ContactModel>()

        // TODO: 정확 일치(=)를 부분 일치(LIKE)로 넓힌다. 결과 타입은 이미 Multiple 을 지원한다.
        context.contentResolver
            .query(
                Phone.CONTENT_URI,
                arrayOf(Phone.DISPLAY_NAME, Phone.NUMBER),
                "${Phone.DISPLAY_NAME} = ?",
                arrayOf(name),
                null,
            )
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndexOrThrow(Phone.NUMBER)

                while (cursor.moveToNext()) {
                    contacts += ContactModel(
                        name = cursor.getString(nameIndex).orEmpty(),
                        phoneNumber = cursor.getString(numberIndex).orEmpty(),
                    )
                }
            }

        contacts.toLookupResult()
    }
}
