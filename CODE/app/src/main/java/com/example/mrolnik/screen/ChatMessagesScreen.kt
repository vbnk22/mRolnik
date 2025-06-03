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
import com.example.mrolnik.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*



val senderUserId = UserService.getLoggedUserId()

@Composable
fun ChatMessagesScreen(
    chatRoomId: Int,
    otherUserName: String,
    navController: NavController
) {
    var messages by remember {mutableStateOf(emptyList<Chat>())}
    val backIcon = painterResource(R.drawable.baseline_arrow_back)
    var newMessage by remember { mutableStateOf("") }


    Column(modifier = Modifier.fillMaxSize()) {

        // TopBar z nazwą użytkownika
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, top = 8.dp, start = 8.dp, end = 8.dp)
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
                text = "Czat z: $otherUserName",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }

        LaunchedEffect(Unit) {
            messages = chatService.getMessages(chatRoomId)
        }

        // Lista wiadomości
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message)
            }
        }

        // Pole do wpisywania nowej wiadomości
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                label = { Text("Wpisz wiadomość") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newMessage.isNotBlank()) {
//                        messages = messages + Chat(
//                            chatId = messages.size + 1,
//                            chatRoomId = chatRoomId,
//                            senderId = 1,
//                            message = newMessage,
//                            timestamp = System.currentTimeMillis()
//                        )

                        val message = Chat(
                            chatRoomId = chatRoomId,
                            senderUserId = senderUserId,
                            message = newMessage,
                            timestamp = System.currentTimeMillis()
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            chatService.sendMessage(message)
                        }
                        newMessage = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Wyślij")
            }
        }
    }
}

@Composable
fun MessageBubble(message: Chat) {
    val isCurrentUser = message.senderUserId == senderUserId // TODO: Zastąpić aktualnym użytkownikiem
    val bubbleColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val textColor = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary

    val time = formatTimestamp(message.timestamp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = bubbleColor,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.message,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = time,
                    color = textColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}
