package com.example.mrolnik.service
import android.util.Log
import com.example.mrolnik.config.SupabaseClient
import com.example.mrolnik.model.Chat
import com.example.mrolnik.model.ChatRoom
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class ChatService {
    private val supabase = SupabaseClient().getSupabaseClient()

    suspend fun getMessages(chatRoomId: Int): List<Chat> {
        return try {
            supabase.from("chat")
                .select {
                    filter {
                        eq("chatRoomId", chatRoomId)
                    }
                    order("timestamp", order = Order.ASCENDING)
                }
                .decodeList<Chat>()
        } catch (e: Exception) {
            Log.e("ChatService", "Fetching messages error: ${e.message}")
            emptyList()
        }
    }

    suspend fun sendMessage(chat: Chat): Boolean {
        return try {
            supabase.from("chat").insert(
                mapOf(
                    "chatRoomId" to chat.chatRoomId,
                    "message" to chat.message,
                    "timestamp" to chat.timestamp,
                    "senderUserId" to chat.senderUserId
                )
            )
            true
        } catch (e: Exception) {
            Log.e("ChatService", "Sending message error: ${e.message}")
            false
        }
    }

    suspend fun getChatRooms(userId: Int): List<ChatRoom> {
        return try {
            supabase.from("chatRoom")
                .select {
                    filter {
                        or {
                            eq("firstUserId", userId)
                            eq("secondUserId", userId)
                        }
                    }
                }
                .decodeList<ChatRoom>()
        } catch (e: Exception) {
            Log.e("ChatService", "Fetching chat rooms error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getReceiverUserId(senderUserId: Int): Int? {
        return try {
            val result = supabase.from("chatRoom")
                .select(columns = Columns.list("secondUserId")) {
                    filter {
                        eq("firstUserId", senderUserId)
                    }
                }
                .decodeSingle<Map<String, Int>>()
            result["secondUserId"]
        } catch (e: Exception) {
            Log.e("ChatService", "Fetching receiver user ID error: ${e.message}")
            null
        }
    }

    suspend fun createChatRoom(senderUserId: Int, receiverUserId: Int): Boolean {
        return try {
            supabase.from("chatRoom").insert(
                mapOf(
                    "firstUserId" to senderUserId,
                    "secondUserId" to receiverUserId
                )
            )
            true
        } catch (e: Exception) {
            Log.e("ChatService", "Creating chat room error: ${e.message}")
            false
        }
    }
}