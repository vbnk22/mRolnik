package com.example.mrolnik.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mrolnik.R
import com.example.mrolnik.model.Chat
import com.example.mrolnik.model.ChatRoom
import com.example.mrolnik.model.Fertilizer
import com.example.mrolnik.model.Offer
import com.example.mrolnik.service.ChatService
import com.example.mrolnik.service.UserService
import com.example.mrolnik.service.userService
import com.example.mrolnik.viewmodel.LocalSharedViewModel

val chatService = ChatService()
val currentUserId = UserService.getLoggedUserId()
@Composable
fun ChatScreen(navController: NavController) {
    // TODO: Zamień na rzeczywiste dane z bazy danych (np. Firebase)
    var chatRooms by remember {mutableStateOf(emptyList<ChatRoom>())}
    val backIcon = painterResource(R.drawable.baseline_arrow_back)


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = backIcon,
                    contentDescription = "Wróć",
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Chat",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
        Text("Twoje pokoje czatu", style = MaterialTheme.typography.headlineSmall)

        LaunchedEffect(Unit) {
            chatRooms = chatService.getChatRooms(currentUserId)
        }

        LazyColumn {
            items(chatRooms) { room ->
                ChatRoomCard(room = room, navController = navController)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ChatRoomCard(room: ChatRoom, navController: NavController) {
    // TODO: Pobrać nazwę użytkownika (np. z secondUserId) z bazy danych
    var secondUserName : String? = ""
    LaunchedEffect(Unit) {
        val secondUserId = chatService.getReceiverUserId(currentUserId)
        secondUserName = if (secondUserId != null) {
            userService.getNameFromId(secondUserId)
        } else {
            "Nieznany użytkownik"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = secondUserName ?: "Nieznany użytkownik",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = {
                // TODO: Przekazać ID pokoju czatu jako argument do ChatMessagesScreen
                navController.navigate("chatMessages/${room.chatRoomId}/Użytkownik ${room.secondUserId}")
            }) {
                Text("Wejdź")
            }
        }
    }
}
