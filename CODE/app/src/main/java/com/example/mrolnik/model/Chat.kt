package com.example.mrolnik.model


data class Chat(
    val chatId: Int? = null,
    val chatRoomId: Int,
    val message: String, // przyjmujemy, że wiadomość będzie tekstem
    val timestamp: Long,
    val senderUserId: Int
)