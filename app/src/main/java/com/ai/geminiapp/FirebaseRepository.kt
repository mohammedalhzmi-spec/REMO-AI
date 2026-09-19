package com.ai.geminiapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    suspend fun saveMessage(message: ChatMessage, mode: String) {
        try {
            val user = auth.currentUser ?: return
            val messageMap = mapOf(
                "text" to message.text,
                "isUser" to message.isUser,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("users").document(user.uid)
                .collection("modes").document(mode)
                .collection("messages").add(messageMap).await()
        } catch (e: Exception) {
            // Silently fail to prevent crash
        }
    }

    suspend fun getMessages(mode: String): List<ChatMessage> {
        return try {
            val user = auth.currentUser ?: return emptyList()
            val snapshot = db.collection("users").document(user.uid)
                .collection("modes").document(mode)
                .collection("messages").orderBy("timestamp").get().await()
            
            snapshot.documents.map { doc ->
                ChatMessage(
                    text = doc.getString("text") ?: "",
                    isUser = doc.getBoolean("isUser") ?: false
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
