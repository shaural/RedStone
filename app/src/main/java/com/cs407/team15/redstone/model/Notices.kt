package com.cs407.team15.redstone.model

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class Notices(
    var writer: String?,
    var title: String?,
    var content: String?,
    var date: String?,
    var notice_id: Int,
    var is_dismissed: Boolean
) {
    companion object {
        suspend fun getNoticesForUser(user_id: String): List<Notices> {
            return try {
                val noticeDocuments = FirebaseFirestore.getInstance().collection("users")
                    .document(user_id).collection("notices").get().await().documents
                noticeDocuments.map(this::noticesDocumentToNotices)
            }
            catch (e: FirebaseException) {
                mutableListOf<Notices>()
            }
        }

        suspend fun getNoticesForUser(): List<Notices> {
            return getNoticesForUser(FirebaseAuth.getInstance().currentUser!!.uid)
        }

        fun noticesDocumentToNotices(noticeDocument: DocumentSnapshot): Notices {
            val writer = noticeDocument.getString("writer")
            val title = noticeDocument.getString("title")
            val content = noticeDocument.getString("content")
            val date = noticeDocument.getString("date")
            val notice_id = noticeDocument.getLong("notice_id") as Int
            val is_dismissed = noticeDocument.getBoolean("is_dismissed") as Boolean
            return Notices(writer, title, content, date, notice_id, is_dismissed)
        }
    }

    suspend fun dismiss() {
        val thisNoticesDocument = FirebaseFirestore.getInstance().collectionGroup("notices")
            .whereEqualTo("notice_id", notice_id).get().await().documents.first()
        thisNoticesDocument.reference.update("is_dismissed", true).await()
    }
}

