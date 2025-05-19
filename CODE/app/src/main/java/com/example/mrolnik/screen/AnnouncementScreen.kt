package com.example.mrolnik.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mrolnik.R

@Composable
fun AnnouncementScreen(navController: NavController) {
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedAnnouncement by remember { mutableStateOf<Announcement?>(null) }
    var editedDescription by remember { mutableStateOf("") }

    val backIcon = painterResource(R.drawable.baseline_arrow_back)

    val loggedInUser = "Jan Kowalski"

    val announcements = remember {
        mutableStateListOf(
            Announcement("Kupię nawóz azotowy", "Jan Kowalski"),
            Announcement("Sprzedam ciągnik Ursus C-360, dobry stan", "Anna Nowak"),
            Announcement("Zatrudnię pomocnika na żniwa", "Piotr Zieliński")
        )
    }

    var showOnlyMyAnnouncements by remember { mutableStateOf(false) }
    val filteredAnnouncements = if (showOnlyMyAnnouncements) {
        announcements.filter { it.username == loggedInUser }
    } else {
        announcements
    }

    var description by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                text = "Ogłoszenia",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showAddDialog = true }) {
                Text("Dodaj ogłoszenie")
            }
            Button(onClick = { showOnlyMyAnnouncements = !showOnlyMyAnnouncements }) {
                Text(if (showOnlyMyAnnouncements) "Wszystkie ogłoszenia" else "Moje ogłoszenia")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            itemsIndexed(filteredAnnouncements) { index, announcement ->
                AnnouncementItem(
                    announcement = announcement,
                    isExpanded = expandedIndex == index,
                    onClick = {
                        expandedIndex = if (expandedIndex == index) null else index
                    },
                    loggedInUser = loggedInUser,
                    onEdit = {
                        selectedAnnouncement = announcement
                        editedDescription = announcement.description
                        showEditDialog = true
                    },
                    onDelete = {
                        selectedAnnouncement = announcement
                        showDeleteDialog = true
                    }
                )
            }
        }

        if (showAddDialog) {
            CustomModalDialog(
                onDismiss = { showAddDialog = false },
                title = "Nowe ogłoszenie",
                onConfirm = {
                    if (description.isNotBlank()) {
                        announcements.add(Announcement(description, loggedInUser))
                        description = ""
                    }
                    showAddDialog = false
                },
                content = {
                    Text("Dodajesz jako: $loggedInUser", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Treść ogłoszenia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }

        if (showEditDialog && selectedAnnouncement != null) {
            CustomModalDialog(
                onDismiss = {
                    showEditDialog = false
                    selectedAnnouncement = null
                },
                title = "Edytuj ogłoszenie",
                onConfirm = {
                    selectedAnnouncement?.let {
                        val index = announcements.indexOf(it)
                        if (index != -1) {
                            announcements[index] = it.copy(description = editedDescription)
                        }
                    }
                    showEditDialog = false
                    selectedAnnouncement = null
                },
                content = {
                    Text("Edytujesz jako: $loggedInUser", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Nowa treść ogłoszenia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }

        if (showDeleteDialog && selectedAnnouncement != null) {
            CustomModalDialog(
                onDismiss = {
                    showDeleteDialog = false
                    selectedAnnouncement = null
                },
                title = "Potwierdź usunięcie",
                onConfirm = {
                    announcements.remove(selectedAnnouncement)
                    showDeleteDialog = false
                    selectedAnnouncement = null
                },
                content = {
                    Text("Czy na pewno chcesz usunąć ogłoszenie?", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("\"${selectedAnnouncement?.description}\"")
                }
            )
        }
    }
}
//
data class Announcement(
    val description: String,
    val username: String
)

@Composable
fun AnnouncementItem(
    announcement: Announcement,
    isExpanded: Boolean,
    onClick: () -> Unit,
    loggedInUser: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = announcement.description.take(30) + if (announcement.description.length > 30) "..." else "",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("${announcement.description}")
                Text("Dodane przez: ${announcement.username}")
                Spacer(modifier = Modifier.height(8.dp))
                if (announcement.username != loggedInUser) {
                    Button(onClick = {
                        // TODO: dodać obsługę czatu z użytkownikiem udostępniającym ogłoszenie
                    }) {
                        Text("Napisz wiadomość")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onEdit) {
                            Text("Edytuj ogłoszenie")
                        }
                        Button(onClick = onDelete) {
                            Text("Usuń ogłoszenie")
                        }
                    }
                }
            }
        }
    }
}
