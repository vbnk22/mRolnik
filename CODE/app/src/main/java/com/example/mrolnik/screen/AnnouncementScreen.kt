package com.example.mrolnik.screen

import android.annotation.SuppressLint
import android.util.Log
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
import com.example.mrolnik.model.Offer
import com.example.mrolnik.service.UserService
import com.example.mrolnik.service.OfferService
import kotlinx.coroutines.launch

var offerService = OfferService()

@Composable
fun AnnouncementScreen(navController: NavController) {
    val userNames = remember { mutableStateMapOf<Int, String>() }

    val coroutineScope = rememberCoroutineScope()

    val loggedInUser = UserService.getLoggedUser().firstName + " " + UserService.getLoggedUser().lastName
    val loggedInUserId:Int = UserService.getLoggedUserId()

    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedOffer by remember { mutableStateOf<Offer?>(null) }
    var editedDescription by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var offers = remember { mutableStateListOf<Offer>() }

    LaunchedEffect(Unit) {
        val loadedOffers = offerService.getAllOffers()
        offers.clear()
        offers.addAll(loadedOffers)

        val userIds = loadedOffers.map { it.userId }.distinct()
        userIds.forEach { userId ->
            if (userId !in userNames) {
                val name = UserService().getNameFromId(userId)
                if (name != null) {
                    userNames[userId] = name
                }
            }
        }
    }


    var showOnlyMyOffers by remember { mutableStateOf(false) }
    val filteredOffers = if (showOnlyMyOffers) {
        offers.filter { it.userId == loggedInUserId }
    } else {
        offers
    }

    val backIcon = painterResource(R.drawable.baseline_arrow_back)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(painter = backIcon, contentDescription = "Wróć", modifier = Modifier.size(24.dp))
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
            Button(onClick = { showOnlyMyOffers = !showOnlyMyOffers }) {
                Text(if (showOnlyMyOffers) "Wszystkie ogłoszenia" else "Moje ogłoszenia")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            itemsIndexed(filteredOffers) { index, offer ->
                OfferItem(
                    offer = offer,
                    isExpanded = expandedIndex == index,
                    onClick = {
                        expandedIndex = if (expandedIndex == index) null else index
                    },
                    loggedInUserId = UserService.getLoggedUserId(),
                    onEdit = {
                        selectedOffer = offer
                        editedDescription = offer.description
                        showEditDialog = true
                    },
                    onDelete = {
                        selectedOffer = offer
                        showDeleteDialog = true
                    },
                    userNames = userNames
                )
            }
        }

        if (showAddDialog) {
            CustomModalDialog(
                onDismiss = { showAddDialog = false },
                title = "Nowe ogłoszenie",
                onConfirm = {
                    if (description.isNotBlank()) {
                        val newOffer = Offer(userId = loggedInUserId, description = description)
                        coroutineScope.launch {
                            Log.i("AnnouncementScreen", "Adding offer: ${newOffer.offerId}")
                            val result = offerService.addOffer(newOffer)
                            if (result != null) {
                                Log.i("AnnouncementScreen", "Adding offer ress: ${result.offerId}")
                                offers.add(result)
                            }
                        }
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

        if (showEditDialog && selectedOffer != null) {
            CustomModalDialog(
                onDismiss = {
                    showEditDialog = false
                    selectedOffer = null
                },
                title = "Edytuj ogłoszenie",
                onConfirm = {
                    selectedOffer?.let { offer ->
                        val updatedOffer = Offer(
                            offerId = offer.offerId,
                            userId = offer.userId,
                            description = editedDescription
                        )
                        coroutineScope.launch {
                            offerService.updateOffer(updatedOffer)

                            val refreshedOffers = offerService.getAllOffers()
                            offers.clear()
                            offers.addAll(refreshedOffers)
                        }
                    }
                    showEditDialog = false
                    selectedOffer = null
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

        if (showDeleteDialog && selectedOffer != null) {
            CustomModalDialog(
                onDismiss = {
                    showDeleteDialog = false
                    selectedOffer = null
                },
                title = "Potwierdź usunięcie",
                onConfirm = {
                    selectedOffer?.let { offer ->
                        coroutineScope.launch {
                            offerService.deleteOffer(offer)
                            offers.removeAll { it.offerId == offer.offerId }
                        }
                    }
                    showDeleteDialog = false
                    selectedOffer = null
                },
                content = {
                    Text("Czy na pewno chcesz usunąć ogłoszenie?", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("\"${selectedOffer?.description}\"")
                }
            )
        }
    }
}

@Composable
fun OfferItem(
    offer: Offer,
    isExpanded: Boolean,
    onClick: () -> Unit,
    loggedInUserId: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    userNames: Map<Int, String>
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
                    text = offer.description.take(30) + if (offer.description.length > 30) "..." else "",
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
                Text(offer.description)
                Spacer(modifier = Modifier.height(4.dp))

                val name = userNames[offer.userId]
                Text("Dodane przez: ${name ?: "Nieznany użytkownik"}")




                Spacer(modifier = Modifier.height(8.dp))
                if (loggedInUserId != offer.userId) {
                    Button(onClick = {
                        // TODO: dodaj obsługę wiadomości
                    }) {
                        Text("Napisz wiadomość")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onEdit) {
                            Text("Edytuj")
                        }
                        Button(onClick = onDelete) {
                            Text("Usuń")
                        }
                    }
                }
            }
        }
    }
}
