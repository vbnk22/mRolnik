package com.example.mrolnik.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mrolnik.model.Offer
import com.example.mrolnik.service.UserService
import com.example.mrolnik.service.OfferService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

var offerService = OfferService()

@Composable
fun AnnouncementScreen(navController: NavController) {
    val userNames = remember { mutableStateMapOf<Int, String>() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val loggedInUser = UserService.getLoggedUser().firstName + " " + UserService.getLoggedUser().lastName
    val loggedInUserId: Int = UserService.getLoggedUserId()

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header z przyciskiem powrotu i tytułem
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Wróć",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Ogłoszenia",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }

            // Przyciski zarządzania
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Dodaj ogłoszenie",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dodaj",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { showOnlyMyOffers = !showOnlyMyOffers },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showOnlyMyOffers) Color(0xFF2196F3) else Color(0xFF757575)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = if (showOnlyMyOffers) Icons.Default.Person else Icons.Default.Group,
                        contentDescription = "Przełącz widok",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showOnlyMyOffers) "Moje" else "Wszystkie",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            // Tytuł listy
            Text(
                text = if (showOnlyMyOffers) "Moje ogłoszenia" else "Wszystkie ogłoszenia",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista ogłoszeń
            if (filteredOffers.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Brak ogłoszeń",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showOnlyMyOffers) "Brak Twoich ogłoszeń" else "Brak ogłoszeń",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Dodaj pierwsze ogłoszenie klikając przycisk powyżej",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Text(
                    text = snackbarData.visuals.message,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        // Dialog dodawania ogłoszenia
        if (showAddDialog) {
            CustomModalDialog(
                onDismiss = {
                    showAddDialog = false
                    description = ""
                },
                title = "Nowe ogłoszenie",
                onConfirm = {
                    keyboardController?.hide()
                    focusManager.clearFocus()

                    if (description.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Treść ogłoszenia nie może być pusta",
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@CustomModalDialog
                    }

                    val newOffer = Offer(userId = loggedInUserId, description = description)
                    coroutineScope.launch {
                        try {
                            Log.i("AnnouncementScreen", "Adding offer: ${newOffer.offerId}")
                            val result = offerService.addOffer(newOffer)
                            if (result != null) {
                                Log.i("AnnouncementScreen", "Adding offer result: ${result.offerId}")
                                offers.add(result)
                                snackbarHostState.showSnackbar(
                                    message = "Ogłoszenie zostało dodane",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(
                                message = "Błąd podczas dodawania ogłoszenia",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                    description = ""
                    showAddDialog = false
                },
                content = {
                    Text(
                        text = "Dodajesz jako: $loggedInUser",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Treść ogłoszenia") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Ogłoszenie",
                                tint = Color(0xFF4CAF50)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )
                }
            )
        }

        // Dialog edycji ogłoszenia
        if (showEditDialog && selectedOffer != null) {
            CustomModalDialog(
                onDismiss = {
                    showEditDialog = false
                    selectedOffer = null
                    editedDescription = ""
                },
                title = "Edytuj ogłoszenie",
                onConfirm = {
                    keyboardController?.hide()
                    focusManager.clearFocus()

                    if (editedDescription.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Treść ogłoszenia nie może być pusta",
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@CustomModalDialog
                    }

                    selectedOffer?.let { offer ->
                        val updatedOffer = Offer(
                            offerId = offer.offerId,
                            userId = offer.userId,
                            description = editedDescription
                        )
                        coroutineScope.launch {
                            try {
                                offerService.updateOffer(updatedOffer)
                                val refreshedOffers = offerService.getAllOffers()
                                offers.clear()
                                offers.addAll(refreshedOffers)
                                snackbarHostState.showSnackbar(
                                    message = "Ogłoszenie zostało zaktualizowane",
                                    duration = SnackbarDuration.Short
                                )
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Błąd podczas aktualizacji ogłoszenia",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                    showEditDialog = false
                    selectedOffer = null
                    editedDescription = ""
                },
                content = {
                    Text(
                        text = "Edytujesz jako: $loggedInUser",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Nowa treść ogłoszenia") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edytuj",
                                tint = Color(0xFF4CAF50)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )
                }
            )
        }

        // Dialog usuwania ogłoszenia
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
                            try {
                                offerService.deleteOffer(offer)
                                offers.removeAll { it.offerId == offer.offerId }
                                snackbarHostState.showSnackbar(
                                    message = "Ogłoszenie zostało usunięte",
                                    duration = SnackbarDuration.Short
                                )
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Błąd podczas usuwania ogłoszenia",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                    showDeleteDialog = false
                    selectedOffer = null
                },
                content = {
                    Text(
                        text = "Czy na pewno chcesz usunąć ogłoszenie?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Text(
                            text = "\"${selectedOffer?.description}\"",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
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
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Ogłoszenie",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = offer.description.take(50) + if (offer.description.length > 50) "..." else "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                ) {
                    Text(
                        text = offer.description,
                        fontSize = 14.sp,
                        color = Color(0xFF424242),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Autor",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dodane przez: ${userNames[offer.userId] ?: "Nieznany użytkownik"}",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (loggedInUserId != offer.userId) {
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                chatService.createChatRoom(loggedInUserId, offer.userId)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "Wiadomość",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Napisz wiadomość",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onEdit,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edytuj",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edytuj",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = onDelete,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF44336)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Usuń",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Usuń",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}