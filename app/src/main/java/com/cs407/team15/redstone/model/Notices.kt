package com.cs407.team15.redstone.model

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class Notices(
    var writer: String?,
    var title: String?,
    var content: String?,
    var date: String?,
    var notice_id: String,
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

        suspend fun submitNotice(writer: String?, title: String?, content: String?, user_id: String): Boolean {
            val date = SimpleDateFormat("yyyy-MM-dd").format(Date())
            val notice_id = UUID.randomUUID().toString()
            val notice = hashMapOf("writer" to writer, "title" to title, "content" to content,
                "date" to date, "notice_id" to notice_id, "is_dismissed" to false)

            return try {
                FirebaseFirestore.getInstance().collection("users").document(user_id)
                    .collection("notices").add(notice).await()
                true
            }
            catch (e: FirebaseException) {
                false
            }
        }

        fun noticesDocumentToNotices(noticeDocument: DocumentSnapshot): Notices {
            val writer = noticeDocument.getString("writer")
            val title = noticeDocument.getString("title")
            val content = noticeDocument.getString("content")
            val date = noticeDocument.getString("date")
            val notice_id = noticeDocument.getString("notice_id") as String
            val is_dismissed = noticeDocument.getBoolean("is_dismissed") as Boolean
            return Notices(writer, title, content, date, notice_id, is_dismissed)
        }
    }
}

