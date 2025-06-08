package com.example.mrolnik.screen

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.mrolnik.R
import com.example.mrolnik.model.Offer
import com.example.mrolnik.service.MarketplaceService
import com.example.mrolnik.service.UserService
import com.example.mrolnik.service.OfferService
import com.example.mrolnik.model.MarketplaceItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

var userService = UserService()
var marketplaceService = MarketplaceService()

@Composable
fun MarketplaceScreen(navController: NavController) {
    // Form fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var publicationDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    var expandedItemId by remember { mutableStateOf<Int?>(null) }
    val loggedInUser = UserService.getLoggedUser().firstName + " " + UserService.getLoggedUser().lastName
    val loggedInUserId: Int = UserService.getLoggedUserId()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var itemBeingEdited by remember { mutableStateOf<MarketplaceItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var marketplaceItems by remember { mutableStateOf<List<MarketplaceItem>>(emptyList()) }
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(Unit) {
        try {
            Log.d("Marketplace", "Pobieram oferty...")
            val items = marketplaceService.getAllMarketplaceItems()
            Log.d("Marketplace", "Pobrano: ${items.size}")
            marketplaceItems = items
        } catch (e: Exception) {
            Log.e("MarketplaceScreen", "Błąd przy pobieraniu ofert: ${e.message}")
        }
    }

    var showOnlyMyOffers by remember { mutableStateOf(false) }
    val filteredItems = if (showOnlyMyOffers) {
        marketplaceItems.filter { it.userId == loggedInUserId }
    } else {
        marketplaceItems
    }

    fun uriToByteArray(context: Context, uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: ByteArray(0)
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
                    text = "Rynek",
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
                        contentDescription = "Dodaj ofertę",
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
                    onClick = {
                        showOnlyMyOffers = !showOnlyMyOffers
                        coroutineScope.launch {
                            try {
                                marketplaceItems = if (showOnlyMyOffers) {
                                    marketplaceService.getMarketplaceItemsByUserId(UserService.getLoggedUserId())
                                } else {
                                    marketplaceService.getAllMarketplaceItems()
                                }
                            } catch (e: Exception) {
                                Log.e("MarketplaceScreen", "Błąd przy filtrowaniu ofert: ${e.message}")
                            }
                        }
                    },
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
                text = if (showOnlyMyOffers) "Moje oferty" else "Wszystkie oferty",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista ofert
            if (filteredItems.isEmpty()) {
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
                            imageVector = Icons.Default.Store,
                            contentDescription = "Brak ofert",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showOnlyMyOffers) "Brak Twoich ofert" else "Brak ofert",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Dodaj pierwszą ofertę klikając przycisk powyżej",
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
                    itemsIndexed(filteredItems) { index, item ->
                        MarketplaceItemCard(
                            item = item,
                            isExpanded = expandedItemId == item.marketplaceItemId,
                            onClick = {
                                expandedItemId = if (expandedItemId == item.marketplaceItemId) null else item.marketplaceItemId
                            },
                            loggedInUserId = loggedInUserId,
                            onMessageClick = {
                                coroutineScope.launch {
                                    chatService.createChatRoom(loggedInUserId, item.userId)
                                }
                            },
                            onDelete = { itemToDelete ->
                                itemBeingEdited = itemToDelete
                                showDeleteDialog = true
                            },
                            onEdit = { itemToEdit ->
                                itemBeingEdited = itemToEdit
                                title = itemToEdit.title
                                description = itemToEdit.description
                                price = itemToEdit.price.toString()
                                publicationDate = itemToEdit.publicationDate
                                location = itemToEdit.location
                                isActive = itemToEdit.isActive
                                showEditDialog = true
                            }
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

        // Dialog dodawania oferty
        if (showAddDialog) {
            CustomModalDialog(
                onDismiss = {
                    showAddDialog = false
                    title = ""
                    description = ""
                    price = ""
                    publicationDate = ""
                    location = ""
                    isActive = true
                    selectedImageUri = null
                },
                title = "Nowa oferta",
                onConfirm = {
                    keyboardController?.hide()
                    focusManager.clearFocus()

                    if (title.isBlank() || description.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Tytuł i opis nie mogą być puste",
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@CustomModalDialog
                    }

                    coroutineScope.launch {
                        try {
                            val success = if (selectedImageUri != null) {
                                val imageBytes = uriToByteArray(context, selectedImageUri!!)
                                val imageName = "image_${System.currentTimeMillis()}"
                                marketplaceService.addMarketplaceItem(
                                    MarketplaceItem(
                                        userId = UserService.getLoggedUserId(),
                                        title = title,
                                        description = description,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        image = null,
                                        publicationDate = publicationDate,
                                        location = location,
                                        isActive = isActive
                                    ),
                                    imageBytes,
                                    imageName
                                )
                            } else {
                                marketplaceService.addMarketplaceItem(
                                    MarketplaceItem(
                                        userId = UserService.getLoggedUserId(),
                                        title = title,
                                        description = description,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        image = null,
                                        publicationDate = publicationDate,
                                        location = location,
                                        isActive = isActive
                                    ),
                                    ByteArray(0),
                                    ""
                                )
                            }

                            marketplaceItems = marketplaceService.getAllMarketplaceItems()
                            snackbarHostState.showSnackbar(
                                message = "Oferta została dodana",
                                duration = SnackbarDuration.Short
                            )
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(
                                message = "Błąd podczas dodawania oferty",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }

                    title = ""
                    description = ""
                    price = ""
                    publicationDate = ""
                    location = ""
                    isActive = true
                    selectedImageUri = null
                    showAddDialog = false
                },
                content = {
                    Column {
                        Text(
                            text = "Dodajesz jako: $loggedInUser",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Tytuł") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Title,
                                    contentDescription = "Tytuł",
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Opis") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Opis",
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

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Cena (zł)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = "Cena",
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF757575)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Wybierz obrazek",
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Wybierz obrazek",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }

                        selectedImageUri?.let { uri ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Wybrano: ${uri.lastPathSegment ?: "obrazek"}",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = publicationDate,
                            onValueChange = { publicationDate = it },
                            label = { Text("Data publikacji") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Data",
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Lokalizacja") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Lokalizacja",
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF4CAF50)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aktywne",
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            )
        }

        // Dialog edycji oferty
        if (showEditDialog && itemBeingEdited != null) {
            CustomModalDialog(
                onDismiss = {
                    showEditDialog = false
                    itemBeingEdited = null
                    title = ""
                    description = ""
                    price = ""
                    publicationDate = ""
                    location = ""
                    isActive = true
                },
                title = "Edytuj ofertę",
                onConfirm = {
                    keyboardController?.hide()
                    focusManager.clearFocus()

                    if (title.isBlank() || description.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Tytuł i opis nie mogą być puste",
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@CustomModalDialog
                    }

                    itemBeingEdited?.let { item ->
                        val updatedItem = MarketplaceItem(
                            userId = item.userId,
                            title = title,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            image = item.image,
                            publicationDate = publicationDate,
                            location = location,
                            isActive = isActive
                        )
                        updatedItem.marketplaceItemId = item.marketplaceItemId

                        coroutineScope.launch {
                            try {
                                marketplaceService.updateMarketplaceItem(updatedItem)
                                marketplaceItems = marketplaceService.getAllMarketplaceItems()
                                snackbarHostState.showSnackbar(
                                    message = "Oferta została zaktualizowana",
                                    duration = SnackbarDuration.Short
                                )
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Błąd podczas aktualizacji oferty",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }

                    showEditDialog = false
                    itemBeingEdited = null
                    title = ""
                    description = ""
                    price = ""
                    publicationDate = ""
                    location = ""
                    isActive = true
                },
                content = {
                    Column {
                        Text(
                            text = "Edytujesz jako: $loggedInUser",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Tytuł") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Title,
                                    contentDescription = "Tytuł",
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Opis") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Opis",
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

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Cena (zł)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = "Cena",
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = publicationDate,
                            onValueChange = { publicationDate = it },
                            label = { Text("Data publikacji") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Data",
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Lokalizacja") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Lokalizacja",
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF4CAF50)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aktywne",
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            )
        }

        // Dialog usuwania oferty
        if (showDeleteDialog && itemBeingEdited != null) {
            CustomModalDialog(
                onDismiss = {
                    showDeleteDialog = false
                    itemBeingEdited = null
                },
                title = "Potwierdź usunięcie",
                onConfirm = {
                    itemBeingEdited?.let { item ->
                        coroutineScope.launch {
                            try {
                                marketplaceService.deleteMarketplaceItem(item)
                                marketplaceItems = marketplaceService.getAllMarketplaceItems()
                                snackbarHostState.showSnackbar(
                                    message = "Oferta została usunięta",
                                    duration = SnackbarDuration.Short
                                )
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Błąd podczas usuwania oferty",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }

                    showDeleteDialog = false
                    itemBeingEdited = null
                },
                content = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Ostrzeżenie",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFE57373)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Czy na pewno chcesz usunąć tę ofertę?",
                            fontSize = 16.sp,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = itemBeingEdited?.title ?: "",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ta operacja jest nieodwracalna.",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun MarketplaceItemCard(
    item: MarketplaceItem,
    isExpanded: Boolean = false,
    onClick: () -> Unit = {},
    loggedInUserId: Int,
    onMessageClick: () -> Unit = {},
    onDelete: (MarketplaceItem) -> Unit = {},
    onEdit: (MarketplaceItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Obrazek na górze - pełna szerokość
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (item.image != null) {
                    SubcomposeAsyncImage(
                        model = item.image,
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF5F5F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF5F5F5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Błąd ładowania obrazka",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    )
                } else {
                    // Placeholder gdy brak obrazka
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F5F5))
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Brak obrazka",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                    }
                }
            }

            // Zawartość karty
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Nagłówek z tytułem, ceną i przyciskiem rozwijania
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Tytuł
                        Text(
                            text = item.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Cena
                        Text(
                            text = "${String.format("%.2f", item.price)} zł",
                            fontSize = 20.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Przycisk rozwijania
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Rozwinięte informacje
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Opis
                    if (item.description.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Opis",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 14.sp,
                                    color = Color(0xFF424242),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Informacje dodatkowe
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Data publikacji
                            if (item.publicationDate.isNotBlank()) {
                                InfoRowMarketplace(
                                    icon = Icons.Default.DateRange,
                                    label = "Data publikacji",
                                    value = item.publicationDate
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Lokalizacja
                            if (item.location.isNotBlank()) {
                                InfoRowMarketplace(
                                    icon = Icons.Default.LocationOn,
                                    label = "Lokalizacja",
                                    value = item.location
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Sprzedający
                            val sellerName = remember { mutableStateOf("") }

                            LaunchedEffect(item.userId) {
                                try {
                                    val name = userService.getNameFromId(item.userId)
                                    sellerName.value = name ?: ""
                                } catch (e: Exception) {
                                    Log.e("MarketplaceScreen", "Nie udało się pobrać imienia i nazwiska: ${e.message}")
                                }
                            }

                            if (sellerName.value.isNotBlank()) {
                                InfoRowMarketplace(
                                    icon = Icons.Default.Person,
                                    label = "Sprzedający",
                                    value = sellerName.value
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Status aktywności
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (item.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = "Status",
                                    modifier = Modifier.size(16.dp),
                                    tint = if (item.isActive) Color(0xFF4CAF50) else Color(0xFFE57373)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Status:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF757575)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (item.isActive) "Aktywne" else "Nieaktywne",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.isActive) Color(0xFF4CAF50) else Color(0xFFE57373)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Przyciski akcji
                    if (item.userId != loggedInUserId) {
                        // Przycisk dla innych użytkowników
                        Button(
                            onClick = onMessageClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
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
                                contentDescription = "Napisz wiadomość",
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Napisz wiadomość",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    } else {
                        // Przyciski dla właściciela oferty
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onEdit(item) },
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
                                onClick = { onDelete(item) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE57373)
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
}

@Composable
private fun InfoRowMarketplace(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = Color(0xFF757575)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF424242)
        )
    }
}

