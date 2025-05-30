package com.example.mrolnik.model


data class Chat(
    val chatId: Int,
    val chatRoomId: Int,
    val message: String, // przyjmujemy, że wiadomość będzie tekstem
    val timestamp: Long,
    val senderId: Int
)